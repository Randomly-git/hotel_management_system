package com.hotel.hotel.service;

import com.hotel.hotel.dto.DashboardStatistics;
import com.hotel.hotel.entity.Department;
import com.hotel.hotel.entity.DepartmentFeedback;
import com.hotel.hotel.entity.DepartmentTask;
import com.hotel.hotel.entity.TaskOrder;
import com.hotel.hotel.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 运营仪表盘服务
 * 为酒店管理者提供全面的运营数据分析和监控
 */
@Slf4j
@Service
public class DashboardService {

    @Autowired
    private TaskOrderRepository taskOrderRepository;

    @Autowired
    private DepartmentTaskRepository departmentTaskRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DepartmentFeedbackRepository feedbackRepository;

    /**
     * 获取仪表盘统计数据
     */
    public DashboardStatistics getDashboardStatistics(Long hotelId, String timeRange) {
        log.info("获取仪表盘统计: hotelId={}, timeRange={}", hotelId, timeRange);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = calculateStartTime(now, timeRange);

        return DashboardStatistics.builder()
                .timeRange(buildTimeRange(startTime, now, timeRange))
                .taskOverview(buildTaskOverview(hotelId, startTime, now))
                .departmentPerformance(buildDepartmentPerformance(hotelId, startTime, now))
                .requestTrend(buildRequestTrend(hotelId, startTime, now))
                .aiStatistics(buildAIStatistics(hotelId, startTime, now))
                .alerts(buildAlerts(hotelId))
                .build();
    }

    /**
     * 获取管理者监控视图
     */
    public DashboardStatistics.ManagerMonitorView getManagerMonitorView(Long hotelId) {
        log.info("获取管理者监控视图: hotelId={}", hotelId);

        return DashboardStatistics.ManagerMonitorView.builder()
                .attentionTasks(getAttentionTasks(hotelId))
                .departmentLoads(getDepartmentLoads(hotelId))
                .vipRequests(getVIPRequests(hotelId))
                .pendingComplaintsCount(getPendingComplaintsCount(hotelId))
                .soonOverdueCount(getSoonOverdueCount(hotelId))
                .build();
    }

    /**
     * 获取各部门绩效对比
     */
    public List<DashboardStatistics.DepartmentPerformance> getDepartmentPerformanceComparison(
            Long hotelId, String timeRange) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = calculateStartTime(now, timeRange);

