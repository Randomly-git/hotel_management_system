package com.hotel.hotel.controller;

import com.hotel.hotel.entity.CustomerFeedback;
import com.hotel.hotel.repository.CustomerFeedbackRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "评价审核管理")
public class AuditController {

    @Autowired
    private CustomerFeedbackRepository feedbackRepository;

    @GetMapping("/pending")
    @Operation(summary = "获取待审核评价", description = "返回经过精简的、人性化的恶意评论审核列表")
    public List<Map<String, Object>> getPendingAudits() {
        List<CustomerFeedback> rawList = feedbackRepository.findByNeedsReviewTrueAndReviewStatus("PENDING");

        // 重新包装返回结构，使其对人类审核员更友好
        return rawList.stream().map(f -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("ID", f.getFeedbackId());
            map.put("客户姓名", f.getCustomerName());
            map.put("评论内容", f.getFeedbackContent());
            map.put("情感得分", f.getSentimentScore());
            map.put("所属部门", f.getDepartment() != null ? f.getDepartment().getDeptName() : "未知");
            map.put("提交时间", f.getFeedbackTime());
            return map;
        }).collect(Collectors.toList());
    }

    @PostMapping("/process")
    @Operation(summary = "提交审核结论", description = "批准(APPROVE)将计入绩效，拒绝(REJECT)将永久排除该评价")
    public ResponseEntity<String> processAudit(
            @RequestParam Long feedbackId,
            @Parameter(description = "审核操作", schema = @Schema(allowableValues = {"APPROVE", "REJECT"}))
            @RequestParam String action,
            @RequestParam(required = false) String comment) {

        CustomerFeedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("评价不存在"));

        if ("APPROVE".equalsIgnoreCase(action)) {
            feedback.setNeedsReview(false); // 关键：设为 false 才能被 calculate 接口查到
            feedback.setReviewStatus("APPROVED");
        } else if ("REJECT".equalsIgnoreCase(action)) {
            feedback.setReviewStatus("REJECTED");
            // 注意：REJECT 时 needsReview 保持为 true 或不做修改，
            // 只要 calculate 逻辑过滤了状态，它就不会被计算
        } else {
            return ResponseEntity.badRequest().body("非法操作指令，只能是 APPROVE 或 REJECT");
        }

        feedback.setReviewComment(comment);
        feedbackRepository.save(feedback);

        return ResponseEntity.ok("审核操作成功：该评价已标记为 " + feedback.getReviewStatus());
    }
}