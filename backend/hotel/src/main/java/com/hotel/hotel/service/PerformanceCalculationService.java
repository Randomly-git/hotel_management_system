package com.hotel.hotel.service;

import com.hotel.hotel.entity.CustomerFeedback;
import com.hotel.hotel.entity.Department;
import com.hotel.hotel.entity.DepartmentPerformance;
import com.hotel.hotel.repository.CustomerFeedbackRepository;
import com.hotel.hotel.repository.DepartmentPerformanceRepository;
import com.hotel.hotel.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class PerformanceCalculationService {

    private final DepartmentRepository departmentRepository;
    private final CustomerFeedbackRepository feedbackRepository;
    private final DepartmentPerformanceRepository performanceRepository;

    @Autowired
    public PerformanceCalculationService(
            DepartmentRepository departmentRepository,
            CustomerFeedbackRepository feedbackRepository,
            DepartmentPerformanceRepository performanceRepository) {
        this.departmentRepository = departmentRepository;
        this.feedbackRepository = feedbackRepository;
        this.performanceRepository = performanceRepository;
    }

    /**
     * 【核心任务】每日定时调用，计算并保存所有部门的绩效指数
     */
    @Transactional
    public void calculateDailyPerformance() {
        LocalDate today = LocalDate.now();
        List<Department> departments = departmentRepository.findAll();

        for (Department dept : departments) {
            // 1. 获取所有归因到该部门且尚未处理的反馈
            // 为了简化，我们获取所有未处理的，生产环境应按日期筛选
            List<CustomerFeedback> unprocessedFeedback = feedbackRepository.findByDepartmentDeptIdAndIsProcessedFalse(dept.getDeptId());

            if (unprocessedFeedback.isEmpty()) {
                System.out.println("部门 [" + dept.getDeptName() + "] 今日无新增待处理反馈，跳过计算。");
                continue;
            }

            // 2. 核心计算逻辑：Σ(情感分×权重) / 总评价数 × 100
            // ⚠️ 注释：根据需求文档中的预警机制 (得分<80/70分)，我们采用【情感分归一化后的平均值×100】作为绩效指数，以确保得分在 0-100 范围内。
            // 部门权重 (dept.getWeight()) 暂时用于未来计算【酒店整体口碑指数】时使用。

            BigDecimal sumNormalizedScore = BigDecimal.ZERO;
            int totalReviews = unprocessedFeedback.size();

            for (CustomerFeedback feedback : unprocessedFeedback) {
                // 将情感得分 [-1.0, 1.0] 归一化到 [0, 1.0] 区间
                // Normalized_Score = (SentimentScore + 1.0) / 2.0
                BigDecimal normalizedScore = feedback.getSentimentScore()
                        .add(BigDecimal.ONE)
                        .divide(new BigDecimal("2.0"), 4, RoundingMode.HALF_UP);

                // 累加归一化后的得分 (根据业务公式，这里应是 Σ(情感分))
                // ⚠️ 如果严格遵循公式：Σ(情感分×权重) / 总评价数
                // 这里的 "权重" 指的是部门的权重。我们暂时不乘部门权重，避免分数不合理。
                sumNormalizedScore = sumNormalizedScore.add(normalizedScore);
            }

            // 3. 计算绩效指数 ScoreIndex
            BigDecimal averageNormalizedScore = sumNormalizedScore
                    .divide(BigDecimal.valueOf(totalReviews), 4, RoundingMode.HALF_UP);

            // ScoreIndex = Average Normalized Score * 100
            BigDecimal scoreIndex = averageNormalizedScore
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);

            // 4. 判断预警等级
            String alertLevel = determineAlertLevel(scoreIndex);

            // 5. 保存绩效记录
            DepartmentPerformance performance = new DepartmentPerformance();
            performance.setDepartment(dept);
            performance.setScoreIndex(scoreIndex);
            performance.setTotalReviews(totalReviews);
            performance.setAlertLevel(alertLevel);
            performance.setStatisticsDate(today);

            performanceRepository.save(performance);

            // 6. 标记已处理的反馈
            unprocessedFeedback.forEach(f -> f.setIsProcessed(true));
            feedbackRepository.saveAll(unprocessedFeedback);

            System.out.println(String.format("部门 [%s] 绩效指数计算完成: %.2f，预警等级: %s",
                    dept.getDeptName(), scoreIndex.doubleValue(), alertLevel));
        }
    }

    /**
     * 辅助方法：根据绩效指数判断预警等级
     * @param score 绩效指数
     * @return 预警等级字符串
     */
    private String determineAlertLevel(BigDecimal score) {
        if (score.compareTo(new BigDecimal("70")) < 0) {
            // 部门得分 < 70分：红色预警，需整改
            return "RED";
        } else if (score.compareTo(new BigDecimal("80")) < 0) {
            // 部门得分 < 80分：黄色预警，需关注
            return "YELLOW";
        } else {
            return "NORMAL";
        }
    }
}