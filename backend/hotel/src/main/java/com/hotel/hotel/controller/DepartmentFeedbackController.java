package com.hotel.hotel.controller;

import com.hotel.hotel.common.Response;
import com.hotel.hotel.entity.DepartmentFeedback;
import com.hotel.hotel.service.DepartmentFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 部门评价管理控制器
 * 提供客户满意度调查和评价管理功能
 */
@RestController
@RequestMapping("/api/v1/department-feedback")
@Tag(name = "部门评价管理", description = "客户满意度调查和评价管理")
public class DepartmentFeedbackController {

    @Autowired
    private DepartmentFeedbackService feedbackService;

    /**
     * 客户提交评价
     */
    @Operation(summary = "客户提交评价", description = "客户对已完成的任务进行满意度评价")
    @PostMapping("/submit")
    public ResponseEntity<Response<DepartmentFeedback>> submitFeedback(
            @RequestBody DepartmentFeedbackService.FeedbackRequest request) {

        try {
            DepartmentFeedback feedback = feedbackService.createFeedback(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Response.success("评价提交成功", feedback));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Response.error("评价提交失败: " + e.getMessage()));
        }
    }

    /**
     * 查询任务评价
     */
    @Operation(summary = "查询任务评价", description = "根据任务单ID查询评价信息")
    @GetMapping("/task/{taskOrderId}")
    public ResponseEntity<Response<DepartmentFeedback>> getTaskFeedback(
            @Parameter(description = "任务单ID") @PathVariable Long taskOrderId) {

        try {
            return feedbackService.getFeedbackByTaskOrderId(taskOrderId)
                    .map(feedback -> ResponseEntity.ok(Response.success(feedback)))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Response.error("未找到评价信息")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("查询失败: " + e.getMessage()));
        }
    }

    /**
     * 获取客户评价历史
     */
    @Operation(summary = "获取客户评价历史", description = "获取指定客户的所有评价记录")
    @GetMapping("/customer/{customerId}/history")
    public ResponseEntity<Response<List<DepartmentFeedback>>> getCustomerFeedbackHistory(
            @Parameter(description = "客户ID") @PathVariable String customerId,
            @Parameter(description = "酒店ID") @RequestParam Long hotelId) {

        try {
            List<DepartmentFeedback> feedbacks = feedbackService.getCustomerFeedbackHistory(customerId, hotelId);
            return ResponseEntity.ok(Response.success(feedbacks));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("获取历史记录失败: " + e.getMessage()));
        }
    }

    /**
     * 获取部门评价统计
     */
    @Operation(summary = "获取部门评价统计", description = "获取指定部门的评价统计数据")
    @GetMapping("/department/statistics")
    public ResponseEntity<Response<Map<String, Object>>> getDepartmentStatistics(
            @Parameter(description = "酒店ID") @RequestParam Long hotelId,
            @Parameter(description = "部门ID") @RequestParam Long departmentId) {

        try {
            Map<String, Object> statistics = feedbackService.getDepartmentFeedbackStatistics(hotelId, departmentId);
            return ResponseEntity.ok(Response.success(statistics));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("获取统计数据失败: " + e.getMessage()));
        }
    }

    /**
     * 获取酒店整体评价统计
     */
    @Operation(summary = "获取酒店整体评价统计", description = "获取整个酒店的评价统计数据")
    @GetMapping("/hotel/statistics")
    public ResponseEntity<Response<Map<String, Object>>> getHotelStatistics(
            @Parameter(description = "酒店ID") @RequestParam Long hotelId) {

        try {
            Map<String, Object> statistics = feedbackService.getHotelFeedbackStatistics(hotelId);
            return ResponseEntity.ok(Response.success(statistics));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("获取统计数据失败: " + e.getMessage()));
        }
    }

    /**
     * 检查任务是否可以评价
     */
    @Operation(summary = "检查任务是否可以评价", description = "检查指定任务是否已完成且未评价")
    @GetMapping("/task/{taskOrderId}/check-feedback-eligibility")
    public ResponseEntity<Response<Map<String, Object>>> checkFeedbackEligibility(
            @Parameter(description = "任务单ID") @PathVariable Long taskOrderId) {

        try {
            boolean hasFeedback = feedbackService.getFeedbackByTaskOrderId(taskOrderId).isPresent();

            Map<String, Object> result = Map.of(
                "canFeedback", !hasFeedback,
                "hasFeedback", hasFeedback,
                "message", hasFeedback ? "该任务已评价" : "可以进行评价"
            );

            return ResponseEntity.ok(Response.success(result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("检查失败: " + e.getMessage()));
        }
    }
}