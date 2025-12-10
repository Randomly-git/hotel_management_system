package com.hotel.hotel.controller;

import com.hotel.hotel.dto.FeedbackRequest;
import com.hotel.hotel.dto.NlpResult;
import com.hotel.hotel.entity.CustomerFeedback;
import com.hotel.hotel.entity.Department;
import com.hotel.hotel.repository.CustomerFeedbackRepository;
import com.hotel.hotel.service.NlpIntegrationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/feedback") // 建议使用版本化接口
public class FeedbackController {

    private final NlpIntegrationService nlpService;
    private final CustomerFeedbackRepository feedbackRepository;

    @Autowired
    public FeedbackController(NlpIntegrationService nlpService, CustomerFeedbackRepository feedbackRepository) {
        this.nlpService = nlpService;
        this.feedbackRepository = feedbackRepository;
    }

    /**
     * [POST] 提交客户反馈接口
     * 接口路径: /api/v1/feedback
     *
     * @param request 包含客户昵称和反馈内容的请求体
     * @return 包含处理结果的响应
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> submitFeedback(@Valid @RequestBody FeedbackRequest request) {

        // 1. 调用 NLP 服务进行分析和归因
        NlpResult nlpResult = nlpService.analyzeFeedback(request.getFeedbackContent());

        // 2. 根据归因的部门名称查找 Department 实体
        // 这一步会确保归因的部门在数据库中存在
        Department attributedDepartment = nlpService.findDepartmentByName(nlpResult.getAttributedDeptName());

        // 3. 构建 CustomerFeedback 实体
        CustomerFeedback feedback = new CustomerFeedback();
        feedback.setCustomerName(request.getCustomerName());
        feedback.setFeedbackContent(request.getFeedbackContent());
        feedback.setSentimentScore(nlpResult.getSentimentScore());
        feedback.setDepartment(attributedDepartment); // 关联部门
        feedback.setIsProcessed(false); // 标记为未处理，等待 PerformanceCalculationService 统一处理
        feedback.setFeedbackTime(LocalDateTime.now()); // 记录接收时间

        // 4. 保存到数据库
        CustomerFeedback savedFeedback = feedbackRepository.save(feedback);

        // 5. 构造响应，返回分析结果供调用方参考
        Map<String, Object> response = new HashMap<>();
        response.put("message", "反馈提交成功，已完成实时NLP分析并入库。");
        response.put("feedbackId", savedFeedback.getFeedbackId());
        response.put("sentimentScore", nlpResult.getSentimentScore());
        response.put("attributedDepartment", attributedDepartment.getDeptName());

        // 返回 HTTP 201 Created (资源创建成功)
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}