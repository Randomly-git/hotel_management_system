package com.hotel.hotel.service;

import com.hotel.hotel.dto.NlpResult;
import com.hotel.hotel.entity.Department;
import com.hotel.hotel.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

@Service
public class NlpIntegrationService {

    private final DepartmentRepository departmentRepository;
    private final Random random = new Random();

    // 推荐使用构造器注入
    @Autowired
    public NlpIntegrationService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    /**
     * 核心方法：对客户反馈内容进行语义分析和部门归因
     *
     * @param feedbackContent 客户的原始反馈文本
     * @return NlpResult 包含情感得分和归因部门名称
     */
    public NlpResult analyzeFeedback(String feedbackContent) {
        // --- 1. 模拟情感分析 (Sentiment Analysis) ---
        // 模拟生成一个范围在 [-0.5, 1.0] 的情感分数，代表中性到好评
        double score = -0.5 + (1.5 * random.nextDouble());
        BigDecimal sentimentScore = BigDecimal.valueOf(score)
                .setScale(4, RoundingMode.HALF_UP);

        // --- 2. 模拟部门归因 (Department Attribution) ---
        String deptName = simulateDepartmentAttribution(feedbackContent);

        NlpResult result = new NlpResult();
        result.setSentimentScore(sentimentScore);
        result.setAttributedDeptName(deptName);

        return result;
    }

    /**
     * 辅助方法：基于关键词模拟部门归因
     */
    private String simulateDepartmentAttribution(String content) {
        String lowerContent = content.toLowerCase();

        // 关键词匹配，确定归因部门
        if (lowerContent.contains("房间") || lowerContent.contains("床") || lowerContent.contains("打扫") || lowerContent.contains("安静")) {
            return "房务部";
        }
        if (lowerContent.contains("前台") || lowerContent.contains("服务员") || lowerContent.contains("态度") || lowerContent.contains("入住")) {
            return "服务部";
        }
        if (lowerContent.contains("餐厅") || lowerContent.contains("早餐") || lowerContent.contains("咖啡") || lowerContent.contains("食物")) {
            return "餐饮部";
        }
        if (lowerContent.contains("空调") || lowerContent.contains("水管") || lowerContent.contains("修理") || lowerContent.contains("灯")) {
            return "工程部";
        }

        // 默认归因给服务部（或根据业务设定为综合部）
        return "服务部";
    }

    /**
     * 根据部门名称获取部门实体
     * @param deptName 部门名称
     * @return Department 实体
     */
    public Department findDepartmentByName(String deptName) {
        // 假设初始数据中一定有这些部门
        return departmentRepository.findByDeptName(deptName)
                .orElseThrow(() -> new RuntimeException("找不到部门: " + deptName));
    }
}