package com.hotel.hotel.repository;

import com.hotel.hotel.entity.CustomerFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface CustomerFeedbackRepository extends JpaRepository<CustomerFeedback, Long> {

    /**
     * 查询某个部门在某个时间段内未处理的反馈 (用于绩效计算)
     */
    List<CustomerFeedback> findByDepartmentDeptIdAndIsProcessedFalseAndFeedbackTimeBetween(
            Long deptId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    /** * 查询指定部门所有未处理的反馈（不限时间）
     */
    List<CustomerFeedback> findByDepartmentDeptIdAndIsProcessedFalse(Long deptId);

    /**
     * 查询所有未处理的反馈
     */
    List<CustomerFeedback> findByIsProcessedFalse();

    /**
     * 核心计算：获取指定酒店、指定部门、且不需要人工审核（或已通过）的有效反馈
     */
    List<CustomerFeedback> findByHotelIdAndDepartmentDeptIdAndIsProcessedFalseAndNeedsReviewFalse(
            String hotelId, Long deptId);

    /**
     * 恶意评论防控：统计指定IP在特定时间后的提交次数
     */
    long countByIpAddressAndFeedbackTimeAfter(String ipAddress, LocalDateTime time);

    /**
     * 反馈审核接口查询：获取待审核的反馈列表
     */
    List<CustomerFeedback> findByHotelIdAndNeedsReviewTrueAndReviewStatus(String hotelId, String reviewStatus);

    /**
     * 预扫描使用：获取指定酒店、部门下所有未处理的原始反馈
     */
    List<CustomerFeedback> findByHotelIdAndDepartmentDeptIdAndIsProcessedFalse(String hotelId, Long deptId);
}