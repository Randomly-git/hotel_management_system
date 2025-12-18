package com.hotel.hotel.service;

import com.hotel.hotel.entity.CustomerFeedback;
import com.hotel.hotel.entity.Department;
import com.hotel.hotel.entity.DepartmentPerformance;
import com.hotel.hotel.repository.CustomerFeedbackRepository;
import com.hotel.hotel.repository.DepartmentPerformanceRepository;
import com.hotel.hotel.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // 测试完成后自动回滚，不污染数据库
public class ReputationSystemTest {

    @Autowired
    private PerformanceCalculationService performanceService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private CustomerFeedbackRepository feedbackRepository;

    @Autowired
    private DepartmentPerformanceRepository performanceRepository;

    private final String TEST_HOTEL = "TEST_HOTEL_001";
    private Department testDept;

    @BeforeEach
    void setUp() {
        // 清理并初始化测试部门
        testDept = new Department(null, "测试部", new BigDecimal("1.0"), LocalDateTime.now(), TEST_HOTEL);
        testDept = departmentRepository.save(testDept);
    }

    @Test
    @DisplayName("场景1：模拟上传正常好评并计算分数")
    void testNormalFeedbackCalculation() {
        // 1. 模拟上传一条好评 (情感分 0.8)
        CustomerFeedback feedback = new CustomerFeedback();
        feedback.setCustomerName("张三");
        feedback.setFeedbackContent("服务非常好，房间很干净");
        feedback.setSentimentScore(new BigDecimal("0.8"));
        feedback.setDepartment(testDept);
        feedback.setHotelId(TEST_HOTEL);
        feedback.setFeedbackTime(LocalDateTime.now());
        feedback.setIsProcessed(false);
        feedback.setNeedsReview(false);
        feedbackRepository.save(feedback);

        // 2. 执行计算
        performanceService.calculateDailyPerformance(TEST_HOTEL);

        // 3. 断言验证
        Optional<DepartmentPerformance> result = performanceRepository
                .findByHotelIdAndDepartmentDeptIdAndStatisticsDate(TEST_HOTEL, testDept.getDeptId(), LocalDate.now());

        assertTrue(result.isPresent());
        // (0.8 + 1) / 2 * 100 = 90分
        assertEquals(new BigDecimal("90.00"), result.get().getScoreIndex());
        assertEquals("GREEN", result.get().getAlertLevel());
    }

    @Test
    @DisplayName("场景2：模拟恶意差评拦截（人工审核流程）")
    void testMaliciousFeedbackFiltering() {
        // 1. 模拟上传极端差评 (情感分 -0.9)
        CustomerFeedback extremeFeedback = new CustomerFeedback();
        extremeFeedback.setCustomerName("测试用户A");
        extremeFeedback.setFeedbackContent("太烂了！！！再也不来了！！");
        extremeFeedback.setSentimentScore(new BigDecimal("-0.9"));
        extremeFeedback.setDepartment(testDept);
        extremeFeedback.setHotelId(TEST_HOTEL);
        extremeFeedback.setFeedbackTime(LocalDateTime.now());
        extremeFeedback.setIsProcessed(false);
        feedbackRepository.save(extremeFeedback);

        // 2. 执行计算
        performanceService.calculateDailyPerformance(TEST_HOTEL);

        // 3. 验证结果
        CustomerFeedback savedFeedback = feedbackRepository.findById(extremeFeedback.getFeedbackId()).orElseThrow();

        // 验证：应被标记为需要审核
        assertTrue(savedFeedback.getNeedsReview(), "极端差评应自动触发人工审核");
        assertEquals("PENDING", savedFeedback.getReviewStatus());

        // 验证：该差评不应计入今日评分（因为无其他有效反馈，今日绩效应为空或跳过）
        Optional<DepartmentPerformance> perf = performanceRepository
                .findByHotelIdAndDepartmentDeptIdAndStatisticsDate(TEST_HOTEL, testDept.getDeptId(), LocalDate.now());
        assertFalse(perf.isPresent(), "若全是被拦截的评价，不应生成绩效记录");
    }

    @Test
    @DisplayName("场景3：边界警告与改进建议生成")
    void testLowScoreAlertAndSuggestions() {
        // 1. 模拟多条低分评价（但不触发-0.8的拦截线，例如 -0.5）
        for (int i = 0; i < 3; i++) {
            CustomerFeedback lowFeedback = new CustomerFeedback();
            lowFeedback.setCustomerName("匿名用户" + i);
            lowFeedback.setFeedbackContent("等太久了，体验不好");
            lowFeedback.setSentimentScore(new BigDecimal("-0.5"));
            lowFeedback.setDepartment(testDept);
            lowFeedback.setHotelId(TEST_HOTEL);
            lowFeedback.setFeedbackTime(LocalDateTime.now());
            lowFeedback.setIsProcessed(false);
            lowFeedback.setNeedsReview(false);
            feedbackRepository.save(lowFeedback);
        }

        // 2. 执行计算
        performanceService.calculateDailyPerformance(TEST_HOTEL);

        // 3. 验证结果
        DepartmentPerformance perf = performanceRepository
                .findByHotelIdAndDepartmentDeptIdAndStatisticsDate(TEST_HOTEL, testDept.getDeptId(), LocalDate.now())
                .orElseThrow();

        // (-0.5 + 1) / 2 * 100 = 25分
        assertTrue(perf.getScoreIndex().compareTo(new BigDecimal("70")) < 0);
        assertEquals("RED", perf.getAlertLevel(), "低分应触发红色预警");
        assertNotNull(perf.getImprovementSuggestions(), "红色预警应伴随改进建议");
    }
}