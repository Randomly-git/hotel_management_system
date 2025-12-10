package com.hotel.hotel.controller;

import com.hotel.hotel.dto.PerformanceQueryDto;
import com.hotel.hotel.entity.DepartmentPerformance;
import com.hotel.hotel.repository.DepartmentPerformanceRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/performance")
public class PerformanceController {

    private final DepartmentPerformanceRepository performanceRepository;

    @Autowired
    public PerformanceController(DepartmentPerformanceRepository performanceRepository) {
        this.performanceRepository = performanceRepository;
    }

    /**
     * [GET] 查询指定日期范围内的部门绩效记录（用于图表展示）
     * 接口路径: /api/v1/performance/history
     *
     * @param query 包含 startDate 和 endDate 的查询参数
     * @return 绩效记录列表
     */
    @GetMapping("/history")
    public ResponseEntity<List<DepartmentPerformance>> getPerformanceHistory(@Valid PerformanceQueryDto query) {

        if (query.getStartDate().isAfter(query.getEndDate())) {
            // 简单参数校验
            return ResponseEntity.badRequest().build();
        }

        List<DepartmentPerformance> historyRecords;

        if (query.getDeptId() != null) {
            // 如果指定了部门，则查询特定部门的历史记录 (需在 Repository 中编写该查询方法)
            // historyRecords = performanceRepository.findByDepartmentDeptIdAndStatisticsDateBetweenOrderByStatisticsDateAsc(
            //     query.getDeptId(), query.getStartDate(), query.getEndDate()
            // );

            // 暂时使用全量查询，简化逻辑：
            historyRecords = performanceRepository.findByStatisticsDateBetweenOrderByStatisticsDateAsc(
                    query.getStartDate(), query.getEndDate()
            );

        } else {
            // 查询所有部门在该时间段内的记录
            historyRecords = performanceRepository.findByStatisticsDateBetweenOrderByStatisticsDateAsc(
                    query.getStartDate(), query.getEndDate()
            );
        }

        if (historyRecords.isEmpty()) {
            return ResponseEntity.noContent().build(); // 返回 204 No Content
        }

        return ResponseEntity.ok(historyRecords);
    }
}