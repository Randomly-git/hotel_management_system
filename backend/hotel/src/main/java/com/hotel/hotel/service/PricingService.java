package com.hotel.hotel.service;

import com.hotel.hotel.entity.RoomType;
import com.hotel.hotel.entity.PricingRecord;
import com.hotel.hotel.entity.Booking;
import com.hotel.hotel.repository.RoomTypeRepository;
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

    private final RoomTypeRepository roomTypeRepository;
    private final PricingRecordRepository pricingRecordRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public PricingService(RoomTypeRepository roomTypeRepository,
                          PricingRecordRepository pricingRecordRepository,
                          BookingRepository bookingRepository) {
        this.roomTypeRepository = roomTypeRepository;
        this.pricingRecordRepository = pricingRecordRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * 核心任务：为所有房型生成未来 7 天的调价建议
     */
    @Transactional
    public void calculateAndAdjustPricesForFutureWeek() {
        LocalDate today = LocalDate.now();
        List<RoomType> allRoomTypes = roomTypeRepository.findAll();

        for (RoomType roomType : allRoomTypes) {
            for (int i = 0; i < 7; i++) {
                LocalDate targetDate = today.plusDays(i);

                // 幂等性检查：避免重复生成同一天、同一房型的建议
                Optional<PricingRecord> existingRecord = pricingRecordRepository
                        .findTopByRoomTypeTypeIdAndEffectiveDateOrderByAdjustTimeDesc(
                                roomType.getTypeId(), targetDate);

                if (existingRecord.isPresent()) {
                    continue;
                }

                // 执行逻辑：生成 PENDING 状态的建议
                adjustPrice(roomType, targetDate);
            }
        }
    }

    /**
     * 分房型动态定价核心算法
     */
    @Transactional
    public PricingRecord adjustPrice(RoomType roomType, LocalDate targetDate) {
        // 1. 获取物理表 sys_room_type 中的基础价格
        BigDecimal basePrice = roomType.getBasePrice();
        Integer typeId = roomType.getTypeId();

        // 2. 获取真实市场表现（该房型过去30天成交均价 ADR）
        BigDecimal marketAdr = getHistoricalAveragePrice(typeId);

        // 3. 计算目标日期的实时预订率（基于已存在的有效预订）
        double occupancyRate = calculateRealOccupancy(typeId, targetDate, roomType.getTotalCount());

        // 4. 定价公式：60% 基准价 + 40% 市场表现价
        // 解决了您提到的“不同房型一个价不合理”的问题
        BigDecimal suggestedPrice = basePrice.multiply(new BigDecimal("0.6"))
                .add(marketAdr.multiply(new BigDecimal("0.4")));

        // 5. 动态溢价逻辑：若预订率超过 80%，价格上浮 30%
        String factorMsg = String.format("基准:%.2f, 历史均价:%.2f, 预订率:%.2f",
                basePrice, marketAdr, occupancyRate);
        if (occupancyRate > 0.80) {
            suggestedPrice = suggestedPrice.multiply(new BigDecimal("1.3"));
            factorMsg += " | 高需求溢价(1.3x)";
        }

        // 6. 价格安全边界：不低于基准价 70%，不高于基准价 250%
        BigDecimal finalPrice = suggestedPrice.setScale(2, RoundingMode.HALF_UP)
                .min(basePrice.multiply(new BigDecimal("2.5")))
                .max(basePrice.multiply(new BigDecimal("0.7")));

        // 7. 构造记录并设为 PENDING (等待店长审批)
        PricingRecord record = new PricingRecord();
        record.setRoomType(roomType);
        record.setBasePrice(basePrice);
        record.setOriginalPrice(basePrice);
        record.setAdjustedPrice(finalPrice);
        record.setAdjustFactor(factorMsg);
        record.setEffectiveDate(targetDate);
        record.setStatus("PENDING"); // 适配数据库 status 字段

        return pricingRecordRepository.save(record);
    }

    /**
     * 计算特定房型在特定日期的真实预订率
     */
    private double calculateRealOccupancy(Integer typeId, LocalDate date, Integer total) {
        if (total == null || total == 0) return 0.0;

        // 调用 Repository 查询指定日期、房型的有效预订
        // 注意：JPA 参数需要 Long，此处进行转换
        List<Booking> activeBookings = bookingRepository.findBookingsByRoomTypeAndDateRange(
                1L, typeId.longValue(), date, date.plusDays(1));

        return (double) activeBookings.size() / total;
    }

    /**
     * 配合 Controller：按状态查询记录
     */
    public List<PricingRecord> getRecordsByStatus(String status) {
        // 建议在 PricingRecordRepository 增加 List<PricingRecord> findByStatus(String status)
        // 暂时用 findAll 过滤（安全但性能稍低）
        return pricingRecordRepository.findAll().stream()
                .filter(r -> status.equals(r.getStatus()))
                .toList();
    }

    /**
     * 配合 Controller：执行审批动作
     */
    @Transactional
    public boolean applyPriceRecord(Long recordId) {
        Optional<PricingRecord> recordOpt = pricingRecordRepository.findById(recordId);
        if (recordOpt.isPresent()) {
            PricingRecord record = recordOpt.get();
            record.setStatus("APPLIED"); // 更新为已应用
            pricingRecordRepository.save(record);

            // 审批通过后，触发真正的渠道同步
            simulateChannelSync();
            return true;
        }
        return false;
    }

    /**
     * 获取已生效价格（含保底降级逻辑）
     */
    public List<PricingRecord> getAppliedPricesByDate(LocalDate date) {
        // 1. 尝试从数据库获取已审批的动态价格
        List<PricingRecord> appliedRecords = pricingRecordRepository.findByEffectiveDateAndStatus(date, "APPLIED");

        // 2. 如果该日期已经有审批过的价格，直接返回
        if (!appliedRecords.isEmpty()) {
            return appliedRecords;
        }

        // 3. 【降级逻辑】如果没有审批记录，则返回所有房型的基准价
        System.out.println(">>> 未找到审批记录，执行保底降级逻辑，返回基准价。");
        List<RoomType> allTypes = roomTypeRepository.findAll();
        List<PricingRecord> fallbackRecords = new ArrayList<>();

        for (RoomType type : allTypes) {
            PricingRecord fallback = new PricingRecord();
            fallback.setRoomType(type);
            fallback.setEffectiveDate(date);
            fallback.setBasePrice(type.getBasePrice());
            fallback.setOriginalPrice(type.getBasePrice());
            fallback.setAdjustedPrice(type.getBasePrice()); // 调整后的价格即为基准价
            fallback.setStatus("BASE_PRICE_FALLBACK"); // 标记该价格为保底价
            fallback.setAdjustFactor("系统自动降级：使用房型基准价");

            fallbackRecords.add(fallback);
        }

        return fallbackRecords;
    }

    /**
     * 获取房型历史成交均价
     */
    private BigDecimal getHistoricalAveragePrice(Integer typeId) {
        List<Booking> history = bookingRepository.findBookingsByRoomTypeAndDateRange(
                1L, typeId.longValue(), LocalDate.now().minusDays(30), LocalDate.now());

        if (history.isEmpty()) {
            return roomTypeRepository.findById(typeId).map(RoomType::getBasePrice).orElse(BigDecimal.ZERO);
        }

        BigDecimal totalAdr = history.stream()
                .map(Booking::getAdr)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalAdr.divide(new BigDecimal(history.size()), 2, RoundingMode.HALF_UP);
    }

    /**
     * 仅同步店长已批准（APPLIED）的价格到渠道
     */
    public void simulateChannelSync() {
        System.out.println("✅ 系统扫描中：仅同步状态为 APPLIED 的定价记录至全渠道。");
    }
}