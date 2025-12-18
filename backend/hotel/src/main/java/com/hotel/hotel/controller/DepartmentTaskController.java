package com.hotel.hotel.controller;

import com.hotel.hotel.common.Response;
import com.hotel.hotel.entity.TaskOrder;
import com.hotel.hotel.entity.DepartmentTask;
import com.hotel.hotel.service.DepartmentTaskService;
import com.hotel.hotel.service.DepartmentTaskManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 部门任务管理控制器
 * 为各部门提供任务管理功能
 */
@RestController
@RequestMapping("/api/v1/department-tasks")
@Tag(name = "部门任务管理", description = "各部门的任务处理和状态管理")
public class DepartmentTaskController {

    private final DepartmentTaskService departmentTaskService;
    private final DepartmentTaskManagementService departmentTaskManagementService;

    @Autowired
    public DepartmentTaskController(DepartmentTaskService departmentTaskService,
                                     DepartmentTaskManagementService departmentTaskManagementService) {
        this.departmentTaskService = departmentTaskService;
        this.departmentTaskManagementService = departmentTaskManagementService;
    }

    /**
     * 获取部门的待处理任务
     */
    @Operation(summary = "获取部门待处理任务", description = "获取指定部门的待处理任务列表")
    @GetMapping("/pending/{departmentName}")
    public ResponseEntity<Response<List<TaskOrder>>> getDepartmentPendingTasks(
            @Parameter(description = "部门名称") @PathVariable String departmentName,
            @Parameter(description = "酒店ID") @RequestParam Long hotelId) {

        try {
            List<TaskOrder> tasks = departmentTaskService.getDepartmentPendingTasks(departmentName, hotelId);
            return ResponseEntity.ok(Response.success(tasks));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Response.error("获取待处理任务失败: " + e.getMessage()));
        }
    }

    /**
     * 获取部门的所有任务
     */
    @Operation(summary = "获取部门所有任务", description = "获取指定部门的所有任务（包括各种状态）")
    @GetMapping("/all/{departmentName}")
    public ResponseEntity<Response<List<TaskOrder>>> getDepartmentAllTasks(
            @Parameter(description = "部门名称") @PathVariable String departmentName,
            @Parameter(description = "酒店ID") @RequestParam Long hotelId) {

        try {
            List<TaskOrder> tasks = departmentTaskService.getDepartmentAllTasks(departmentName, hotelId);
            return ResponseEntity.ok(Response.success(tasks));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Response.error("获取任务列表失败: " + e.getMessage()));
        }
    }

    /**
     * 接受任务
     */
    @Operation(summary = "接受任务", description = "部门接受任务，状态从PENDING改为IN_PROGRESS")
    @PostMapping("/accept/{taskId}")
    public ResponseEntity<Response<TaskOrder>> acceptTask(
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            @RequestBody AcceptTaskRequest request) {

        try {
            TaskOrder task = departmentTaskService.acceptTask(taskId, request.getDepartmentId());
            return ResponseEntity.ok(Response.success("任务接受成功", task));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Response.error("接受任务失败: " + e.getMessage()));
        }
    }

    /**
     * 完成任务
     */
    @Operation(summary = "完成任务", description = "部门完成任务，状态改为COMPLETED")
    @PostMapping("/complete/{taskId}")
    public ResponseEntity<Response<TaskOrder>> completeTask(
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            @RequestBody CompleteTaskRequest request) {

        try {
            TaskOrder task = departmentTaskService.completeTask(
                taskId, request.getDepartmentId(), request.getCompletionRemark());
            return ResponseEntity.ok(Response.success("任务完成成功", task));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Response.error("完成任务失败: " + e.getMessage()));
        }
    }

    /**
     * 取消任务
     */
    @Operation(summary = "取消任务", description = "部门取消任务，状态改为CANCELLED")
    @PostMapping("/cancel/{taskId}")
    public ResponseEntity<Response<TaskOrder>> cancelTask(
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            @RequestBody CancelTaskRequest request) {

        try {
            TaskOrder task = departmentTaskService.cancelTask(
                taskId, request.getDepartmentId(), request.getCancelReason());
            return ResponseEntity.ok(Response.success("任务取消成功", task));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Response.error("取消任务失败: " + e.getMessage()));
        }
    }

    /**
     * 获取部门任务统计
     */
    @Operation(summary = "部门任务统计", description = "获取指定部门的任务统计信息")
    @GetMapping("/statistics/{departmentName}")
    public ResponseEntity<Response<Map<String, Object>>> getDepartmentStatistics(
            @Parameter(description = "部门名称") @PathVariable String departmentName,
            @Parameter(description = "酒店ID") @RequestParam Long hotelId) {

        try {
            Map<String, Object> statistics = departmentTaskService.getDepartmentTaskStatistics(departmentName, hotelId);
            return ResponseEntity.ok(Response.success(statistics));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Response.error("获取统计信息失败: " + e.getMessage()));
        }
    }

    /**
     * 批量分配任务给部门
     */
    @Operation(summary = "批量分配任务", description = "将多个任务批量分配给指定部门")
    @PostMapping("/batch-assign")
    public ResponseEntity<Response<String>> batchAssignTasks(
            @RequestBody BatchAssignRequest request) {

        try {
            int updatedCount = departmentTaskService.batchAssignToDepartment(
                request.getTaskIds(), request.getDepartmentName(), request.getHotelId());
            return ResponseEntity.ok(Response.success("成功分配 " + updatedCount + " 个任务"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Response.error("批量分配失败: " + e.getMessage()));
        }
    }

    /**
     * 获取部门任务详情（从department_task表）
     */
    @Operation(summary = "获取部门任务详情", description = "从department_task表获取指定部门的任务列表")
    @GetMapping("/new/{departmentName}/tasks")
    public ResponseEntity<Response<List<DepartmentTask>>> getDepartmentTasks(
            @Parameter(description = "部门名称") @PathVariable String departmentName,
            @Parameter(description = "酒店ID") @RequestParam Long hotelId) {

        try {
            List<DepartmentTask> tasks = departmentTaskManagementService.getTasksByDepartment(hotelId, departmentName);
            return ResponseEntity.ok(Response.success(tasks));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Response.error("获取部门任务失败: " + e.getMessage()));
        }
    }

    /**
     * 获取部门任务统计（从department_task表）
     */
    @Operation(summary = "部门任务统计", description = "获取department_task表的统计信息")
    @GetMapping("/new/statistics")
    public ResponseEntity<Response<Map<String, Object>>> getDepartmentTaskStatistics(
            @Parameter(description = "酒店ID") @RequestParam Long hotelId) {

        try {
            Map<String, Object> statistics = departmentTaskManagementService.getDepartmentTaskStatistics(hotelId);
            return ResponseEntity.ok(Response.success(statistics));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Response.error("获取统计失败: " + e.getMessage()));
        }
    }

    /**
     * 管理端完成任务 - 同时更新task_order和department_task
     */
    @Operation(summary = "管理端完成任务", description = "完成任务并更新task_order状态为COMPLETED，同时记录department_task的实际完成时间")
    @PutMapping("/manage/{taskId}/complete")
    public ResponseEntity<Response<String>> completeTaskManagement(
            @Parameter(description = "部门任务ID") @PathVariable Long taskId,
            @RequestBody CompleteTaskRequest request) {

        try {
            departmentTaskManagementService.completeTaskWithTaskOrder(taskId, request.getCompletionRemark());
            return ResponseEntity.ok(Response.success("任务完成成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Response.error("完成任务失败: " + e.getMessage()));
        }
    }

    // DTO类定义
    @Data
    public static class AcceptTaskRequest {
        private Long departmentId;
    }

    @Data
    public static class CompleteTaskRequest {
        private Long departmentId;
        private String completionRemark;
    }

    @Data
    public static class CancelTaskRequest {
        private Long departmentId;
        private String cancelReason;
    }

    @Data
    public static class BatchAssignRequest {
        private List<Long> taskIds;
        private String departmentName;
        private Long hotelId;
    }
}