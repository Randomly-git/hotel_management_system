package com.hotel.hotel.controller;

import com.hotel.hotel.common.Response;
import com.hotel.hotel.entity.TaskHistory;
import com.hotel.hotel.service.TaskHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务历史记录控制器
 * 提供任务审计和操作历史查询功能
 */
@RestController
@RequestMapping("/api/v1/task-history")
@Tag(name = "任务历史管理", description = "任务历史记录和审计功能")
public class TaskHistoryController {

    @Autowired
    private TaskHistoryService taskHistoryService;

    /**
     * 查询任务历史记录
     */
    @Operation(summary = "查询任务历史", description = "根据任务单ID查询完整的操作历史")
    @GetMapping("/task/{taskOrderId}")
    public ResponseEntity<Response<List<TaskHistory>>> getTaskHistory(
            @Parameter(description = "任务单ID") @PathVariable Long taskOrderId) {

        try {
            List<TaskHistory> history = taskHistoryService.getTaskHistory(taskOrderId);
            return ResponseEntity.ok(Response.success(history));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Response.error("查询任务历史失败: " + e.getMessage()));
        }
    }

    /**
     * 查询酒店操作历史
     */
    @Operation(summary = "查询酒店操作历史", description = "根据时间范围查询酒店的所有操作历史")
    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<Response<List<TaskHistory>>> getHotelHistory(
            @Parameter(description = "酒店ID") @PathVariable Long hotelId,
            @Parameter(description = "开始时间") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        try {
            List<TaskHistory> history = taskHistoryService.getHotelHistory(hotelId, startTime, endTime);
            return ResponseEntity.ok(Response.success(history));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Response.error("查询酒店历史失败: " + e.getMessage()));
        }
    }

    /**
     * 获取操作统计信息
     */
    @Operation(summary = "获取操作统计", description = "获取指定时间范围内的操作统计数据")
    @GetMapping("/statistics/{hotelId}")
    public ResponseEntity<Response<StatisticsResponse>> getOperationStatistics(
            @Parameter(description = "酒店ID") @PathVariable Long hotelId,
            @Parameter(description = "开始时间") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        try {
            List<Object[]> operationStats = taskHistoryService.getOperationStatistics(hotelId, startTime, endTime);
            Double avgProcessingTime = taskHistoryService.calculateAverageProcessingTime(hotelId, startTime, endTime);

            StatisticsResponse responseData = new StatisticsResponse();
            responseData.setOperationStats(operationStats);
            responseData.setAverageProcessingTime(avgProcessingTime);

            return ResponseEntity.ok(Response.success("统计数据获取成功", responseData));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Response.error("获取统计数据失败: " + e.getMessage()));
        }
    }

    /**
     * 获取今日操作统计
     */
    @Operation(summary = "获取今日操作统计", description = "获取今日的操作统计信息")
    @GetMapping("/today/{hotelId}")
    public ResponseEntity<Response<StatisticsResponse>> getTodayStatistics(
            @Parameter(description = "酒店ID") @PathVariable Long hotelId) {

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59);

            List<Object[]> operationStats = taskHistoryService.getOperationStatistics(hotelId, startOfDay, endOfDay);
            Double avgProcessingTime = taskHistoryService.calculateAverageProcessingTime(hotelId, startOfDay, endOfDay);

            StatisticsResponse responseData = new StatisticsResponse();
            responseData.setOperationStats(operationStats);
            responseData.setAverageProcessingTime(avgProcessingTime);

            return ResponseEntity.ok(Response.success("今日统计获取成功", responseData));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Response.error("获取今日统计失败: " + e.getMessage()));
        }
    }

    /**
     * 统计数据响应DTO
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class StatisticsResponse {
        private List<Object[]> operationStats;
        private Double averageProcessingTime;
    }
}