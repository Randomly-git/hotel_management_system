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
import java.util.Optional;

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
        // 统计日期为今天 (2025-12-14)
        LocalDate today = LocalDate.now();
        List<Department> departments = departmentRepository.findAll();

        for (Department dept : departments) {
            // 1. 获取所有归因到该部门且尚未处理的反馈
            // ⚠️ 最佳实践：建议同时筛选反馈时间，避免一次性处理历史所有未处理数据
            List<CustomerFeedback> unprocessedFeedback = feedbackRepository.findByDepartmentDeptIdAndIsProcessedFalse(dept.getDeptId());

            if (unprocessedFeedback.isEmpty()) {
                System.out.println("部门 [" + dept.getDeptName() + "] 无新增待处理反馈，跳过计算。");
                continue;
            }

            // 2. 核心计算逻辑：Σ(情感分×权重) / 总评价数 × 100
            BigDecimal sumNormalizedScore = BigDecimal.ZERO;
            int totalReviews = unprocessedFeedback.size();

            for (CustomerFeedback feedback : unprocessedFeedback) {
                // 将情感得分 [-1.0, 1.0] 归一化到 [0, 1.0] 区间
                BigDecimal normalizedScore = feedback.getSentimentScore()
                        .add(BigDecimal.ONE)
                        .divide(new BigDecimal("2.0"), 4, RoundingMode.HALF_UP);
                sumNormalizedScore = sumNormalizedScore.add(normalizedScore);
            }

            // 3. 计算绩效指数 ScoreIndex
            BigDecimal averageNormalizedScore = sumNormalizedScore
                    .divide(BigDecimal.valueOf(totalReviews), 4, RoundingMode.HALF_UP);

            BigDecimal scoreIndex = averageNormalizedScore
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);

            // 4. 判断预警等级
            String alertLevel = determineAlertLevel(scoreIndex);


            // ==========================================================
            // 5. 【核心修改】实现查找/更新 (UPSERT) 逻辑，解决 Duplicate Entry 错误
            // ==========================================================

            // 查找今日是否已存在该部门的绩效记录
            Optional<DepartmentPerformance> existingPerformanceOpt =
                    performanceRepository.findByDepartmentDeptIdAndStatisticsDate(dept.getDeptId(), today);

            DepartmentPerformance performance;
            String action;

            if (existingPerformanceOpt.isPresent()) {
                // 记录已存在，执行更新 (UPDATE)
                performance = existingPerformanceOpt.get();
                action = "更新";
            } else {
                // 记录不存在，执行插入 (INSERT)
                performance = new DepartmentPerformance();
                performance.setDepartment(dept); // 关联部门对象
                performance.setStatisticsDate(today); // 设置统计日期
                action = "创建";
            }

            // 6. 设置或更新绩效字段
            performance.setScoreIndex(scoreIndex);
            // 注意：此处 totalReviews 应是 *当天* 所有已处理和未处理的总和，
            // 这里我们简化为当前计算批次的总数。在生产环境可能需要单独查询当日全部反馈。
            performance.setTotalReviews(performance.getTotalReviews() + totalReviews);
            performance.setAlertLevel(alertLevel);

            // 7. 保存/更新绩效记录
            performanceRepository.save(performance);

            // 8. 标记已处理的反馈 (这步保持不变，无论更新还是插入，反馈都应该被标记)
            unprocessedFeedback.forEach(f -> f.setIsProcessed(true));
            feedbackRepository.saveAll(unprocessedFeedback);

            System.out.println(String.format("部门 [%s] 绩效指数计算%s完成: %.2f，预警等级: %s",
                    dept.getDeptName(), action, scoreIndex.doubleValue(), alertLevel));
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