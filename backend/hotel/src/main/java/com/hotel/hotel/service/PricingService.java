package com.hotel.hotel.service;

import com.hotel.hotel.entity.RoomType;
import com.hotel.hotel.entity.PricingRecord;
import com.hotel.hotel.repository.RoomTypeRepository;
import com.hotel.hotel.repository.PricingRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@Service
public class PricingService {

    private final RoomTypeRepository roomTypeRepository;
    private final PricingRecordRepository pricingRecordRepository;

    @Autowired
    public PricingService(RoomTypeRepository roomTypeRepository, PricingRecordRepository pricingRecordRepository) {
        this.roomTypeRepository = roomTypeRepository;
        this.pricingRecordRepository = pricingRecordRepository;
    }

    /**
     * 【核心任务】每日定时调价，计算未来 N 天的价格
     * 假设我们每天调整未来 7 天的价格
     */
    @Transactional
    public void calculateAndAdjustPricesForFutureWeek() {
        LocalDate today = LocalDate.now();
        List<RoomType> allRoomTypes = roomTypeRepository.findAll();

        for (RoomType roomType : allRoomTypes) {
            for (int i = 0; i < 7; i++) {
                LocalDate targetDate = today.plusDays(i);

                // 检查该日期是否已存在调价记录，如果有，则跳过或进行覆盖更新（这里选择跳过，确保幂等性）
                Optional<PricingRecord> existingRecord = pricingRecordRepository.findTopByRoomTypeTypeIdAndEffectiveDateOrderByAdjustTimeDesc(
                        roomType.getTypeId(), targetDate
                );

                if (existingRecord.isPresent()) {
                    System.out.println(String.format("房型 [%s] 生效日期 [%s] 已存在调价记录，跳过。",
                            roomType.getTypeName(), targetDate));
                    continue;
                }

                // 执行动态调价
                adjustPrice(roomType, targetDate);
            }
        }

        // 模拟执行全渠道同步
        simulateChannelSync();
    }

    /**
     * 根据多因子模型对特定房型在特定生效日期进行调价
     * * @param roomType 房型实体
     * @param targetDate 价格生效日期
     * @return 调整后的价格记录
     */
    @Transactional
    public PricingRecord adjustPrice(RoomType roomType, LocalDate targetDate) {

        // 1. 获取当前价格（以基础价格为起点，或以上一次记录价格为起点）
        BigDecimal currentPrice = roomType.getBasePrice();

        // 2. 收集多因子数据 (模拟)
        Map<String, BigDecimal> factors = collectDynamicFactors(roomType, targetDate);

        // 3. 执行多因子智能定价模型 (公式模拟)

        // 价格起点：基础价格 * 季节/周策略系数
        BigDecimal baseFactor = factors.get("SEASONAL_FACTOR");
        BigDecimal calculatedPrice = currentPrice.multiply(baseFactor);

        // 竞争对手调整：如果竞争对手价格高，则价格调高 (+5% 调整)
        if (factors.get("COMPETITOR_HIGH").compareTo(BigDecimal.ONE) > 0) {
            calculatedPrice = calculatedPrice.multiply(new BigDecimal("1.05"));
        }

        // 预订量调整：如果预订量低，则降价 (-10% 调整)
        if (factors.get("OCCUPANCY_RATE").compareTo(new BigDecimal("0.30")) < 0) {
            calculatedPrice = calculatedPrice.multiply(new BigDecimal("0.90"));
        }

        // 4. 确保价格调整在合理范围内 (防止价格过低或过高)
        BigDecimal maxPrice = roomType.getBasePrice().multiply(new BigDecimal("1.8"));
        BigDecimal minPrice = roomType.getBasePrice().multiply(new BigDecimal("0.7"));

        BigDecimal adjustedPrice = calculatedPrice.setScale(2, RoundingMode.HALF_UP);

        // 价格截断
        adjustedPrice = adjustedPrice.min(maxPrice).max(minPrice);

        // 5. 创建并保存调价记录
        PricingRecord record = new PricingRecord();
        record.setRoomType(roomType);
        record.setOriginalPrice(currentPrice); // 记录调整前的基础价格
        record.setAdjustedPrice(adjustedPrice);
        record.setAdjustFactor(String.format("周策略系数: %.2f, 竞争对手: %s, 预订率: %.2f",
                baseFactor, factors.get("COMPETITOR_HIGH").equals(BigDecimal.ONE) ? "否" : "是", factors.get("OCCUPANCY_RATE")));
        record.setEffectiveDate(targetDate);

        PricingRecord savedRecord = pricingRecordRepository.save(record);

        System.out.println(String.format("房型 [%s] 生效日期 [%s] 价格从 %.2f 调整至 %.2f",
                roomType.getTypeName(), targetDate, currentPrice, adjustedPrice));

        return savedRecord;
    }

    /**
     * 辅助方法：模拟收集动态定价所需的多因子数据
     */
    private Map<String, BigDecimal> collectDynamicFactors(RoomType roomType, LocalDate targetDate) {
        Map<String, BigDecimal> factors = new HashMap<>();
        DayOfWeek dayOfWeek = targetDate.getDayOfWeek();
        Random rand = new Random();

        // 1. 季节/周策略因子 (基于需求文档策略细化)
        BigDecimal seasonalFactor;
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            seasonalFactor = new BigDecimal("1.2"); // 周末价格：基础价×1.2
        } else if (targetDate.getMonthValue() == 5 || targetDate.getMonthValue() == 10) {
            seasonalFactor = new BigDecimal("1.5"); // 模拟节假日价格
        } else {
            seasonalFactor = new BigDecimal("1.0"); // 平日价格：基础价×1.0
        }
        factors.put("SEASONAL_FACTOR", seasonalFactor);

        // 2. 竞争对手价格模拟 (模拟：随机决定竞争对手是否涨价)
        factors.put("COMPETITOR_HIGH", rand.nextBoolean() ? new BigDecimal("1.1") : BigDecimal.ONE);

        // 3. 预订率模拟 (模拟：随机生成 10% 到 90% 之间的预订率)
        double occupancy = 0.1 + (0.8 * rand.nextDouble());
        factors.put("OCCUPANCY_RATE", BigDecimal.valueOf(occupancy).setScale(2, RoundingMode.HALF_UP));

        // ... 实际应用中还会包括：天气、大型活动、客户画像数据等

        return factors;
    }

    /**
     * 辅助方法：模拟执行全渠道价格同步
     */
    public void simulateChannelSync() {
        System.out.println("------------------------------------");
        System.out.println("✅ 动态定价结果已保存，开始模拟全渠道价格同步...");
        // 实际中这里会调用 OTA API 或 PMS (Property Management System) 接口
        System.out.println("✅ 价格同步完成：OTA, 官网, 预订系统价格已更新。");
        System.out.println("------------------------------------");
    }

    /**
     * 提供给外部查询某个生效日期的最新价格
     */
    public List<PricingRecord> getPricesByEffectiveDate(LocalDate effectiveDate) {
        // 实际查询应该返回特定生效日期的所有房型价格，这里我们简化为查询所有记录
        // ⚠️ 理想的 Repository 方法：List<PricingRecord> findByEffectiveDate(LocalDate date);

        // 简单实现：查询所有记录并筛选
        return pricingRecordRepository.findAll().stream()
                .filter(record -> record.getEffectiveDate().isEqual(effectiveDate))
                .toList();
    }
}