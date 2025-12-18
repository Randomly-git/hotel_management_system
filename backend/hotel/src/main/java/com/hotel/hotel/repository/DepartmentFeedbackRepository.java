package com.hotel.hotel.repository;

import com.hotel.hotel.entity.DepartmentFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 部门评价数据访问接口
 */
@Repository
public interface DepartmentFeedbackRepository extends JpaRepository<DepartmentFeedback, Long> {

    /**
     * 根据任务单ID查找评价
     */
    Optional<DepartmentFeedback> findByTaskOrderId(Long taskOrderId);

    /**
     * 根据部门任务ID查找评价
     */
    Optional<DepartmentFeedback> findByDepartmentTaskId(Long departmentTaskId);

    /**
     * 根据客户ID查找所有评价
     */
    List<DepartmentFeedback> findByCustomerId(String customerId);

    /**
     * 根据酒店ID查找所有评价
     */
    List<DepartmentFeedback> findByHotelId(Long hotelId);

    /**
     * 根据部门ID查找评价
     */
    List<DepartmentFeedback> findByDepartmentId(Long departmentId);

    /**
     * 根据酒店ID和部门ID查找评价
     */
    List<DepartmentFeedback> findByHotelIdAndDepartmentId(Long hotelId, Long departmentId);

    /**
     * 查找指定时间段内的评价
     */
    @Query("SELECT f FROM DepartmentFeedback f WHERE f.hotelId = :hotelId AND f.createdAt BETWEEN :startTime AND :endTime")
    List<DepartmentFeedback> findByHotelIdAndCreatedAtBetween(
            @Param("hotelId") Long hotelId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 计算部门的平均评分
     */
    @Query("SELECT AVG(f.overallRating) FROM DepartmentFeedback f WHERE f.hotelId = :hotelId AND f.departmentId = :departmentId")
    BigDecimal findAverageRatingByHotelAndDepartment(
            @Param("hotelId") Long hotelId,
            @Param("departmentId") Long departmentId);

    /**
     * 统计部门评价数量
     */
    @Query("SELECT COUNT(f) FROM DepartmentFeedback f WHERE f.hotelId = :hotelId AND f.departmentId = :departmentId")
    Long countByHotelAndDepartment(
            @Param("hotelId") Long hotelId,
            @Param("departmentId") Long departmentId);

    /**
     * 查找推荐率
     */
    @Query("SELECT COUNT(f) FROM DepartmentFeedback f WHERE f.hotelId = :hotelId AND f.departmentId = :departmentId AND f.isRecommended = true")
    Long countRecommendedByHotelAndDepartment(
            @Param("hotelId") Long hotelId,
            @Param("departmentId") Long departmentId);

    /**
     * 查找低评分评价（评分<=2）
     */
    @Query("SELECT f FROM DepartmentFeedback f WHERE f.hotelId = :hotelId AND f.overallRating <= 2.0")
    List<DepartmentFeedback> findLowRatingFeedbackByHotel(@Param("hotelId") Long hotelId);

    /**
     * 查找高评分评价（评分>=4）
     */
    @Query("SELECT f FROM DepartmentFeedback f WHERE f.hotelId = :hotelId AND f.overallRating >= 4.0")
    List<DepartmentFeedback> findHighRatingFeedbackByHotel(@Param("hotelId") Long hotelId);

    /**
     * 根据评分等级查找评价
     */
    @Query("SELECT f FROM DepartmentFeedback f WHERE f.hotelId = :hotelId AND f.overallRating >= :minRating AND f.overallRating <= :maxRating")
    List<DepartmentFeedback> findByHotelIdAndRatingRange(
            @Param("hotelId") Long hotelId,
            @Param("minRating") BigDecimal minRating,
            @Param("maxRating") BigDecimal maxRating);

    /**
     * 查找包含指定标签的评价
     */
    @Query("SELECT f FROM DepartmentFeedback f WHERE f.hotelId = :hotelId AND f.feedbackTags LIKE %:tag%")
    List<DepartmentFeedback> findByHotelIdAndTagContaining(
            @Param("hotelId") Long hotelId,
            @Param("tag") String tag);
}