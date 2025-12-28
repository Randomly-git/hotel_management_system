package com.hotel.hotel.service;

import com.hotel.hotel.entity.CustomerFeedback;
import com.hotel.hotel.entity.Department;
import com.hotel.hotel.entity.DepartmentPerformance;
import com.hotel.hotel.repository.CustomerFeedbackRepository;
import com.hotel.hotel.repository.DepartmentPerformanceRepository;
import com.hotel.hotel.repository.DepartmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PerformanceCalculationService {

    private final DepartmentRepository departmentRepository;
    private final CustomerFeedbackRepository feedbackRepository;
    private final DepartmentPerformanceRepository performanceRepository;
    // 注入成员B实现的NLP服务，用于获取实时意图或验证情感
    private final ZhipuNlpService nlpService;

    @Value("${hotel.config.default-id:DEFAULT_HOTEL}") // 读取配置，若无则默认为 DEFAULT_HOTEL
    private String defaultHotelId;

    @Autowired
    public PerformanceCalculationService(
            DepartmentRepository departmentRepository,
            CustomerFeedbackRepository feedbackRepository,
            DepartmentPerformanceRepository performanceRepository,
            ZhipuNlpService nlpService) {
        this.departmentRepository = departmentRepository;
        this.feedbackRepository = feedbackRepository;
        this.performanceRepository = performanceRepository;
        this.nlpService = nlpService;
    }

    /**
     * 缺省调用方法：由定时任务调用，使用预定义的默认酒店ID
     */
    @Transactional
    public void calculateDailyPerformance() {
        log.info("触发缺省绩效计算任务，使用默认酒店ID: {}", defaultHotelId);
        LocalDate today = LocalDate.now();
        this.calculateDailyPerformance(defaultHotelId,today);
    }

    @Transactional
    public void calculateDailyPerformance(String hotelId, LocalDate targetDate) {
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);

        // 1. 获取当前酒店的所有部门
        List<Department> departments = departmentRepository.findByHotelId(hotelId);

        for (Department dept : departments) {

            // 2. 获取该部门下未处理的反馈（不包括正在被审核或者已经被拒绝的恶意评论）
            List<CustomerFeedback> allUnprocessed = feedbackRepository
                    .findByDepartmentDeptIdAndIsProcessedFalseAndNeedsReviewFalseAndFeedbackTimeBetween(
                            dept.getDeptId(), startOfDay, endOfDay);

            if (allUnprocessed.isEmpty()) {
                log.info("酒店 [{}] 部门 [{}] 今日无待处理反馈", hotelId, dept.getDeptName());
                continue;
            }

            // 3. 执行预扫描拦截
            List<CustomerFeedback> validFeedbacks = new java.util.ArrayList<>();
            List<CustomerFeedback> maliciousFeedbacks = new java.util.ArrayList<>();

            for (CustomerFeedback fb : allUnprocessed) {
                // 调用逻辑判断：分值过低判定为恶意
                if ("APPROVED".equals(fb.getReviewStatus())) {
                    validFeedbacks.add(fb);
                    continue; // 跳过后续的自动拦截逻辑
                }

                // 调用逻辑判断：分值过低判定为恶意
                if (isValidFeedback(fb)) {
                    validFeedbacks.add(fb);
                } else {
                    // 只有从未审核过的数据才会进入这里
                    fb.setNeedsReview(true);
                    fb.setReviewStatus("PENDING");
                    maliciousFeedbacks.add(fb);
                }
            }

            // 4. 持久化恶意评价的状态（这是让测试通过的关键！）
            if (!maliciousFeedbacks.isEmpty()) {
                feedbackRepository.saveAll(maliciousFeedbacks);
                log.warn("酒店 [{}] 部门 [{}] 拦截到 {} 条疑似恶意评价", hotelId, dept.getDeptName(), maliciousFeedbacks.size());
            }

            // 5. 判断过滤后是否有有效样本进行绩效计算
            if (validFeedbacks.isEmpty()) {
                log.info("酒店 [{}] 部门 [{}] 过滤后无有效样本", hotelId, dept.getDeptName());
                continue;
            }

            // 6. 算法计算（仅使用 validFeedbacks）
            BigDecimal scoreIndex = calculateWeightedScore(validFeedbacks);
            String alertLevel = determineAlertLevel(scoreIndex);

            // 7. UPSERT 绩效记录
            DepartmentPerformance performance = performanceRepository
                    .findByHotelIdAndDepartmentDeptIdAndStatisticsDate(hotelId, dept.getDeptId(), targetDate)
                    .orElse(new DepartmentPerformance());

            performance.setDepartment(dept);
            performance.setStatisticsDate(targetDate);
            performance.setScoreIndex(scoreIndex);
            performance.setTotalReviews(validFeedbacks.size());
            performance.setAlertLevel(alertLevel);
            performance.setHotelId(hotelId);
            performance.setTrendStatus(calculateTrendWithHotel(hotelId, dept.getDeptId(), scoreIndex, targetDate));

            // 8. 触发改进建议（当分数低于75时）
            if (scoreIndex.compareTo(new BigDecimal("75")) < 0) {
                performance.setImprovementSuggestions(generateImprovementSuggestions(validFeedbacks));
            }

            performanceRepository.save(performance);

            // 9. 标记处理状态：只有参与了计算的 validFeedbacks 才标记为 Processed
            // 拦截的恶意评价 remains isProcessed = false，直到人工审核通过
            validFeedbacks.forEach(f -> f.setIsProcessed(true));
            feedbackRepository.saveAll(validFeedbacks);

            log.info("酒店 [{}] 部门 [{}] 绩效计算完成，得分：{}", hotelId, dept.getDeptName(), scoreIndex);
        }
    }

    /**
     * 辅助方法：支持租户隔离的趋势计算
     */
    private String calculateTrendWithHotel(String hotelId, Long deptId, BigDecimal currentScore, LocalDate today) {
        return performanceRepository
                .findTopByHotelIdAndDepartmentDeptIdAndStatisticsDateBeforeOrderByStatisticsDateDesc(hotelId, deptId, today)
                .map(prev -> currentScore.compareTo(prev.getScoreIndex()) >= 0 ? "UP" : "DOWN")
                .orElse("STABLE");
    }

    /**
     * 恶意评论过滤逻辑
     */
    private boolean isValidFeedback(CustomerFeedback feedback) {
        // 规则1：情感极度负面 (<-0.8) 自动转人工审核
        if (feedback.getSentimentScore().compareTo(new BigDecimal("-0.8")) < 0) {
            feedback.setNeedsReview(true);
            feedback.setReviewStatus("PENDING");
            return false;
        }

        // 规则2：简单频率检查 (实际开发中应从Redis或DB查询该IP近1小时提交数)
        // if (checkIpFrequency(feedback.getIpAddress())) { ... }

        return true;
    }

    /**
     * 加权评分算法：将情感分归一化并计算平均值
     */
    private BigDecimal calculateWeightedScore(List<CustomerFeedback> feedbacks) {
        BigDecimal sum = feedbacks.stream()
                .map(f -> f.getSentimentScore().add(BigDecimal.ONE)
                        .divide(new BigDecimal("2.0"), 4, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(BigDecimal.valueOf(feedbacks.size()), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 判定预警等级 (RED < 70, YELLOW < 80)
     */
    private String determineAlertLevel(BigDecimal score) {
        if (score.compareTo(new BigDecimal("70")) < 0) return "RED";
        if (score.compareTo(new BigDecimal("80")) < 0) return "YELLOW";
        return "GREEN";
    }

    /**
     * 趋势计算：对比昨日分数
     */
    private String calculateTrend(Long deptId, BigDecimal currentScore, LocalDate today) {
        return performanceRepository.findByDepartmentDeptIdAndStatisticsDate(deptId, today.minusDays(1))
                .map(prev -> currentScore.compareTo(prev.getScoreIndex()) >= 0 ? "UP" : "DOWN")
                .orElse("STABLE");
    }

    /**
     * 调用成员 B 的 ZhipuNlpService 生成改进建议
     */
    private String generateImprovementSuggestions(List<CustomerFeedback> feedbacks) {
        // 1. 提取所有负面反馈内容 (情感分 < 0)
        String combinedComments = feedbacks.stream()
                .filter(f -> f.getSentimentScore().compareTo(BigDecimal.ZERO) < 0)
                .map(f -> "- " + f.getFeedbackContent())
                .collect(Collectors.joining("\n"));

        // 2. 如果没有负面内容，直接返回
        if (combinedComments.isEmpty()) {
            return "今日暂无负面反馈，请继续保持优秀的服务水平。";
        }

        // 3. 调用成员 B 的服务中的方法
        return nlpService.generateManagementSuggestions(combinedComments);
    }

}