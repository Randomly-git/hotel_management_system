package com.hotel.hotel.scheduler;

import com.hotel.hotel.service.PerformanceCalculationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PerformanceCalculationScheduler {

    private final PerformanceCalculationService performanceService;
    private final String defaultHotelId;

    public PerformanceCalculationScheduler(
            PerformanceCalculationService performanceService,
            @Value("${hotel.config.default-id}") String defaultHotelId) {
        this.performanceService = performanceService;
        this.defaultHotelId = defaultHotelId;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void runDailyCalculation() {
        log.info("开始执行酒店 [{}] 的每日绩效计算任务...", defaultHotelId);
        try {
            performanceService.calculateDailyPerformance(defaultHotelId);
            log.info("每日绩效计算任务执行成功。");
        } catch (Exception e) {
            log.error("每日绩效计算任务执行失败: {}", e.getMessage(), e);
        }
    }
}