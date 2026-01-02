package com.hotel.hotel.service;

import com.hotel.hotel.entity.HotelRoomType; // 使用正确的实体类
import com.hotel.hotel.entity.PricingRecord;
import com.hotel.hotel.entity.Booking;
import com.hotel.hotel.repository.HotelRoomTypeRepository; // 需对应修改 Repository 名
import com.hotel.hotel.repository.PricingRecordRepository;
import com.hotel.hotel.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
public class PricingService {

    private final HotelRoomTypeRepository roomTypeRepository;
    private final PricingRecordRepository pricingRecordRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public PricingService(HotelRoomTypeRepository roomTypeRepository,
                          PricingRecordRepository pricingRecordRepository,
                          BookingRepository bookingRepository) {
        this.roomTypeRepository = roomTypeRepository;
        this.pricingRecordRepository = pricingRecordRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * 配合 Controller：按状态查询调价记录
     */
    public List<PricingRecord> getRecordsByStatus(String status) {
        // 直接通过 Repository 查询，性能优于之前的 findAll 过滤
        return pricingRecordRepository.findByStatus(status);
    }

    /**
     * 配合 Controller：执行审批动作
     */
    @Transactional
    public boolean applyPriceRecord(Long recordId) {
        Optional<PricingRecord> recordOpt = pricingRecordRepository.findById(recordId);
        if (recordOpt.isPresent()) {
            PricingRecord record = recordOpt.get();
            record.setStatus("APPLIED"); // 更新状态为已应用
            pricingRecordRepository.save(record);

            // 此处可触发渠道同步模拟逻辑
            System.out.println("✅ 价格记录已批准：ID=" + recordId + ", 房型=" + record.getRoomType().getTypeCode());
            return true;
        }
        return false;
    }

    /**
     * 获取已生效价格（含保底降级逻辑）
     */
    public List<PricingRecord> getAppliedPricesByDate(LocalDate date) {
        // 1. 尝试获取已审批的价格
        List<PricingRecord> appliedRecords = pricingRecordRepository.findByEffectiveDateAndStatus(date, "APPLIED");

        // 2. 如果存在审批过的动态价格，直接返回
        if (!appliedRecords.isEmpty()) {
            return appliedRecords;
        }

        // 3. 【保底逻辑】如果没有审批记录，返回所有房型的基准价（此时不存入数据库，仅作为展示）
        System.out.println(">>> 日期 " + date + " 未找到 APPLIED 记录，返回房型基准价。");
        List<HotelRoomType> allTypes = roomTypeRepository.findByHotelId(1L);
        List<PricingRecord> fallbackRecords = new ArrayList<>();

        for (HotelRoomType type : allTypes) {
            PricingRecord fallback = PricingRecord.builder()
                    .roomType(type)
                    .effectiveDate(date)
                    .basePrice(type.getBasePrice())
                    .originalPrice(type.getBasePrice())
                    .adjustedPrice(type.getBasePrice())
                    .status("BASE_PRICE_FALLBACK")
                    .adjustFactor("系统自动降级：使用房型基准价")
                    .build();
            fallbackRecords.add(fallback);
        }

        return fallbackRecords;
    }

    /**
     * 核心任务：为所有房型生成未来 7 天的调价建议
     * 修正：将起始时间调整为 2025-01-01 以适配你的数据集
     */
    @Transactional
    public void calculateAndAdjustPricesForFutureWeek() {
        // 由于 2026 年没有数据，我们模拟在 2025 年初运行
        LocalDate anchorDate = LocalDate.now();
        List<HotelRoomType> allRoomTypes = roomTypeRepository.findByHotelId(1L);

        for (HotelRoomType roomType : allRoomTypes) {
            for (int i = 0; i < 7; i++) {
                LocalDate targetDate = anchorDate.plusDays(i);

                // 使用 Long 类型的 id 进行查询
                Optional<PricingRecord> existingRecord = pricingRecordRepository
                        .findTopByRoomType_IdAndEffectiveDateOrderByAdjustTimeDesc(
                                roomType.getId(), targetDate);

                if (existingRecord.isPresent()) continue;

                adjustPrice(roomType, targetDate);
            }
        }
    }

    @Transactional
    public PricingRecord adjustPrice(HotelRoomType roomType, LocalDate targetDate) {
        // 1. 获取 room_types 表中的基础价格
        BigDecimal basePrice = roomType.getBasePrice();
        Long typeId = roomType.getId();

        // 2. 获取历史均价 (扩大搜索范围至 365 天，确保能抓到 2024 年的数据)
        BigDecimal marketAdr = getHistoricalAveragePrice(typeId);

        // 3. 计算预订率 (假设每个房型默认有 10 间房)
        int totalRooms = 10;
        double occupancyRate = calculateRealOccupancy(typeId, targetDate, totalRooms);

        // 4. 定价逻辑维持原样
        BigDecimal suggestedPrice = basePrice.multiply(new BigDecimal("0.9"))
                .add(marketAdr.multiply(new BigDecimal("0.1")));

        String factorMsg = String.format("基准:%.2f, 历史均价:%.2f, 预订率:%.2f",
                basePrice, marketAdr, occupancyRate);

        if (occupancyRate > 0.80) {
            suggestedPrice = suggestedPrice.multiply(new BigDecimal("1.3"));
            factorMsg += " | 高需求溢价(1.3x)";
        }

        BigDecimal finalPrice = suggestedPrice.setScale(2, RoundingMode.HALF_UP)
                .min(basePrice.multiply(new BigDecimal("2.5")))
                .max(basePrice.multiply(new BigDecimal("0.7")));

        PricingRecord record = new PricingRecord();
        // 注意：PricingRecord 实体中的 setRoomType 可能也需要更新为 HotelRoomType
        record.setRoomType(roomType);
        record.setBasePrice(basePrice);
        record.setOriginalPrice(basePrice);
        record.setAdjustedPrice(finalPrice);
        record.setAdjustFactor(factorMsg);
        record.setEffectiveDate(targetDate);
        record.setStatus("PENDING");

        return pricingRecordRepository.save(record);
    }

    private double calculateRealOccupancy(Long typeId, LocalDate date, Integer total) {
        if (total == null || total == 0) return 0.0;

        // 统计指定日期该房型的预订数量
        List<Booking> activeBookings = bookingRepository.findBookingsByRoomTypeAndDateRange(
                1L, typeId, date, date.plusDays(1));

        return (double) activeBookings.size() / total;
    }

    private BigDecimal getHistoricalAveragePrice(Long typeId) {
        // 修正：查找过去一整年的数据，防止 30 天内无数据导致均价为 0
        LocalDate end = LocalDate.of(2025, 12, 31);
        LocalDate start = end.minusYears(1);

        List<Booking> history = bookingRepository.findBookingsByRoomTypeAndDateRange(
                1L, typeId, start, end);

        if (history.isEmpty()) {
            return roomTypeRepository.findById(typeId)
                    .map(HotelRoomType::getBasePrice).orElse(BigDecimal.ZERO);
        }

        BigDecimal totalAdr = history.stream()
                .map(Booking::getAdr)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalAdr.divide(new BigDecimal(history.size()), 2, RoundingMode.HALF_UP);
    }
}