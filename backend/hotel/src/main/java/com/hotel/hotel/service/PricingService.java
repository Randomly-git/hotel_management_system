package com.hotel.hotel.service;

import com.hotel.hotel.entity.Booking;
import com.hotel.hotel.entity.CompetitorPrice;
import com.hotel.hotel.entity.PricingRecord;
import com.hotel.hotel.entity.RoomType;
import com.hotel.hotel.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class PricingService {

    private final RoomTypeRepository roomTypeRepository;
    private final PricingRecordRepository pricingRecordRepository;
    private final CompetitorPriceRepository competitorPriceRepository;
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;

    private final Random random = new Random();

    @Autowired
    public PricingService(RoomTypeRepository roomTypeRepository,
                          PricingRecordRepository pricingRecordRepository,
                          CompetitorPriceRepository competitorPriceRepository,
                          BookingRepository bookingRepository,
                          RoomRepository roomRepository) {
        this.roomTypeRepository = roomTypeRepository;
        this.pricingRecordRepository = pricingRecordRepository;
        this.competitorPriceRepository = competitorPriceRepository;
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
    }

    /**
     * 核心功能：供 BookingService 调用。
     * 逻辑：优先找审批通过的动态价，若无（如远期预订）则回退使用该房型的基础挂牌价。
     */
    public BigDecimal getEffectivePrice(RoomType roomType, Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // 仅查询状态为 APPROVED 的价格记录
        List<PricingRecord> records = pricingRecordRepository.findByEffectiveDateAndStatus(localDate, "APPROVED");

        return records.stream()
                .filter(r -> r.getRoomType().getTypeId().equals(roomType.getTypeId()))
                .findFirst()
                .map(PricingRecord::getAdjustedPrice)
                .orElse(roomType.getBasePrice()); // 【保底逻辑】核心点：远期预订自动回退到基础价
    }

    @Transactional
    public PricingRecord adjustPrice(RoomType roomType, LocalDate targetDate) {
        BigDecimal basePrice = roomType.getBasePrice();

        BigDecimal seasonalFactor = calculateSeasonalFactor(targetDate);
        BigDecimal competitorFactor = calculateCompetitorFactor(roomType, targetDate);
        BigDecimal occupancyFactor = calculateRealOccupancyFactor(roomType, targetDate);

        BigDecimal calculatedPrice = basePrice
                .multiply(seasonalFactor)
                .multiply(competitorFactor)
                .multiply(occupancyFactor);

        BigDecimal adjustedPrice = calculatedPrice.setScale(2, RoundingMode.HALF_UP);

        // 偏离 > 50% 触发审批
        BigDecimal deviation = adjustedPrice.subtract(basePrice).abs()
                .divide(basePrice, 4, RoundingMode.HALF_UP);

        String status = "APPROVED";
        if (deviation.compareTo(new BigDecimal("0.50")) > 0) {
            status = "PENDING";
            System.out.println("⚠️ 警报：房型 [" + roomType.getTypeName() + "] 偏离度 " + deviation + "，转入人工审批。");
        }

        // 优化点：检查是否已存在当天的记录，若存在则更新，不存在则新建
        List<PricingRecord> existing = pricingRecordRepository.findByEffectiveDateAndStatus(targetDate, status);
        PricingRecord record = existing.stream()
                .filter(r -> r.getRoomType().getTypeId().equals(roomType.getTypeId()))
                .findFirst()
                .orElse(new PricingRecord());

        record.setRoomType(roomType);
        record.setBasePrice(basePrice);
        record.setOriginalPrice(basePrice);
        record.setAdjustedPrice(adjustedPrice);
        record.setStatus(status);
        record.setEffectiveDate(targetDate);
        record.setAdjustFactor(String.format("季节:%.1f, 竞品:%.2f, 占用加成:%.2f",
                seasonalFactor, competitorFactor, occupancyFactor));

        return pricingRecordRepository.save(record);
    }

    /**
     * 模拟执行竞品数据抓取 - 已延长至 30 天
     */
    public void mockCrawlCompetitorPrices() {
        String[] competitors = {"精品酒店-悦季", "商务酒店-全季", "高端公寓-嘉里"};
        List<RoomType> roomTypes = roomTypeRepository.findAll();
        LocalDate today = LocalDate.now();
        List<CompetitorPrice> mockData = new ArrayList<>();

        // 修改点：从 7 天延长至 30 天
        for (int i = 0; i < 30; i++) {
            LocalDate targetDate = today.plusDays(i);
            for (RoomType type : roomTypes) {
                for (String compName : competitors) {
                    CompetitorPrice cp = new CompetitorPrice();
                    cp.setCompetitorName(compName);
                    cp.setRoomTypeName(type.getTypeName());
                    cp.setStayDate(targetDate);
                    double fluctuation = 0.8 + (random.nextDouble() * 0.4);
                    cp.setPrice(type.getBasePrice().multiply(BigDecimal.valueOf(fluctuation)).setScale(2, RoundingMode.HALF_UP));
                    mockData.add(cp);
                }
            }
        }
        competitorPriceRepository.saveAll(mockData);
    }

    /**
     * 执行未来 30 天的动态调价计算
     */
    @Transactional
    public void calculateAndAdjustPricesForFutureWeek() {
        LocalDate today = LocalDate.now();
        List<RoomType> allRoomTypes = roomTypeRepository.findAll();
        for (RoomType roomType : allRoomTypes) {
            // 修改点：循环次数改为 30
            for (int i = 0; i < 30; i++) {
                LocalDate targetDate = today.plusDays(i);
                adjustPrice(roomType, targetDate);
            }
        }
        simulateChannelSync();
    }

    // --- 内部辅助计算方法 (保持现状) ---

    private BigDecimal calculateRealOccupancyFactor(RoomType roomType, LocalDate targetDate) {
        long totalRooms = roomRepository.countByRoomType_TypeId(roomType.getTypeId());
        if (totalRooms == 0) return BigDecimal.ONE;
        Date date = Date.from(targetDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        long occupiedCount = bookingRepository.countOccupiedRoomsByDate(roomType.getTypeId(), date);
        double occupancyRate = (double) occupiedCount / totalRooms;

        if (occupancyRate < 0.3) return new BigDecimal("0.90");
        if (occupancyRate > 0.8) return new BigDecimal("1.20");
        return BigDecimal.ONE;
    }

    private BigDecimal calculateCompetitorFactor(RoomType roomType, LocalDate date) {
        List<CompetitorPrice> comps = competitorPriceRepository.findByStayDate(date);
        if (comps.isEmpty()) return BigDecimal.ONE;
        BigDecimal avg = comps.stream()
                .map(CompetitorPrice::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(comps.size()), RoundingMode.HALF_UP);
        if (avg.compareTo(roomType.getBasePrice().multiply(new BigDecimal("1.1"))) > 0) return new BigDecimal("1.05");
        return BigDecimal.ONE;
    }

    private BigDecimal calculateSeasonalFactor(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) return new BigDecimal("1.2");
        if (date.getMonthValue() == 5 || date.getMonthValue() == 10) return new BigDecimal("1.5");
        return BigDecimal.ONE;
    }

    @Transactional
    public void approvePrice(Long recordId) {
        PricingRecord record = pricingRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("错误：找不到 ID 为 " + recordId + " 的价格记录"));
        record.setStatus("APPROVED");
        pricingRecordRepository.save(record);
        simulateChannelSync();
    }

    /**
     * 店长暴力改价逻辑
     */
    @Transactional
    public void manualUpdatePrice(Long recordId, BigDecimal newPrice, String reason) {
        PricingRecord record = pricingRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("错误：找不到 ID 为 " + recordId + " 的价格记录"));

        record.setAdjustedPrice(newPrice);
        record.setStatus("APPROVED"); // 强制生效
        // 在因子说明中记录这是手动修改的，方便日后查账
        record.setAdjustFactor(record.getAdjustFactor() + " | [店长干预]: " + reason);

        pricingRecordRepository.save(record);
        System.out.println("⚠️ 警告：价格记录 " + recordId + " 已由店长手动修改为 " + newPrice);
    }

    public void simulateChannelSync() { System.out.println("✅ 定价同步操作已触发。"); }
    public List<PricingRecord> getPendingPrices() { return pricingRecordRepository.findByStatus("PENDING"); }
    public List<PricingRecord> getPricesByEffectiveDate(LocalDate date) { return pricingRecordRepository.findByEffectiveDateAndStatus(date, "APPROVED"); }
    public List<CompetitorPrice> getAllCompetitorPrices() { return competitorPriceRepository.findAll(); }
}