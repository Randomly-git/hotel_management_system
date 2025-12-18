package com.hotel.hotel.service;

import com.hotel.hotel.entity.Department;
import com.hotel.hotel.entity.TaskOrder;
import com.hotel.hotel.repository.DepartmentRepository;
import com.hotel.hotel.repository.TaskOrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 部门任务管理服务
 * 负责各部门的任务分配、处理和状态管理
 */
@Slf4j
@Service
public class DepartmentTaskService {

    private final TaskOrderRepository taskOrderRepository;
    private final DepartmentRepository departmentRepository;

    @Autowired
    public DepartmentTaskService(TaskOrderRepository taskOrderRepository,
                                DepartmentRepository departmentRepository) {
        this.taskOrderRepository = taskOrderRepository;
        this.departmentRepository = departmentRepository;
    }

    /**
     * 获取指定部门的待处理任务
     * @param departmentName 部门名称
     * @param hotelId 酒店ID（租户隔离）
     * @return 待处理任务列表
     */
    public List<TaskOrder> getDepartmentPendingTasks(String departmentName, Long hotelId) {
        log.info("获取部门 {} 的待处理任务，酒店ID: {}", departmentName, hotelId);

        return taskOrderRepository.findByAssignedDepartment_DeptNameAndStatusAndHotelIdOrderByCreateTimeDesc(
                departmentName, "PENDING", hotelId);
    }

    /**
     * 获取指定部门的所有任务
     * @param departmentName 部门名称
     * @param hotelId 酒店ID（租户隔离）
     * @return 所有任务列表
     */
    public List<TaskOrder> getDepartmentAllTasks(String departmentName, Long hotelId) {
        log.info("获取部门 {} 的所有任务，酒店ID: {}", departmentName, hotelId);

        return taskOrderRepository.findByAssignedDepartment_DeptNameAndHotelIdOrderByCreateTimeDesc(
                departmentName, hotelId);
    }

    /**
     * 接受任务（将状态从PENDING改为IN_PROGRESS）
     * @param taskId 任务ID
     * @param departmentId 部门ID（验证权限）
     * @return 更新后的任务
     */
    @Transactional
    public TaskOrder acceptTask(Long taskId, Long departmentId) {
        log.info("部门 {} 接受任务: {}", departmentId, taskId);

        TaskOrder task = taskOrderRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在: " + taskId));

        // 验证任务是否分配给该部门
        if (!task.getAssignedDepartment().getDeptId().equals(departmentId)) {
            throw new IllegalArgumentException("该任务未分配给此部门");
        }

        // 验证任务状态
        if (!"PENDING".equals(task.getStatus())) {
            throw new IllegalArgumentException("只能接受待处理状态的任务");
        }

        task.setStatus("IN_PROGRESS");
        return taskOrderRepository.save(task);
    }

    /**
     * 完成任务（将状态改为COMPLETED）
     * @param taskId 任务ID
     * @param departmentId 部门ID（验证权限）
     * @param completionRemark 完成备注
     * @return 更新后的任务
     */
    @Transactional
    public TaskOrder completeTask(Long taskId, Long departmentId, String completionRemark) {
        log.info("部门 {} 完成任务: {}, 备注: {}", departmentId, taskId, completionRemark);

        TaskOrder task = taskOrderRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在: " + taskId));

        // 验证任务是否分配给该部门
        if (!task.getAssignedDepartment().getDeptId().equals(departmentId)) {
            throw new IllegalArgumentException("该任务未分配给此部门");
        }

        // 验证任务状态
        if (!"IN_PROGRESS".equals(task.getStatus())) {
            throw new IllegalArgumentException("只能完成处理中状态的任务");
        }

        task.setStatus("COMPLETED");
        if (completionRemark != null && !completionRemark.trim().isEmpty()) {
            task.setTaskContent(task.getTaskContent() + "\n完成备注: " + completionRemark);
        }

        return taskOrderRepository.save(task);
    }

    /**
     * 取消任务
     * @param taskId 任务ID
     * @param departmentId 部门ID（验证权限）
     * @param cancelReason 取消原因
     * @return 更新后的任务
     */
    @Transactional
    public TaskOrder cancelTask(Long taskId, Long departmentId, String cancelReason) {
        log.info("部门 {} 取消任务: {}, 原因: {}", departmentId, taskId, cancelReason);

        TaskOrder task = taskOrderRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在: " + taskId));

        // 验证任务是否分配给该部门
        if (!task.getAssignedDepartment().getDeptId().equals(departmentId)) {
            throw new IllegalArgumentException("该任务未分配给此部门");
        }

        task.setStatus("CANCELLED");
        if (cancelReason != null && !cancelReason.trim().isEmpty()) {
            task.setTaskContent(task.getTaskContent() + "\n取消原因: " + cancelReason);
        }

        return taskOrderRepository.save(task);
    }

    /**
     * 获取部门任务统计
     * @param departmentName 部门名称
     * @param hotelId 酒店ID（租户隔离）
     * @return 统计信息
     */
    public Map<String, Object> getDepartmentTaskStatistics(String departmentName, Long hotelId) {
        log.info("获取部门 {} 的任务统计，酒店ID: {}", departmentName, hotelId);

        List<TaskOrder> tasks = taskOrderRepository.findByAssignedDepartment_DeptNameAndHotelId(
                departmentName, hotelId);

        Map<String, Long> statusCount = tasks.stream()
                .collect(Collectors.groupingBy(TaskOrder::getStatus, Collectors.counting()));

        // 计算平均处理时间
        double avgProcessTime = tasks.stream()
                .filter(task -> "COMPLETED".equals(task.getStatus()) && task.getCompletedTime() != null)
                .mapToLong(task -> java.time.Duration.between(task.getCreateTime(), task.getCompletedTime()).toMinutes())
                .average()
                .orElse(0.0);

        // 计算超时任务数
        long overdueCount = tasks.stream()
                .filter(task -> "PENDING".equals(task.getStatus()) &&
                        task.getDueTime() != null &&
                        task.getDueTime().isBefore(LocalDateTime.now()))
                .count();

        return Map.of(
                "totalTasks", tasks.size(),
                "pendingTasks", statusCount.getOrDefault("PENDING", 0L),
                "inProgressTasks", statusCount.getOrDefault("IN_PROGRESS", 0L),
                "completedTasks", statusCount.getOrDefault("COMPLETED", 0L),
                "cancelledTasks", statusCount.getOrDefault("CANCELLED", 0L),
                "avgProcessTimeMinutes", avgProcessTime,
                "overdueTasks", overdueCount
        );
    }

    /**
     * 批量分配任务给部门
     * @param taskIds 任务ID列表
     * @param departmentName 部门名称
     * @param hotelId 酒店ID（租户隔离）
     * @return 更新的任务数量
     */
    @Transactional
    public int batchAssignToDepartment(List<Long> taskIds, String departmentName, Long hotelId) {
        log.info("批量分配 {} 个任务给部门 {}, 酒店ID: {}", taskIds.size(), departmentName, hotelId);

        Department department = departmentRepository.findByDeptName(departmentName)
                .orElseThrow(() -> new RuntimeException("部门不存在: " + departmentName));

        return taskOrderRepository.assignTasksToDepartment(taskIds, department.getDeptId(), hotelId);
    }
}