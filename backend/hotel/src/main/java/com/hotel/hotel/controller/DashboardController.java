package com.hotel.hotel.controller;

import com.hotel.hotel.common.Response;
import com.hotel.hotel.dto.DashboardStatistics;
import com.hotel.hotel.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 运营仪表盘控制器
 * 为酒店管理者提供全面的运营数据分析和监控
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "运营仪表盘", description = "为酒店管理者提供运营数据分析和监控功能")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * 获取仪表盘统计数据
     * 包含：任务概览、部门绩效、请求趋势、AI统计、告警信息
     */
    @Operation(summary = "获取仪表盘统计", description = "获取全面的运营统计数据，支持今日/本周/本月范围查询")
    @GetMapping("/statistics")
    public ResponseEntity<Response<DashboardStatistics>> getDashboardStatistics(
            @Parameter(description = "酒店ID", required = true)
            @RequestParam Long hotelId,
            @Parameter(description = "时间范围：today/week/month", required = false)
            @RequestParam(defaultValue = "today") String timeRange) {

        try {
            log.info("获取仪表盘统计: hotelId={}, timeRange={}", hotelId, timeRange);

            DashboardStatistics statistics = dashboardService.getDashboardStatistics(hotelId, timeRange);

            return ResponseEntity.ok(Response.success(statistics));
        } catch (Exception e) {
            log.error("获取仪表盘统计失败", e);
            return ResponseEntity.internalServerError()
                    .body(Response.error("获取统计数据失败: " + e.getMessage()));
        }
    }

    /**
     * 获取管理者监控视图
     * 包含：需要关注的任务、部门负载、VIP请求、待处理投诉、即将超期任务
     */
    @Operation(summary = "获取管理者监控视图", description = "获取实时任务监控信息，帮助管理者快速了解当前运营状态")
    @GetMapping("/monitor")
    public ResponseEntity<Response<DashboardStatistics.ManagerMonitorView>> getManagerMonitorView(
            @Parameter(description = "酒店ID", required = true)
            @RequestParam Long hotelId) {

        try {
            log.info("获取管理者监控视图: hotelId={}", hotelId);

            DashboardStatistics.ManagerMonitorView monitorView = dashboardService.getManagerMonitorView(hotelId);

            return ResponseEntity.ok(Response.success(monitorView));
        } catch (Exception e) {
            log.error("获取管理者监控视图失败", e);
            return ResponseEntity.internalServerError()
                    .body(Response.error("获取监控视图失败: " + e.getMessage()));
        }
    }

    /**
     * 获取各部门绩效对比
     */
    @Operation(summary = "获取部门绩效对比", description = "获取各部门的绩效数据对比，支持时间范围筛选")
    @GetMapping("/department-performance")
    public ResponseEntity<Response<List<DashboardStatistics.DepartmentPerformance>>> getDepartmentPerformance(
            @Parameter(description = "酒店ID", required = true)
            @RequestParam Long hotelId,
            @Parameter(description = "时间范围：today/week/month", required = false)
            @RequestParam(defaultValue = "today") String timeRange) {

        try {
            log.info("获取部门绩效对比: hotelId={}, timeRange={}", hotelId, timeRange);

            List<DashboardStatistics.DepartmentPerformance> performanceList =
                    dashboardService.getDepartmentPerformanceComparison(hotelId, timeRange);

            return ResponseEntity.ok(Response.success(performanceList));
        } catch (Exception e) {
            log.error("获取部门绩效对比失败", e);
            return ResponseEntity.internalServerError()
                    .body(Response.error("获取部门绩效失败: " + e.getMessage()));
        }
    }

    /**
     * 快速概览 - 返回关键指标
     */
    @Operation(summary = "快速概览", description = "返回关键运营指标，用于首页展示")
    @GetMapping("/overview")
    public ResponseEntity<Response<Map<String, Object>>> getQuickOverview(
            @Parameter(description = "酒店ID", required = true)
            @RequestParam Long hotelId) {

        try {
            DashboardStatistics statistics = dashboardService.getDashboardStatistics(hotelId, "today");

            Map<String, Object> overview = Map.of(
                    "totalTasks", statistics.getTaskOverview().getTotalTasks(),
                    "pendingTasks", statistics.getTaskOverview().getPendingTasks(),
                    "completedTasks", statistics.getTaskOverview().getCompletedTasks(),
                    "overdueTasks", statistics.getTaskOverview().getOverdueTasks(),
                    "averageCompletionTime", statistics.getTaskOverview().getAverageCompletionTime(),
                    "completionRate", statistics.getTaskOverview().getCompletionRate(),
                    "alertCount", statistics.getAlerts().size(),
                    "highAlertCount", statistics.getAlerts().stream()
                            .filter(a -> "高".equals(a.getAlertLevel()))
                            .count()
            );

            return ResponseEntity.ok(Response.success(overview));
        } catch (Exception e) {
            log.error("获取快速概览失败", e);
            return ResponseEntity.internalServerError()
                    .body(Response.error("获取概览失败: " + e.getMessage()));
        }
    }

    /**
     * 获取告警信息
     */
    @Operation(summary = "获取告警信息", description = "获取当前所有告警信息")
    @GetMapping("/alerts")
    public ResponseEntity<Response<List<DashboardStatistics.AlertInfo>>> getAlerts(
            @Parameter(description = "酒店ID", required = true)
            @RequestParam Long hotelId) {

        try {
            DashboardStatistics statistics = dashboardService.getDashboardStatistics(hotelId, "today");

            return ResponseEntity.ok(Response.success(statistics.getAlerts()));
        } catch (Exception e) {
            log.error("获取告警信息失败", e);
            return ResponseEntity.internalServerError()
                    .body(Response.error("获取告警失败: " + e.getMessage()));
        }
    }

    /**
     * 获取请求趋势数据
     */
    @Operation(summary = "获取请求趋势", description = "获取客户请求趋势分析数据")
    @GetMapping("/request-trend")
    public ResponseEntity<Response<DashboardStatistics.RequestTrend>> getRequestTrend(
            @Parameter(description = "酒店ID", required = true)
            @RequestParam Long hotelId,
            @Parameter(description = "时间范围：today/week/month", required = false)
            @RequestParam(defaultValue = "week") String timeRange) {

        try {
            DashboardStatistics statistics = dashboardService.getDashboardStatistics(hotelId, timeRange);

            return ResponseEntity.ok(Response.success(statistics.getRequestTrend()));
        } catch (Exception e) {
            log.error("获取请求趋势失败", e);
            return ResponseEntity.internalServerError()
                    .body(Response.error("获取趋势失败: " + e.getMessage()));
        }
    }

    /**
     * 获取AI解析统计
     */
    @Operation(summary = "获取AI解析统计", description = "获取AI解析效果统计数据")
    @GetMapping("/ai-statistics")
    public ResponseEntity<Response<DashboardStatistics.AIStatistics>> getAIStatistics(
            @Parameter(description = "酒店ID", required = true)
            @RequestParam Long hotelId,
            @Parameter(description = "时间范围：today/week/month", required = false)
            @RequestParam(defaultValue = "week") String timeRange) {

        try {
            DashboardStatistics statistics = dashboardService.getDashboardStatistics(hotelId, timeRange);

            return ResponseEntity.ok(Response.success(statistics.getAiStatistics()));
        } catch (Exception e) {
            log.error("获取AI统计失败", e);
            return ResponseEntity.internalServerError()
                    .body(Response.error("获取AI统计失败: " + e.getMessage()));
        }
    }
}
