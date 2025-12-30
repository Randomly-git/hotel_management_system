package com.hotel.hotel.controller;

import com.hotel.hotel.entity.DepartmentPerformance;
import com.hotel.hotel.repository.DepartmentPerformanceRepository;
import com.hotel.hotel.service.PerformanceCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/performance")
@Tag(name = "绩效与建议管理", description = "负责部门绩效计算、趋势查询及 AI 建议生成")
public class PerformanceController {

    private final DepartmentPerformanceRepository performanceRepository;
    private final PerformanceCalculationService calculationService;

    @Autowired
    public PerformanceController(DepartmentPerformanceRepository performanceRepository,
                                 PerformanceCalculationService calculationService) {
        this.performanceRepository = performanceRepository;
        this.calculationService = calculationService;
    }

    /**
     * [POST] 触发绩效计算
     * 可选的 date 参数，如果不传则默认为今天
     */
    @PostMapping("/calculate")
    @Operation(summary = "触发绩效计算", description = "计算指定日期的部门绩效，汇总评价并生成 AI 改进建议")
    public ResponseEntity<String> calculatePerformance(
            @RequestParam String hotelId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        // 如果没有提供日期，则默认为今天
        LocalDate targetDate = (date == null) ? LocalDate.now() : date;

        log.info("手动触发酒店 [{}] 在日期 [{}] 的绩效计算", hotelId, targetDate);

        // 调用 Service 进行计算 (注意：需要修改 Service 方法签名接收 targetDate)
        calculationService.calculateDailyPerformance(hotelId, targetDate);

        return ResponseEntity.ok("日期 [" + targetDate + "] 的绩效计算已完成");
    }

    /**
     * [GET] 查询绩效历史
     * 直接使用 @RequestParam 接收参数，避免关联 PerformanceQueryDto 报错
     */
    @GetMapping("/history")
    @Operation(summary = "查询历史绩效", description = "获取指定日期范围内所有部门的绩效记录")
    public ResponseEntity<List<DepartmentPerformance>> getHistory(
            @RequestParam String hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (endDate == null) endDate = LocalDate.now();
        if (startDate == null) startDate = endDate.minusDays(7);

        log.info("查询酒店 [{}] 从 {} 到 {} 的绩效历史", hotelId, startDate, endDate);

        // 调用 Repository 中已有的范围查询方法，支持按酒店ID过滤
        List<DepartmentPerformance> records = performanceRepository
                .findByHotelIdAndStatisticsDateBetweenOrderByStatisticsDateAsc(hotelId, startDate, endDate);


        if (records.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(records);
    }

    /**
     * [GET] 获取特定部门今日结果
     * 使用 Repository 中的 findByHotelIdAndDepartmentDeptIdAndStatisticsDate
     */
    @GetMapping("/today")
    @Operation(summary = "查看部门今日得分", description = "精准查询某个部门今天的评分和建议内容")
    public ResponseEntity<DepartmentPerformance> getTodayResult(
            @RequestParam String hotelId,
            @RequestParam Long deptId) {

        return performanceRepository.findByHotelIdAndDepartmentDeptIdAndStatisticsDate(
                        hotelId, deptId, LocalDate.now())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}