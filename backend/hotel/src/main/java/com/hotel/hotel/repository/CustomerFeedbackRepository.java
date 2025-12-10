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
}