        return buildDepartmentPerformance(hotelId, startTime, now);
    }

    // ==================== 私有方法 ====================

    private LocalDateTime calculateStartTime(LocalDateTime now, String timeRange) {
        return switch (timeRange.toLowerCase()) {
            case "today" -> now.toLocalDate().atStartOfDay();
            case "week" -> now.minus(7, ChronoUnit.DAYS);
            case "month" -> now.minus(30, ChronoUnit.DAYS);
            default -> now.toLocalDate().atStartOfDay();
        };
    }

    private DashboardStatistics.TimeRange buildTimeRange(LocalDateTime startTime, LocalDateTime endTime, String timeRange) {
        String description = switch (timeRange.toLowerCase()) {
            case "today" -> "今日";
            case "week" -> "近7天";
            case "month" -> "近30天";
            default -> "自定义";
        };

        return DashboardStatistics.TimeRange.builder()
                .startTime(startTime)
                .endTime(endTime)
                .description(description)
                .build();
    }

    /**
     * 构建任务统计概览
     */
    private DashboardStatistics.TaskOverview buildTaskOverview(Long hotelId, LocalDateTime startTime, LocalDateTime endTime) {
        List<TaskOrder> allTasks = taskOrderRepository.findByHotelIdOrderByCreateTimeDesc(hotelId);

        List<TaskOrder> periodTasks = allTasks.stream()
                .filter(t -> t.getCreateTime().isAfter(startTime) && t.getCreateTime().isBefore(endTime))
                .collect(Collectors.toList());

        long total = periodTasks.size();
        long pending = periodTasks.stream().filter(t -> "PENDING".equals(t.getStatus())).count();
        long inProgress = periodTasks.stream().filter(t -> "IN_PROGRESS".equals(t.getStatus())).count();
        long completed = periodTasks.stream().filter(t -> "COMPLETED".equals(t.getStatus())).count();
        long canceled = periodTasks.stream().filter(t -> "CANCELED".equals(t.getStatus())).count();
        long overdue = calculateOverdueTasks(periodTasks);

        // 计算平均完成时间
        Double avgCompletionTime = calculateAverageCompletionTime(hotelId, startTime, endTime);

        // 计算完成率
        BigDecimal completionRate = total > 0
                ? BigDecimal.valueOf(completed * 100.0 / total).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 计算取消率
        BigDecimal cancellationRate = total > 0
                ? BigDecimal.valueOf(canceled * 100.0 / total).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return DashboardStatistics.TaskOverview.builder()
                .totalTasks(total)
                .pendingTasks(pending)
                .inProgressTasks(inProgress)
                .completedTasks(completed)
                .canceledTasks(canceled)
                .overdueTasks(overdue)
                .averageCompletionTime(avgCompletionTime)
                .completionRate(completionRate)
                .cancellationRate(cancellationRate)
                .build();
    }

    /**
     * 构建各部门绩效数据
     */
    private List<DashboardStatistics.DepartmentPerformance> buildDepartmentPerformance(
            Long hotelId, LocalDateTime startTime, LocalDateTime endTime) {

        List<Department> departments = departmentRepository.findAll();
        List<DashboardStatistics.DepartmentPerformance> performanceList = new ArrayList<>();

        for (Department dept : departments) {
            try {
                // 获取该部门的所有任务
                List<DepartmentTask> deptTasks = departmentTaskRepository
                        .findByHotelIdAndDepartmentId(hotelId, dept.getDeptId());

                // 筛选时间范围内的任务
                List<DepartmentTask> periodTasks = deptTasks.stream()
                        .filter(t -> t.getCreatedAt().isAfter(startTime) && t.getCreatedAt().isBefore(endTime))
                        .collect(Collectors.toList());

                long total = periodTasks.size();
                long pending = periodTasks.stream().filter(t -> DepartmentTask.TaskStatus.ASSIGNED.equals(t.getStatus())).count();
                long completed = periodTasks.stream().filter(t -> DepartmentTask.TaskStatus.COMPLETED.equals(t.getStatus())).count();

                // 计算平均完成时间
                Double avgCompletionTime = periodTasks.stream()
                        .filter(t -> t.getActualCompletionTime() != null && t.getCreatedAt() != null)
                        .map(t -> ChronoUnit.MINUTES.between(t.getCreatedAt(), t.getActualCompletionTime()))
                        .collect(Collectors.averagingLong(Long::longValue));

                // 获取评价数据
                List<DepartmentFeedback> feedbacks = feedbackRepository.findByHotelId(hotelId).stream()
                        .filter(f -> dept.getDeptId().equals(f.getDepartmentId()))
                        .collect(Collectors.toList());

                // 计算平均评分
                BigDecimal avgRating = feedbacks.isEmpty() ? BigDecimal.ZERO :
                        feedbacks.stream()
                                .map(DepartmentFeedback::getOverallRating)
                                .filter(Objects::nonNull)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                .divide(BigDecimal.valueOf(feedbacks.size()), 2, RoundingMode.HALF_UP);

                // 计算推荐率
                BigDecimal recommendationRate = feedbacks.isEmpty() ? BigDecimal.ZERO :
                        BigDecimal.valueOf(feedbacks.stream()
                                .filter(DepartmentFeedback::getIsRecommended)
                                .count() * 100.0 / feedbacks.size())
                                .setScale(2, RoundingMode.HALF_UP);

                // 计算绩效等级
                String performanceLevel = calculatePerformanceLevel(avgRating, avgCompletionTime);

                performanceList.add(DashboardStatistics.DepartmentPerformance.builder()
                        .departmentId(dept.getDeptId())
                        .departmentName(dept.getDeptName())
                        .totalTasks(total)
                        .pendingTasks(pending)
                        .completedTasks(completed)
                        .averageCompletionTime(avgCompletionTime != null ? avgCompletionTime : 0.0)
                        .averageRating(avgRating)
                        .recommendationRate(recommendationRate)
                        .performanceLevel(performanceLevel)
                        .build());

            } catch (Exception e) {
                log.warn("处理部门{}绩效数据时出错: {}", dept.getDeptName(), e.getMessage());
            }
        }

        return performanceList;
    }

    /**
     * 构建请求趋势数据
     */
    private DashboardStatistics.RequestTrend buildRequestTrend(
            Long hotelId, LocalDateTime startTime, LocalDateTime endTime) {

        List<TaskOrder> periodTasks = taskOrderRepository.findByHotelIdOrderByCreateTimeDesc(hotelId).stream()
                .filter(t -> t.getCreateTime().isAfter(startTime) && t.getCreateTime().isBefore(endTime))
                .collect(Collectors.toList());

        long total = periodTasks.size();

        // 按任务类型分布
        Map<String, Long> taskTypeDistribution = periodTasks.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getTaskType() != null ? t.getTaskType() : "其他",
                        Collectors.counting()
                ));

        // 按优先级分布
        Map<String, Long> priorityDistribution = periodTasks.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getPriority() != null ? t.getPriority() : "NORMAL",
                        Collectors.counting()
                ));

        // 按小时分布
        List<DashboardStatistics.HourlyData> hourlyDistribution = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            final int hour = i;
            long count = periodTasks.stream()
                    .filter(t -> t.getCreateTime().getHour() == hour)
                    .count();
            hourlyDistribution.add(DashboardStatistics.HourlyData.builder()
                    .hour(hour)
                    .count(count)
                    .build());
        }

        // 按日期分布
        Map<String, Long> dailyCountMap = periodTasks.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        Collectors.counting()
                ));

        List<DashboardStatistics.DailyData> dailyDistribution = dailyCountMap.entrySet().stream()
                .map(e -> DashboardStatistics.DailyData.builder()
                        .date(e.getKey())
                        .count(e.getValue())
                        .completedCount(periodTasks.stream()
                                .filter(t -> t.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")).equals(e.getKey())
                                        && "COMPLETED".equals(t.getStatus()))
                                .count())
                        .build())
                .sorted(Comparator.comparing(DashboardStatistics.DailyData::getDate))
                .collect(Collectors.toList());

        // 计算增长率（与上一周期对比）
        BigDecimal growthRate = calculateGrowthRate(hotelId, startTime, endTime, total);

        return DashboardStatistics.RequestTrend.builder()
                .totalRequests(total)
                .growthRate(growthRate)
                .taskTypeDistribution(taskTypeDistribution)
                .priorityDistribution(priorityDistribution)
                .hourlyDistribution(hourlyDistribution)
                .dailyDistribution(dailyDistribution)
                .build();
    }

    /**
     * 构建AI统计信息
     */
    private DashboardStatistics.AIStatistics buildAIStatistics(
            Long hotelId, LocalDateTime startTime, LocalDateTime endTime) {

        List<TaskOrder> periodTasks = taskOrderRepository.findByHotelIdOrderByCreateTimeDesc(hotelId).stream()
                .filter(t -> t.getCreateTime().isAfter(startTime) && t.getCreateTime().isBefore(endTime))
                .collect(Collectors.toList());

        long totalParsed = periodTasks.size();
        long manualInterventionCount = periodTasks.stream()
                .filter(t -> t.getAssignedDepartment() != null && "业务部".equals(t.getAssignedDepartment().getDeptName()))
                .count();

        long successfulParsed = totalParsed - manualInterventionCount;
        long failedParsed = manualInterventionCount;

        BigDecimal successRate = totalParsed > 0
                ? BigDecimal.valueOf(successfulParsed * 100.0 / totalParsed).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal manualInterventionRate = totalParsed > 0
                ? BigDecimal.valueOf(manualInterventionCount * 100.0 / totalParsed).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 按意图分类
        Map<String, Long> intentDistribution = periodTasks.stream()
                .collect(Collectors.groupingBy(
                        t -> extractIntentFromTaskContent(t.getTaskContent()),
                        Collectors.counting()
                ));

        // 按部门分类
        Map<String, Long> departmentDistribution = periodTasks.stream()
                .filter(t -> t.getAssignedDepartment() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getAssignedDepartment().getDeptName(),
                        Collectors.counting()
                ));

        return DashboardStatistics.AIStatistics.builder()
                .totalParsed(totalParsed)
                .successfulParsed(successfulParsed)
                .failedParsed(failedParsed)
                .successRate(successRate)
                .manualInterventionCount(manualInterventionCount)
                .manualInterventionRate(manualInterventionRate)
                .intentDistribution(intentDistribution)
                .departmentDistribution(departmentDistribution)
                .build();
    }

    /**
     * 构建告警信息
     */
    private List<DashboardStatistics.AlertInfo> buildAlerts(Long hotelId) {
        List<DashboardStatistics.AlertInfo> alerts = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // 1. 检查超期任务
        List<DepartmentTask> allTasks = departmentTaskRepository.findAll().stream()
                .filter(t -> t.getHotelId().equals(hotelId))
                .collect(Collectors.toList());

        List<DepartmentTask> overdueTasks = allTasks.stream()
                .filter(t -> DepartmentTask.TaskStatus.ASSIGNED.equals(t.getStatus()) || DepartmentTask.TaskStatus.IN_PROGRESS.equals(t.getStatus()))
                .filter(t -> t.getExpectedCompletionTime() != null && t.getExpectedCompletionTime().isBefore(now))
                .collect(Collectors.toList());

        if (!overdueTasks.isEmpty()) {
            Map<Long, List<DepartmentTask>> byDept = overdueTasks.stream()
                    .collect(Collectors.groupingBy(DepartmentTask::getDepartmentId));

            byDept.forEach((deptId, tasks) -> {
                Department dept = departmentRepository.findById(deptId).orElse(null);
                if (dept != null) {
                    alerts.add(DashboardStatistics.AlertInfo.builder()
                            .alertType("任务超期")
                            .alertLevel(tasks.size() > 5 ? "高" : "中")
                            .message(String.format("部门有%d个任务已超期", tasks.size()))
                            .department(dept.getDeptName())
                            .alertTime(now)
                            .pendingCount((long) tasks.size())
                            .build());
                }
            });
        }

        // 2. 检查部门负载
        List<Department> allDepartments = departmentRepository.findAll();
        for (Department dept : allDepartments) {
            List<DepartmentTask> deptTasks = allTasks.stream()
                    .filter(t -> dept.getDeptId().equals(t.getDepartmentId()))
                    .collect(Collectors.toList());

            long pendingCount = deptTasks.stream()
                    .filter(t -> DepartmentTask.TaskStatus.ASSIGNED.equals(t.getStatus()))
                    .count();

            if (pendingCount > 10) {
                alerts.add(DashboardStatistics.AlertInfo.builder()
                        .alertType("部门负载过高")
                        .alertLevel(pendingCount > 20 ? "高" : "中")
                        .message(String.format("部门有%d个待处理任务", pendingCount))
                        .department(dept.getDeptName())
                        .alertTime(now)
                        .pendingCount(pendingCount)
                        .build());
            }
        }

        // 3. 检查低评分预警
        List<DepartmentFeedback> lowRatings = feedbackRepository.findByHotelId(hotelId).stream()
                .filter(f -> f.getCreatedAt().isAfter(now.minus(1, ChronoUnit.DAYS)))
                .filter(f -> f.getOverallRating() != null && f.getOverallRating().compareTo(new BigDecimal("3.0")) < 0)
                .collect(Collectors.toList());

        if (!lowRatings.isEmpty()) {
            Map<Long, List<DepartmentFeedback>> byDept = lowRatings.stream()
                    .collect(Collectors.groupingBy(DepartmentFeedback::getDepartmentId));

            byDept.forEach((deptId, feedbacks) -> {
                Department dept = departmentRepository.findById(deptId).orElse(null);
                if (dept != null) {
                    alerts.add(DashboardStatistics.AlertInfo.builder()
                            .alertType("客户满意度低")
                            .alertLevel("中")
                            .message(String.format("部门收到%d个低评分评价", feedbacks.size()))
                            .department(dept.getDeptName())
                            .alertTime(now)
                            .pendingCount((long) feedbacks.size())
                            .build());
                }
            });
        }

        return alerts.stream()
                .sorted(Comparator.comparing(DashboardStatistics.AlertInfo::getAlertLevel).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 获取需要关注的任务
     */
    private List<DashboardStatistics.AttentionTask> getAttentionTasks(Long hotelId) {
        List<DashboardStatistics.AttentionTask> attentionTasks = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        List<TaskOrder> allTasks = taskOrderRepository.findByHotelIdOrderByCreateTimeDesc(hotelId);

        for (TaskOrder task : allTasks) {
            if ("COMPLETED".equals(task.getStatus()) || "CANCELED".equals(task.getStatus())) {
                continue;
            }

            String attentionReason = null;
            long waitingMinutes = ChronoUnit.MINUTES.between(task.getCreateTime(), now);

            // 检查超期
            if (task.getDueTime() != null && task.getDueTime().isBefore(now)) {
                attentionReason = "任务超期";
            }
            // 高优先级任务
            else if ("URGENCY".equals(task.getPriority())) {
                attentionReason = "高优先级";
            }
            // 等待时间过长
            else if (waitingMinutes > 60) {
                attentionReason = "等待时间过长";
            }

            if (attentionReason != null) {
                attentionTasks.add(DashboardStatistics.AttentionTask.builder()
                        .taskId(task.getTaskId())
                        .taskContent(task.getTaskContent())
                        .customerId(task.getGuestMemberId())
                        .roomNumber(task.getRoomNumber())
                        .departmentName(task.getAssignedDepartment() != null ? task.getAssignedDepartment().getDeptName() : "未分配")
                        .status(task.getStatus())
                        .priority(task.getPriority())
                        .createTime(task.getCreateTime())
                        .waitingMinutes(waitingMinutes)
                        .attentionReason(attentionReason)
                        .build());
            }
        }

        return attentionTasks.stream()
                .sorted(Comparator.comparing(DashboardStatistics.AttentionTask::getWaitingMinutes).reversed())
                .limit(20)
                .collect(Collectors.toList());
    }

    /**
     * 获取各部门负载情况
     */
    private List<DashboardStatistics.DepartmentLoad> getDepartmentLoads(Long hotelId) {
        List<DashboardStatistics.DepartmentLoad> loads = new ArrayList<>();

        List<Department> departments = departmentRepository.findAll();
        List<DepartmentTask> allTasks = departmentTaskRepository.findAll().stream()
                .filter(t -> t.getHotelId().equals(hotelId))
                .collect(Collectors.toList());

        for (Department dept : departments) {
            List<DepartmentTask> tasks = allTasks.stream()
                    .filter(t -> dept.getDeptId().equals(t.getDepartmentId()))
                    .collect(Collectors.toList());

            long pendingCount = tasks.stream().filter(t -> DepartmentTask.TaskStatus.ASSIGNED.equals(t.getStatus())).count();
            long inProgressCount = tasks.stream().filter(t -> DepartmentTask.TaskStatus.IN_PROGRESS.equals(t.getStatus())).count();
            long totalCount = tasks.size();

            String loadStatus;
            if (totalCount == 0) {
                loadStatus = "正常";
            } else if (pendingCount > 20) {
                loadStatus = "过载";
            } else if (pendingCount > 10) {
                loadStatus = "繁忙";
            } else {
                loadStatus = "正常";
            }

            Double avgProcessTime = tasks.stream()
                    .filter(t -> t.getActualCompletionTime() != null && t.getCreatedAt() != null)
                    .map(t -> ChronoUnit.MINUTES.between(t.getCreatedAt(), t.getActualCompletionTime()))
                    .collect(Collectors.averagingLong(Long::longValue));

            loads.add(DashboardStatistics.DepartmentLoad.builder()
                    .departmentId(dept.getDeptId())
                    .departmentName(dept.getDeptName())
                    .pendingCount(pendingCount)
                    .inProgressCount(inProgressCount)
                    .totalCount(totalCount)
                    .loadStatus(loadStatus)
                    .avgProcessTime(avgProcessTime != null ? avgProcessTime : 0.0)
                    .build());
        }

        return loads.stream()
                .sorted(Comparator.comparing(DashboardStatistics.DepartmentLoad::getPendingCount).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 获取今日VIP客户请求
     */
    private List<DashboardStatistics.VIPRequest> getVIPRequests(Long hotelId) {
        LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();

        return taskOrderRepository.findByHotelIdOrderByCreateTimeDesc(hotelId).stream()
                .filter(t -> t.getCreateTime().isAfter(today))
                .filter(t -> t.getGuestMemberId() != null && t.getGuestMemberId().startsWith("VIP"))
                .map(t -> DashboardStatistics.VIPRequest.builder()
                        .taskId(t.getTaskId())
                        .customerName(t.getGuestMemberId()) // 实际应该从客户表获取姓名
                        .roomNumber(t.getRoomNumber())
                        .requestContent(t.getTaskContent())
                        .requestTime(t.getCreateTime())
                        .status(t.getStatus())
                        .build())
                .sorted(Comparator.comparing(DashboardStatistics.VIPRequest::getRequestTime).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * 获取待处理投诉数量
     */
    private Long getPendingComplaintsCount(Long hotelId) {
        // 简单实现：计算低评分且未处理的反馈
        LocalDateTime yesterday = LocalDateTime.now().minus(1, ChronoUnit.DAYS);

        return feedbackRepository.findByHotelId(hotelId).stream()
                .filter(f -> f.getCreatedAt().isAfter(yesterday))
                .filter(f -> f.getOverallRating() != null && f.getOverallRating().compareTo(new BigDecimal("2.0")) < 0)
                .count();
    }

    /**
     * 获取即将超期任务数
     */
    private Long getSoonOverdueCount(Long hotelId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime soon = now.plus(1, ChronoUnit.HOURS);

        return departmentTaskRepository.findAll().stream()
                .filter(t -> t.getHotelId().equals(hotelId))
                .filter(t -> DepartmentTask.TaskStatus.ASSIGNED.equals(t.getStatus()) || DepartmentTask.TaskStatus.IN_PROGRESS.equals(t.getStatus()))
                .filter(t -> t.getExpectedCompletionTime() != null)
                .filter(t -> t.getExpectedCompletionTime().isAfter(now) && t.getExpectedCompletionTime().isBefore(soon))
                .count();
    }

    // ==================== 辅助计算方法 ====================

    private long calculateOverdueTasks(List<TaskOrder> tasks) {
        LocalDateTime now = LocalDateTime.now();
        return tasks.stream()
                .filter(t -> ("PENDING".equals(t.getStatus()) || "IN_PROGRESS".equals(t.getStatus())))
                .filter(t -> t.getDueTime() != null && t.getDueTime().isBefore(now))
                .count();
    }

    private Double calculateAverageCompletionTime(Long hotelId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            List<TaskOrder> completedTasks = taskOrderRepository.findByHotelIdOrderByCreateTimeDesc(hotelId).stream()
                    .filter(t -> t.getCreateTime().isAfter(startTime) && t.getCreateTime().isBefore(endTime))
                    .filter(t -> "COMPLETED".equals(t.getStatus()))
                    .filter(t -> t.getCompletedTime() != null)
                    .collect(Collectors.toList());

            if (completedTasks.isEmpty()) {
                return 0.0;
            }

            return completedTasks.stream()
                    .mapToLong(t -> ChronoUnit.MINUTES.between(t.getCreateTime(), t.getCompletedTime()))
                    .average()
                    .orElse(0.0);
        } catch (Exception e) {
            log.warn("计算平均完成时间时出错: {}", e.getMessage());
            return 0.0;
        }
    }

    private String calculatePerformanceLevel(BigDecimal avgRating, Double avgCompletionTime) {
        if (avgRating.compareTo(new BigDecimal("4.5")) >= 0 && avgCompletionTime < 30) {
            return "优秀";
        } else if (avgRating.compareTo(new BigDecimal("4.0")) >= 0) {
            return "良好";
        } else if (avgRating.compareTo(new BigDecimal("3.0")) >= 0) {
            return "一般";
        } else {
            return "需改进";
        }
    }

    private String extractIntentFromTaskContent(String taskContent) {
        if (taskContent == null) {
            return "未知";
        }

        if (taskContent.contains("客户请求:")) {
            String content = taskContent.substring(taskContent.indexOf("客户请求:") + 5);
            if (content.contains("维修") || content.contains("坏了") || content.contains("故障")) {
                return "设备维修";
            } else if (content.contains("毛巾") || content.contains("水") || content.contains("用品")) {
                return "客房服务";
            } else if (content.contains("餐饮") || content.contains("食物") || content.contains("送餐")) {
                return "餐饮服务";
            } else if (content.contains("投诉") || content.contains("不满")) {
                return "投诉处理";
            }
        }

        return "其他";
    }

    private BigDecimal calculateGrowthRate(Long hotelId, LocalDateTime startTime, LocalDateTime endTime, long currentTotal) {
        long periodHours = ChronoUnit.HOURS.between(startTime, endTime);
        LocalDateTime prevStart = startTime.minus(periodHours, ChronoUnit.HOURS);
        LocalDateTime prevEnd = startTime;

        long previousTotal = taskOrderRepository.findByHotelIdOrderByCreateTimeDesc(hotelId).stream()
                .filter(t -> t.getCreateTime().isAfter(prevStart) && t.getCreateTime().isBefore(prevEnd))
                .count();

        if (previousTotal == 0) {
            return currentTotal > 0 ? new BigDecimal("100.00") : BigDecimal.ZERO;
        }

        return BigDecimal.valueOf((currentTotal - previousTotal) * 100.0 / previousTotal)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
