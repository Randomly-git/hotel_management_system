package com.hotel.hotel.service;

import com.hotel.hotel.entity.CompetitorPrice;
import com.hotel.hotel.entity.PricingRecord;
import com.hotel.hotel.entity.RoomType;
import com.hotel.hotel.repository.CompetitorPriceRepository;
import com.hotel.hotel.repository.PricingRecordRepository;
import com.hotel.hotel.repository.RoomTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PricingService {

    private final RoomTypeRepository roomTypeRepository;
    private final PricingRecordRepository pricingRecordRepository;
    private final CompetitorPriceRepository competitorPriceRepository;

    private final Random random = new Random();

    @Autowired
    public PricingService(RoomTypeRepository roomTypeRepository,
                          PricingRecordRepository pricingRecordRepository,
                          CompetitorPriceRepository competitorPriceRepository) {
        this.roomTypeRepository = roomTypeRepository;
        this.pricingRecordRepository = pricingRecordRepository;
        this.competitorPriceRepository = competitorPriceRepository;
    }

    /**
     * 任务 A: F3.1 模拟竞品价格抓取
     */
    @Transactional
    public void mockCrawlCompetitorPrices() {
        String[] competitors = {"精品酒店-悦季", "商务酒店-全季", "高端公寓-嘉里"};
        List<RoomType> roomTypes = roomTypeRepository.findAll();
        LocalDate today = LocalDate.now();
        List<CompetitorPrice> mockData = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
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
        System.out.println(">>> F3.1 模拟抓取完成，存入记录数: " + mockData.size());
    }

    /**
     * 任务 B: F3.2 智能调价核心逻辑
     */
    @Transactional
    public void calculateAndAdjustPricesForFutureWeek() {
        LocalDate today = LocalDate.now();
        List<RoomType> allRoomTypes = roomTypeRepository.findAll();

        for (RoomType roomType : allRoomTypes) {
            for (int i = 0; i < 7; i++) {
                LocalDate targetDate = today.plusDays(i);
                adjustPrice(roomType, targetDate);
            }
        }
        simulateChannelSync();
    }

    @Transactional
    public PricingRecord adjustPrice(RoomType roomType, LocalDate targetDate) {
        BigDecimal basePrice = roomType.getBasePrice();

        BigDecimal seasonalFactor = calculateSeasonalFactor(targetDate);
        BigDecimal competitorFactor = calculateCompetitorFactor(roomType, targetDate);
        BigDecimal occupancyFactor = BigDecimal.valueOf(0.1 + (0.8 * random.nextDouble())).setScale(2, RoundingMode.HALF_UP);

        BigDecimal calculatedPrice = basePrice
                .multiply(seasonalFactor)
                .multiply(competitorFactor);

        if (occupancyFactor.compareTo(new BigDecimal("0.30")) < 0) {
            calculatedPrice = calculatedPrice.multiply(new BigDecimal("0.90"));
        } else if (occupancyFactor.compareTo(new BigDecimal("0.80")) > 0) {
            calculatedPrice = calculatedPrice.multiply(new BigDecimal("1.20"));
        }

        BigDecimal adjustedPrice = calculatedPrice.setScale(2, RoundingMode.HALF_UP);

        // 核心需求：偏离 > 50% 触发审批
        BigDecimal deviation = adjustedPrice.subtract(basePrice).abs()
                .divide(basePrice, 4, RoundingMode.HALF_UP);

        String status = "APPROVED";
        if (deviation.compareTo(new BigDecimal("0.50")) > 0) {
            status = "PENDING";
            System.out.println("⚠️ 警报：房型 [" + roomType.getTypeName() + "] 偏离度 " + deviation + "，转入人工审批。");
        }

        PricingRecord record = new PricingRecord();
        record.setRoomType(roomType);
        record.setBasePrice(basePrice);
        record.setOriginalPrice(basePrice);
        record.setAdjustedPrice(adjustedPrice);
        record.setStatus(status);
        record.setEffectiveDate(targetDate);
        record.setAdjustFactor(String.format("季节:%.1f, 竞品:%.2f, 预订:%.2f",
                seasonalFactor, competitorFactor, occupancyFactor));

        return pricingRecordRepository.save(record);
    }

    /**
     * F3.4 新增方法：获取待审核价格列表
     */
    public List<PricingRecord> getPendingPrices() {
        // 调用 Repository 中之前定义的 findByStatus 方法
        return pricingRecordRepository.findByStatus("PENDING");
    }

    /**
     * F3.4 新增方法：审批通过价格
     */
    @Transactional
    public void approvePrice(Long recordId) {
        PricingRecord record = pricingRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("错误：找不到 ID 为 " + recordId + " 的价格记录"));

        record.setStatus("APPROVED");
        pricingRecordRepository.save(record);
        System.out.println("✅ 管理员已批准价格记录 ID: " + recordId);

        // 审批通过后，通常需要触发一次渠道同步
        simulateChannelSync();
    }

    private BigDecimal calculateCompetitorFactor(RoomType roomType, LocalDate date) {
        List<CompetitorPrice> comps = competitorPriceRepository.findByStayDate(date);
        if (comps.isEmpty()) return BigDecimal.ONE;

        BigDecimal avg = comps.stream()
                .map(CompetitorPrice::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(comps.size()), RoundingMode.HALF_UP);

        if (avg.compareTo(roomType.getBasePrice().multiply(new BigDecimal("1.1"))) > 0) {
            return new BigDecimal("1.05");
        }
        return BigDecimal.ONE;
    }

    private BigDecimal calculateSeasonalFactor(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) return new BigDecimal("1.2");
        if (date.getMonthValue() == 5 || date.getMonthValue() == 10) return new BigDecimal("1.5");
        return BigDecimal.ONE;
    }

    public void simulateChannelSync() {
        System.out.println("✅ 定价同步操作已触发。");
    }

    public List<PricingRecord> getPricesByEffectiveDate(LocalDate date) {
        // 仅返回已批准的价格给预订系统使用
        return pricingRecordRepository.findByEffectiveDateAndStatus(date, "APPROVED");
    }

    public List<CompetitorPrice> getAllCompetitorPrices() {
        return competitorPriceRepository.findAll();
    }
}