package com.hotel.hotel.service;

import com.hotel.hotel.entity.TaskHistory;
import com.hotel.hotel.entity.TaskOrder;
import com.hotel.hotel.repository.TaskHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务历史记录服务类
 * 负责记录和管理任务的状态变更历史
 */
@Slf4j
@Service
public class TaskHistoryService {

    @Autowired
    private TaskHistoryRepository taskHistoryRepository;

    /**
     * 记录任务创建历史
     */
    @Transactional
    public TaskHistory recordTaskCreation(TaskOrder task, String operatorId, String operatorName) {
        log.info("记录任务创建历史: taskId={}, operatorId={}", task.getTaskId(), operatorId);

        TaskHistory history = TaskHistory.builder()
                .taskOrderId(task.getTaskId())
                .oldStatus(null)
                .newStatus(task.getStatus())
                .operationType(TaskHistory.OperationType.CREATE.getCode())
                .operationReason("任务创建")
                .operatorId(operatorId)
                .operatorName(operatorName)
                .operatorType(TaskHistory.OperatorType.CUSTOMER.getCode())
                .departmentId(task.getAssignedDepartment() != null ? task.getAssignedDepartment().getDeptId() : null)
                .departmentName(task.getAssignedDepartment() != null ? task.getAssignedDepartment().getDeptName() : null)
                .hotelId(task.getHotelId())
                .build();

        return taskHistoryRepository.save(history);
    }

    /**
     * 记录任务状态变更历史
     */
    @Transactional
    public TaskHistory recordTaskStatusChange(TaskOrder task, String oldStatus, String newStatus,
                                              String operationReason, String operatorId, String operatorName) {
        log.info("记录任务状态变更历史: taskId={}, oldStatus={}, newStatus={}, operatorId={}",
                task.getTaskId(), oldStatus, newStatus, operatorId);

        String operationType = determineOperationType(newStatus, oldStatus);

        TaskHistory history = TaskHistory.builder()
                .taskOrderId(task.getTaskId())
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .operationType(operationType)
                .operationReason(operationReason)
                .operatorId(operatorId)
                .operatorName(operatorName)
                .operatorType(TaskHistory.OperatorType.STAFF.getCode())
                .departmentId(task.getAssignedDepartment() != null ? task.getAssignedDepartment().getDeptId() : null)
                .departmentName(task.getAssignedDepartment() != null ? task.getAssignedDepartment().getDeptName() : null)
                .hotelId(task.getHotelId())
                .build();

        return taskHistoryRepository.save(history);
    }

    /**
     * 记录任务分配历史
     */
    @Transactional
    public TaskHistory recordTaskAssignment(TaskOrder task, String operatorId, String operatorName) {
        log.info("记录任务分配历史: taskId={}, operatorId={}", task.getTaskId(), operatorId);

        TaskHistory history = TaskHistory.builder()
                .taskOrderId(task.getTaskId())
                .oldStatus(null)
                .newStatus(task.getStatus())
                .operationType(TaskHistory.OperationType.ASSIGN.getCode())
                .operationReason("任务分配到部门: " + task.getAssignedDepartment().getDeptName())
                .operatorId(operatorId)
                .operatorName(operatorName)
                .operatorType(TaskHistory.OperatorType.SYSTEM.getCode())
                .departmentId(task.getAssignedDepartment().getDeptId())
                .departmentName(task.getAssignedDepartment().getDeptName())
                .hotelId(task.getHotelId())
                .build();

        return taskHistoryRepository.save(history);
    }

    /**
     * 记录任务取消历史
     */
    @Transactional
    public TaskHistory recordTaskCancellation(TaskOrder task, String cancelReason, String operatorId, String operatorName) {
        log.info("记录任务取消历史: taskId={}, cancelReason={}, operatorId={}",
                task.getTaskId(), cancelReason, operatorId);

        TaskHistory history = TaskHistory.builder()
                .taskOrderId(task.getTaskId())
                .oldStatus(task.getStatus())
                .newStatus("CANCELED")
                .operationType(TaskHistory.OperationType.CANCEL.getCode())
                .operationReason(cancelReason)
                .operatorId(operatorId)
                .operatorName(operatorName)
                .operatorType(TaskHistory.OperatorType.CUSTOMER.getCode())
                .departmentId(task.getAssignedDepartment() != null ? task.getAssignedDepartment().getDeptId() : null)
                .departmentName(task.getAssignedDepartment() != null ? task.getAssignedDepartment().getDeptName() : null)
                .hotelId(task.getHotelId())
                .build();

        return taskHistoryRepository.save(history);
    }

    /**
     * 获取任务的历史记录
     */
    public List<TaskHistory> getTaskHistory(Long taskOrderId) {
        return taskHistoryRepository.findByTaskOrderIdOrderByOperationTimeDesc(taskOrderId);
    }

    /**
     * 获取酒店的操作历史记录
     */
    public List<TaskHistory> getHotelHistory(Long hotelId, LocalDateTime startTime, LocalDateTime endTime) {
        return taskHistoryRepository.findByHotelIdAndOperationTimeBetween(hotelId, startTime, endTime);
    }

    /**
     * 根据状态变更确定操作类型
     */
    private String determineOperationType(String newStatus, String oldStatus) {
        if ("COMPLETED".equals(newStatus)) {
            return TaskHistory.OperationType.COMPLETE.getCode();
        } else if ("CANCELED".equals(newStatus)) {
            return TaskHistory.OperationType.CANCEL.getCode();
        } else if ("IN_PROGRESS".equals(newStatus)) {
            return TaskHistory.OperationType.ACCEPT.getCode();
        } else {
            return TaskHistory.OperationType.UPDATE.getCode();
        }
    }

    /**
     * 获取操作统计信息
     */
    public List<Object[]> getOperationStatistics(Long hotelId, LocalDateTime startTime, LocalDateTime endTime) {
        return taskHistoryRepository.countOperationsByTypeInPeriod(hotelId, startTime, endTime);
    }

    /**
     * 计算平均处理时间
     */
    public Double calculateAverageProcessingTime(Long hotelId, LocalDateTime startTime, LocalDateTime endTime) {
        return taskHistoryRepository.calculateAverageProcessingTime(hotelId, startTime, endTime);
    }
}