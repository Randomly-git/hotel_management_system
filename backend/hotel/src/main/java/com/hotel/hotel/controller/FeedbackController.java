package com.hotel.hotel.controller;

import com.hotel.hotel.dto.FeedbackRequest;
import com.hotel.hotel.entity.CustomerFeedback;
import com.hotel.hotel.entity.Department;
import com.hotel.hotel.repository.CustomerFeedbackRepository;
import com.hotel.hotel.repository.DepartmentRepository;
import com.hotel.hotel.service.ZhipuNlpService;
import com.hotel.hotel.service.ZhipuNlpService.NlpResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/feedback")
@Tag(name = "绩效与建议管理", description = "负责部门绩效计算、趋势查询及 AI 建议生成")
public class FeedbackController {

    private final ZhipuNlpService nlpService;
    private final CustomerFeedbackRepository feedbackRepository;
    private final DepartmentRepository departmentRepository;

    @Autowired
    public FeedbackController(ZhipuNlpService nlpService,
                              CustomerFeedbackRepository feedbackRepository,
                              DepartmentRepository departmentRepository) {
        this.nlpService = nlpService;
        this.feedbackRepository = feedbackRepository;
        this.departmentRepository = departmentRepository;
    }

    @PostMapping
    @Operation(
            summary = "客户反馈提交（含实时AI打分）",
            description = "接收客户反馈，由智谱AI实时分析其『情感得分』并自动识别『责任部门』，存入数据库待后续绩效汇总。"
    )
    public ResponseEntity<Map<String, Object>> submitFeedback(@Valid @RequestBody FeedbackRequest request) {

        // 1. 调用真实的 Zhipu AI 进行分析
        NlpResult nlpResult = nlpService.analyzeFeedback(request.getFeedbackContent());

        // 2. 根据 AI 返回的部门名称查找数据库
        // 如果 AI 返回的部门找不到，默认归位“综合部”或“服务部”
        Department attributedDepartment = departmentRepository.findByDeptName(nlpResult.getRecommendedDepartment())
                .orElseGet(() -> departmentRepository.findByDeptName("服务部")
                        .orElseThrow(() -> new RuntimeException("系统基础数据异常：未找到预设部门")));

        // 3. 构建并保存实体
        CustomerFeedback feedback = new CustomerFeedback();
        feedback.setCustomerName(request.getCustomerName());
        feedback.setFeedbackContent(request.getFeedbackContent());

        // 使用 AI 实时计算出的分数和归因
        feedback.setSentimentScore(nlpResult.getSentimentScore());
        feedback.setDepartment(attributedDepartment);

        // ====================== 【恶意评价拦截逻辑】 ======================
        // 如果情感得分低于或等于 -0.8，触发人工审核拦截
        if (nlpResult.getSentimentScore().compareTo(new BigDecimal("-0.8")) <= 0) {
            feedback.setNeedsReview(true);         // 标记需要审核
            feedback.setReviewStatus("PENDING");   // 状态设为待定
        } else {
            feedback.setNeedsReview(false);        // 正常评价，无需审核
            feedback.setReviewStatus("APPROVED");  // 状态设为已通过
        }
        // ====================================================================

        feedback.setIsProcessed(false);
        feedback.setFeedbackTime(LocalDateTime.now());
        // 这里 hotelId 建议根据实际登录信息获取，暂时设为默认
        feedback.setHotelId("1");

        CustomerFeedback savedFeedback = feedbackRepository.save(feedback);

        // 4. 返回响应
        Map<String, Object> response = new HashMap<>();
        response.put("message", "评价已提交并完成 AI 分析");
        response.put("feedback_id", savedFeedback.getFeedbackId());
        response.put("analysis_result", Map.of(
                "score", nlpResult.getSentimentScore(),
                "department", attributedDepartment.getDeptName()
        ));

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * [GET] 获取指定部门的负面反馈明细
     * 路径示例: /api/v1/feedback/negative?deptId=1
     */
    @GetMapping("/negative")
    @Operation(summary = "查看某部门特定时间段的负面评论")
    public ResponseEntity<List<CustomerFeedback>> getNegativeFeedback(
            @RequestParam Long deptId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // 将 LocalDate 转换为当天开始和结束的 LocalDateTime
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<CustomerFeedback> negatives = feedbackRepository
                .findByDepartmentDeptIdAndSentimentScoreLessThanAndFeedbackTimeBetween(
                        deptId,
                        java.math.BigDecimal.ZERO,
                        start,
                        end
                );

        return negatives.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(negatives);
    }

    /**
     * 获取反馈列表
     */
    @GetMapping
    @Operation(summary = "获取反馈列表", description = "获取所有反馈的列表，支持分页和筛选")
    public ResponseEntity<Map<String, Object>> getFeedbackList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") String hotelId) {

        List<CustomerFeedback> feedbacks;

        // 根据状态筛选
        if ("pending".equalsIgnoreCase(status)) {
            feedbacks = feedbackRepository.findByHotelIdAndNeedsReviewTrue(hotelId);
        } else if ("processed".equalsIgnoreCase(status)) {
            feedbacks = feedbackRepository.findByHotelIdAndNeedsReviewFalse(hotelId);
        } else {
            feedbacks = feedbackRepository.findByHotelId(hotelId);
        }

        // 分页处理
        int startIndex = (page - 1) * size;
        int endIndex = Math.min(startIndex + size, feedbacks.size());
        List<CustomerFeedback> pagedFeedbacks = feedbacks.subList(startIndex, endIndex);

        // 转换为前端需要的格式
        List<Map<String, Object>> content = pagedFeedbacks.stream().map(f -> {
            Map<String, Object> map = new HashMap<>();
            map.put("feedbackId", f.getFeedbackId());
            map.put("customerName", f.getCustomerName());
            map.put("feedbackContent", f.getFeedbackContent());
            map.put("sentimentScore", f.getSentimentScore());
            map.put("sentimentScoreDisplay", calculateDisplayScore(f.getSentimentScore()));
            Map<String, String> deptMap = null;
            if (f.getDepartment() != null) {
                deptMap = new HashMap<>();
                deptMap.put("deptName", f.getDepartment().getDeptName());
            }
            map.put("department", deptMap);
            map.put("feedbackTime", f.getFeedbackTime());
            map.put("needsReview", f.getNeedsReview());
            map.put("reviewStatus", f.getReviewStatus());
            map.put("reviewComment", f.getReviewComment());
            return map;
        }).collect(Collectors.toList());

        // 返回分页信息
        Map<String, Object> response = new HashMap<>();
        response.put("content", content);
        response.put("totalElements", feedbacks.size());
        response.put("totalPages", (int) Math.ceil((double) feedbacks.size() / size));
        response.put("currentPage", page);
        response.put("size", size);
        response.put("numberOfElements", content.size());

        return ResponseEntity.ok(response);
    }

    /**
     * 计算显示用的评分（1-5星）
     */
    private int calculateDisplayScore(BigDecimal sentimentScore) {
        if (sentimentScore == null) return 3;
        double score = sentimentScore.doubleValue();
        // 将 -1~1 的情感得分转换为 1~5 的星级评分
        int stars = (int) Math.round((score + 1) * 2) + 1;
        return Math.max(1, Math.min(5, stars));
    }
}