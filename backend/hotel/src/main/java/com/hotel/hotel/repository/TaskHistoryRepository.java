package com.hotel.hotel.repository;

import com.hotel.hotel.entity.TaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务历史记录数据访问接口
 */
@Repository
public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {

    /**
     * 根据任务单ID查询历史记录
     */
    List<TaskHistory> findByTaskOrderIdOrderByOperationTimeDesc(Long taskOrderId);

    /**
     * 根据任务单ID和操作类型查询历史记录
     */
    List<TaskHistory> findByTaskOrderIdAndOperationTypeOrderByOperationTimeDesc(Long taskOrderId, String operationType);

    /**
     * 根据酒店ID查询历史记录
     */
    List<TaskHistory> findByHotelIdOrderByOperationTimeDesc(Long hotelId);

    /**
     * 根据酒店ID和时间范围查询历史记录
     */
    @Query("SELECT h FROM TaskHistory h WHERE h.hotelId = :hotelId AND h.operationTime BETWEEN :startTime AND :endTime ORDER BY h.operationTime DESC")
    List<TaskHistory> findByHotelIdAndOperationTimeBetween(@Param("hotelId") Long hotelId,
                                                           @Param("startTime") LocalDateTime startTime,
                                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 根据操作人查询历史记录
     */
    List<TaskHistory> findByOperatorIdOrderByOperationTimeDesc(String operatorId);

    /**
     * 根据部门ID查询历史记录
     */
    List<TaskHistory> findByDepartmentIdOrderByOperationTimeDesc(Long departmentId);

    /**
     * 查询指定时间范围内的操作统计
     */
    @Query("SELECT h.operationType, COUNT(h) FROM TaskHistory h WHERE h.hotelId = :hotelId AND h.operationTime BETWEEN :startTime AND :endTime GROUP BY h.operationType")
    List<Object[]> countOperationsByTypeInPeriod(@Param("hotelId") Long hotelId,
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 查询任务平均处理时间（分钟）
     */
    @Query(value = "SELECT AVG(TIMESTAMPDIFF(MINUTE, (SELECT MIN(h2.operation_time) FROM task_history h2 WHERE h2.task_order_id = h.task_order_id), h.operation_time)) " +
           "FROM task_history h WHERE h.hotel_id = :hotelId AND h.new_status = 'COMPLETED' AND h.operation_time BETWEEN :startTime AND :endTime", nativeQuery = true)
    Double calculateAverageProcessingTime(@Param("hotelId") Long hotelId,
                                         @Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 统计操作人类型分布
     */
    @Query("SELECT h.operatorType, COUNT(h) FROM TaskHistory h WHERE h.hotelId = :hotelId AND h.operationTime BETWEEN :startTime AND :endTime GROUP BY h.operatorType")
    List<Object[]> countOperationsByOperatorType(@Param("hotelId") Long hotelId,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);
}