package com.hotel.hotel.service;

import com.hotel.hotel.entity.DepartmentFeedback;
import com.hotel.hotel.entity.DepartmentTask;
import com.hotel.hotel.entity.TaskOrder;
import com.hotel.hotel.entity.Department;
import com.hotel.hotel.repository.DepartmentFeedbackRepository;
import com.hotel.hotel.repository.DepartmentTaskRepository;
import com.hotel.hotel.repository.TaskOrderRepository;
import com.hotel.hotel.repository.DepartmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 部门评价服务类
 */
@Slf4j
@Service
public class DepartmentFeedbackService {

    @Autowired
    private DepartmentFeedbackRepository feedbackRepository;

    @Autowired
    private TaskOrderRepository taskOrderRepository;

    @Autowired
    private DepartmentTaskRepository departmentTaskRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    /**
     * 创建客户评价
     */
    @Transactional
    public DepartmentFeedback createFeedback(FeedbackRequest request) {
        log.info("创建客户评价开始: taskOrderId={}", request.getTaskOrderId());

        // 验证任务单是否存在
        TaskOrder taskOrder = taskOrderRepository.findById(request.getTaskOrderId())
                .orElseThrow(() -> new RuntimeException("任务单不存在: " + request.getTaskOrderId()));

        // 检查任务单是否已完成
        if (!"COMPLETED".equals(taskOrder.getStatus())) {
            throw new RuntimeException("只能对已完成的任务进行评价");
        }

        // 检查是否已经评价过
        Optional<DepartmentFeedback> existingFeedback = feedbackRepository.findByTaskOrderId(request.getTaskOrderId());
        if (existingFeedback.isPresent()) {
            throw new RuntimeException("该任务已经评价过了");
        }

        // 查找部门任务信息
        Optional<DepartmentTask> deptTaskOpt = departmentTaskRepository.findById(request.getDepartmentTaskId());
        DepartmentTask departmentTask = deptTaskOpt.orElseThrow(
                () -> new RuntimeException("部门任务不存在: " + request.getDepartmentTaskId()));

        // 获取部门名称
        String departmentName = "未知部门";
        Optional<Department> deptOpt = departmentRepository.findByDeptId(departmentTask.getDepartmentId());
        if (deptOpt.isPresent()) {
            departmentName = deptOpt.get().getDeptName();
        }

        // 创建评价记录
        DepartmentFeedback feedback = DepartmentFeedback.builder()
                .taskOrderId(request.getTaskOrderId())
                .departmentTaskId(request.getDepartmentTaskId())
                .customerId(taskOrder.getGuestMemberId())
                .hotelId(taskOrder.getHotelId())
                .departmentId(departmentTask.getDepartmentId())
                .departmentName(departmentName)
                .serviceRating(request.getServiceRating())
                .responseSpeedRating(request.getResponseSpeedRating())
                .serviceQualityRating(request.getServiceQualityRating())
                .feedbackContent(request.getFeedbackContent())
                .feedbackTags(request.getFeedbackTags())
                .isRecommended(request.getIsRecommended())
                .build();

        DepartmentFeedback savedFeedback = feedbackRepository.save(feedback);
        log.info("客户评价创建成功: feedbackId={}, overallRating={}",
                savedFeedback.getFeedbackId(), savedFeedback.getOverallRating());

        return savedFeedback;
    }

    /**
     * 获取客户评价详情
     */
    public Optional<DepartmentFeedback> getFeedbackByTaskOrderId(Long taskOrderId) {
        return feedbackRepository.findByTaskOrderId(taskOrderId);
    }

    /**
     * 获取部门评价统计
     */
    public Map<String, Object> getDepartmentFeedbackStatistics(Long hotelId, Long departmentId) {
        Map<String, Object> statistics = new HashMap<>();

        // 平均评分
        BigDecimal avgRating = feedbackRepository.findAverageRatingByHotelAndDepartment(hotelId, departmentId);
        statistics.put("averageRating", avgRating != null ? avgRating : BigDecimal.ZERO);

        // 评价总数
        Long totalCount = feedbackRepository.countByHotelAndDepartment(hotelId, departmentId);
        statistics.put("totalCount", totalCount != null ? totalCount : 0L);

        // 推荐数量
        Long recommendedCount = feedbackRepository.countRecommendedByHotelAndDepartment(hotelId, departmentId);
        statistics.put("recommendedCount", recommendedCount != null ? recommendedCount : 0L);

        // 推荐率
        if (totalCount != null && totalCount > 0 && recommendedCount != null) {
            BigDecimal recommendationRate = BigDecimal.valueOf(recommendedCount.doubleValue() / totalCount.doubleValue())
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            statistics.put("recommendationRate", recommendationRate);
        } else {
            statistics.put("recommendationRate", BigDecimal.ZERO);
        }

        return statistics;
    }

    /**
     * 获取酒店整体评价统计
     */
    public Map<String, Object> getHotelFeedbackStatistics(Long hotelId) {
        Map<String, Object> statistics = new HashMap<>();

        List<DepartmentFeedback> allFeedbacks = feedbackRepository.findByHotelId(hotelId);

        if (allFeedbacks.isEmpty()) {
            statistics.put("totalFeedbacks", 0);
            statistics.put("averageRating", BigDecimal.ZERO);
            statistics.put("recommendationRate", BigDecimal.ZERO);
            return statistics;
        }

        // 总评价数
        statistics.put("totalFeedbacks", allFeedbacks.size());

        // 平均评分
        BigDecimal totalRating = allFeedbacks.stream()
                .map(DepartmentFeedback::getOverallRating)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgRating = totalRating.divide(BigDecimal.valueOf(allFeedbacks.size()),
                2, java.math.RoundingMode.HALF_UP);
        statistics.put("averageRating", avgRating);

        // 推荐率
        long recommendedCount = allFeedbacks.stream()
                .mapToLong(f -> f.getIsRecommended() ? 1 : 0)
                .sum();
        BigDecimal recommendationRate = BigDecimal.valueOf(recommendedCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(allFeedbacks.size()),
                        2, java.math.RoundingMode.HALF_UP);
        statistics.put("recommendationRate", recommendationRate);

        // 低评分评价（需要关注）
        List<DepartmentFeedback> lowRatings = feedbackRepository.findLowRatingFeedbackByHotel(hotelId);
        statistics.put("lowRatingCount", lowRatings.size());

        // 高评分评价
        List<DepartmentFeedback> highRatings = feedbackRepository.findHighRatingFeedbackByHotel(hotelId);
        statistics.put("highRatingCount", highRatings.size());

        return statistics;
    }

    /**
     * 获取客户评价历史
     */
    public List<DepartmentFeedback> getCustomerFeedbackHistory(String customerId, Long hotelId) {
        return feedbackRepository.findByHotelId(hotelId).stream()
                .filter(feedback -> customerId.equals(feedback.getCustomerId()))
                .sorted((f1, f2) -> f2.getCreatedAt().compareTo(f1.getCreatedAt()))
                .toList();
    }

    /**
     * 评价请求DTO
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FeedbackRequest {
        private Long taskOrderId;
        private Long departmentTaskId;
        private Integer serviceRating;        // 1-5分
        private Integer responseSpeedRating; // 1-5分
        private Integer serviceQualityRating;// 1-5分
        private String feedbackContent;
        private String feedbackTags;          // 用逗号分隔
        private Boolean isRecommended;
    }
}