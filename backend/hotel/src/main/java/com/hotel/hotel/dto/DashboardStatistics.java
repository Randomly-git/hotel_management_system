package com.hotel.hotel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 运营仪表盘统计数据DTO
 * 为酒店管理者提供全面的运营数据视图
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "运营仪表盘统计数据")
public class DashboardStatistics {

    @Schema(description = "数据统计时间范围")
    private TimeRange timeRange;

    @Schema(description = "任务统计概览")
    private TaskOverview taskOverview;

    @Schema(description = "各部门绩效数据")
    private List<DepartmentPerformance> departmentPerformance;

    @Schema(description = "客户请求趋势数据")
    private RequestTrend requestTrend;

    @Schema(description = "AI解析统计")
    private AIStatistics aiStatistics;

    @Schema(description = "告警信息")
    private List<AlertInfo> alerts;

    /**
     * 时间范围
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeRange {
        @Schema(description = "开始时间")
        private LocalDateTime startTime;

        @Schema(description = "结束时间")
        private LocalDateTime endTime;

        @Schema(description = "描述（今日/本周/本月）")
        private String description;
    }

    /**
     * 任务统计概览
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskOverview {
        @Schema(description = "总任务数")
        private Long totalTasks;

        @Schema(description = "待处理任务数")
        private Long pendingTasks;

        @Schema(description = "进行中任务数")
        private Long inProgressTasks;

        @Schema(description = "已完成任务数")
        private Long completedTasks;

        @Schema(description = "已取消任务数")
        private Long canceledTasks;

        @Schema(description = "超期任务数")
        private Long overdueTasks;

        @Schema(description = "平均完成时间（分钟）")
        private Double averageCompletionTime;

        @Schema(description = "任务完成率")
        private BigDecimal completionRate;

        @Schema(description = "任务取消率")
        private BigDecimal cancellationRate;
    }

    /**
     * 部门绩效数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentPerformance {
        @Schema(description = "部门ID")
        private Long departmentId;

        @Schema(description = "部门名称")
        private String departmentName;

        @Schema(description = "总任务数")
        private Long totalTasks;

        @Schema(description = "待处理任务数")
        private Long pendingTasks;

        @Schema(description = "已完成任务数")
        private Long completedTasks;

        @Schema(description = "平均响应时间（分钟）")
        private Double averageResponseTime;

        @Schema(description = "平均完成时间（分钟）")
        private Double averageCompletionTime;

        @Schema(description = "平均满意度评分")
        private BigDecimal averageRating;

        @Schema(description = "推荐率")
        private BigDecimal recommendationRate;

        @Schema(description = "绩效等级（优秀/良好/一般/需改进）")
        private String performanceLevel;
    }

    /**
     * 客户请求趋势
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestTrend {
        @Schema(description = "总请求数")
        private Long totalRequests;

        @Schema(description = "与上期对比增长率")
        private BigDecimal growthRate;

        @Schema(description = "按任务类型分布")
        private Map<String, Long> taskTypeDistribution;

        @Schema(description = "按优先级分布")
        private Map<String, Long> priorityDistribution;

        @Schema(description = "按时段分布（24小时）")
        private List<HourlyData> hourlyDistribution;

        @Schema(description = "按日期分布")
        private List<DailyData> dailyDistribution;
    }

    /**
     * AI解析统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AIStatistics {
        @Schema(description = "总解析次数")
        private Long totalParsed;

        @Schema(description = "成功解析次数")
        private Long successfulParsed;

        @Schema(description = "解析失败次数")
        private Long failedParsed;

        @Schema(description = "解析成功率")
        private BigDecimal successRate;

        @Schema(description = "需要人工介入次数")
        private Long manualInterventionCount;

        @Schema(description = "人工介入率")
        private BigDecimal manualInterventionRate;

        @Schema(description = "按意图分类统计")
        private Map<String, Long> intentDistribution;

        @Schema(description = "按部门分类统计")
        private Map<String, Long> departmentDistribution;
    }

    /**
     * 告警信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertInfo {
        @Schema(description = "告警类型（任务超期/部门负载过高/客户投诉等）")
        private String alertType;

        @Schema(description = "告警级别（高/中/低）")
        private String alertLevel;

        @Schema(description = "告警内容")
        private String message;

        @Schema(description = "关联部门")
        private String department;

        @Schema(description = "告警时间")
        private LocalDateTime alertTime;

        @Schema(description = "待处理任务数")
        private Long pendingCount;
    }

    /**
     * 小时数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HourlyData {
        @Schema(description = "小时（0-23）")
        private Integer hour;

        @Schema(description = "请求数量")
        private Long count;
    }

    /**
     * 日数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyData {
        @Schema(description = "日期")
        private String date;

        @Schema(description = "请求数量")
        private Long count;

        @Schema(description = "完成数量")
        private Long completedCount;
    }

    /**
     * 管理者任务监控视图
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "管理者任务监控视图")
    public static class ManagerMonitorView {
        @Schema(description = "需要关注的任务列表")
        private List<AttentionTask> attentionTasks;

        @Schema(description = "各部门当前负载")
        private List<DepartmentLoad> departmentLoads;

        @Schema(description = "今日VIP客户请求")
        private List<VIPRequest> vipRequests;

        @Schema(description = "待处理投诉")
        private Long pendingComplaintsCount;

        @Schema(description = "即将超期任务数")
        private Long soonOverdueCount;
    }

    /**
     * 需要关注的任务
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttentionTask {
        @Schema(description = "任务ID")
        private Long taskId;

        @Schema(description = "任务内容")
        private String taskContent;

        @Schema(description = "客户ID")
        private String customerId;

        @Schema(description = "客户姓名")
        private String customerName;

        @Schema(description = "房间号")
        private String roomNumber;

        @Schema(description = "责任部门")
        private String departmentName;

        @Schema(description = "任务状态")
        private String status;

        @Schema(description = "优先级")
        private String priority;

        @Schema(description = "创建时间")
        private LocalDateTime createTime;

        @Schema(description = "等待时长（分钟）")
        private Long waitingMinutes;

        @Schema(description = "关注原因（超期/高优先级/VIP客户/投诉等）")
        private String attentionReason;
    }

    /**
     * 部门负载
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentLoad {
        @Schema(description = "部门ID")
        private Long departmentId;

        @Schema(description = "部门名称")
        private String departmentName;

        @Schema(description = "待处理任务数")
        private Long pendingCount;

        @Schema(description = "进行中任务数")
        private Long inProgressCount;

        @Schema(description = "总任务数")
        private Long totalCount;

        @Schema(description = "负载状态（正常/繁忙/过载）")
        private String loadStatus;

        @Schema(description = "平均处理时间（分钟）")
        private Double avgProcessTime;
    }

    /**
     * VIP客户请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VIPRequest {
        @Schema(description = "任务ID")
        private Long taskId;

        @Schema(description = "客户姓名")
        private String customerName;

        @Schema(description = "房间号")
        private String roomNumber;

        @Schema(description = "请求内容")
        private String requestContent;

        @Schema(description = "请求时间")
        private LocalDateTime requestTime;

        @Schema(description = "当前状态")
        private String status;
    }
}
