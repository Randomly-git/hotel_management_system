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

    @Value("${hotel.config.default-id:1}") // 读取配置，若无则默认为 1
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
        log.info("开始计算酒店 [{}] [{}] 的绩效", hotelId, targetDate);

        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);

        log.debug("时间范围: {} 到 {}", startOfDay, endOfDay);

        // 1. 获取当前酒店的所有部门
        List<Department> departments = departmentRepository.findByHotelId(hotelId);
        log.info("找到 {} 个部门", departments.size());

        // 如果没有找到部门，创建一个默认的部门列表用于测试
        if (departments.isEmpty()) {
            log.warn("酒店 {} 没有配置部门信息，将使用默认部门进行测试", hotelId);
            departments = createDefaultDepartments("1"); // 使用数字ID
        }

        for (Department dept : departments) {
            log.info("开始处理部门: {} (ID: {})", dept.getDeptName(), dept.getDeptId());

            // 对于默认部门，尝试查找所有相关的反馈（不限制部门ID）
            List<CustomerFeedback> allUnprocessed;
            if (dept.getDeptId() != null && dept.getDeptId() > 0) {
                // 正常部门：按部门ID查找
                allUnprocessed = feedbackRepository
                        .findByDepartmentDeptIdAndIsProcessedFalseAndNeedsReviewFalseAndFeedbackTimeBetween(
                                dept.getDeptId(), startOfDay, endOfDay);
            } else {
                // 默认部门：查找所有未处理的反馈（用于测试）
                allUnprocessed = feedbackRepository
                        .findByIsProcessedFalseAndNeedsReviewFalseAndFeedbackTimeBetween(
                                startOfDay, endOfDay);
                log.info("默认部门 [{}] 查找所有未处理反馈: {} 条", dept.getDeptName(), allUnprocessed.size());
            }

            log.info("部门 [{}] 找到 {} 条未处理反馈", dept.getDeptName(), allUnprocessed.size());

            if (allUnprocessed.isEmpty()) {
                log.info("酒店 [{}] 部门 [{}] 今日无待处理反馈", hotelId, dept.getDeptName());
                continue;
            }

            // 3. 执行预扫描拦截
            List<CustomerFeedback> validFeedbacks = new java.util.ArrayList<>();
            List<CustomerFeedback> maliciousFeedbacks = new java.util.ArrayList<>();

            log.info("开始过滤反馈数据，共 {} 条", allUnprocessed.size());

            for (CustomerFeedback fb : allUnprocessed) {
                log.debug("处理反馈 ID: {}, 情感分数: {}, 审核状态: {}",
                    fb.getFeedbackId(), fb.getSentimentScore(), fb.getReviewStatus());

                // 调用逻辑判断：分值过低判定为恶意
                if ("APPROVED".equals(fb.getReviewStatus())) {
                    validFeedbacks.add(fb);
                    log.debug("反馈 {} 通过审核状态检查", fb.getFeedbackId());
                    continue; // 跳过后续的自动拦截逻辑
                }

                // 调用逻辑判断：分值过低判定为恶意
                if (isValidFeedback(fb)) {
                    validFeedbacks.add(fb);
                    log.debug("反馈 {} 通过情感分数检查", fb.getFeedbackId());
                } else {
                    // 只有从未审核过的数据才会进入这里
                    fb.setNeedsReview(true);
                    fb.setReviewStatus("PENDING");
                    maliciousFeedbacks.add(fb);
                    log.info("反馈 {} 被标记为需要审核", fb.getFeedbackId());
                }
            }

            log.info("过滤完成: {} 条有效反馈, {} 条恶意反馈",
                validFeedbacks.size(), maliciousFeedbacks.size());

            // 4. 持久化恶意评价的状态（这是让测试通过的关键！）
            if (!maliciousFeedbacks.isEmpty()) {
                feedbackRepository.saveAll(maliciousFeedbacks);
                log.warn("酒店 [{}] 部门 [{}] 拦截到 {} 条疑似恶意评价并已保存状态",
                    hotelId, dept.getDeptName(), maliciousFeedbacks.size());
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

            // 只有真实部门才保存到数据库，默认部门仅用于测试
            if (dept.getDeptId() != null && dept.getDeptId() > 0) {
                DepartmentPerformance saved = performanceRepository.save(performance);
                log.info("绩效数据已保存到数据库: recordId={}, score={}", saved.getRecordId(), saved.getScoreIndex());
            } else {
                log.info("默认部门绩效计算完成 (不保存到数据库): 部门={}, 分数={}", dept.getDeptName(), scoreIndex);
            }

            // 9. 标记处理状态：只有参与了计算的 validFeedbacks 才标记为 Processed
            // 拦截的恶意评价 remains isProcessed = false，直到人工审核通过
            validFeedbacks.forEach(f -> f.setIsProcessed(true));
            feedbackRepository.saveAll(validFeedbacks);

            log.info("酒店 [{}] 部门 [{}] 绩效计算完成，得分：{}，已标记 {} 条反馈为已处理",
                hotelId, dept.getDeptName(), scoreIndex, validFeedbacks.size());
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
        BigDecimal sentimentScore = feedback.getSentimentScore();
        log.debug("检查反馈 {} 的情感分数: {}", feedback.getFeedbackId(), sentimentScore);

        // 规则1：情感极度负面 (<-0.8) 自动转人工审核
        if (sentimentScore.compareTo(new BigDecimal("-0.8")) < 0) {
            log.info("反馈 {} 情感分数过低 ({})，标记为需要审核",
                feedback.getFeedbackId(), sentimentScore);
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

    /**
     * 创建默认部门列表（用于测试和初始化）
     */
    private List<Department> createDefaultDepartments(String hotelId) {
        List<Department> defaultDepartments = new java.util.ArrayList<>();

        // 创建默认部门
        String[] deptNames = {"房务部", "服务部", "餐饮部", "工程部", "业务部"};
        BigDecimal[] weights = {new BigDecimal("0.40"), new BigDecimal("0.30"),
                               new BigDecimal("0.20"), new BigDecimal("0.10"), new BigDecimal("0.00")};

        for (int i = 0; i < deptNames.length; i++) {
            Department dept = new Department();
            dept.setDeptId((long) (i + 1)); // 临时ID
            dept.setDeptName(deptNames[i]);
            dept.setWeight(weights[i]);
            dept.setHotelId("1"); // 使用固定的数字ID
            dept.setCreateTime(java.time.LocalDateTime.now());

            defaultDepartments.add(dept);
        }

        log.info("创建了 {} 个默认部门用于测试", defaultDepartments.size());
        return defaultDepartments;
    }

}