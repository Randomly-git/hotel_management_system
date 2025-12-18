package com.hotel.hotel.repository;

import com.hotel.hotel.entity.DepartmentTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DepartmentTaskRepository extends JpaRepository<DepartmentTask, Long> {

    /**
     * 根据部门ID查询任务
     */
    List<DepartmentTask> findByDepartmentId(Long departmentId);

    /**
     * 根据酒店ID和部门ID查询任务
     */
    List<DepartmentTask> findByHotelIdAndDepartmentId(Long hotelId, Long departmentId);

    /**
     * 根据状态查询任务
     */
    List<DepartmentTask> findByStatus(DepartmentTask.TaskStatus status);

    /**
     * 根据酒店ID和状态查询任务
     */
    List<DepartmentTask> findByHotelIdAndStatus(Long hotelId, DepartmentTask.TaskStatus status);

    /**
     * 根据优先级查询任务
     */
    List<DepartmentTask> findByPriority(DepartmentTask.TaskPriority priority);

    /**
     * 根据房间号查询任务
     */
    List<DepartmentTask> findByRoomNumber(String roomNumber);

    /**
     * 根据客户ID查询任务
     */
    List<DepartmentTask> findByCustomerId(String customerId);

    /**
     * 查询未完成的任务（ASSIGNED或IN_PROGRESS状态）
     */
    @Query("SELECT dt FROM DepartmentTask dt WHERE dt.status IN ('ASSIGNED', 'IN_PROGRESS') AND dt.hotelId = :hotelId")
    List<DepartmentTask> findPendingTasks(@Param("hotelId") Long hotelId);

    /**
     * 查询超时任务（当前时间超过期望完成时间且状态为未完成）
     */
    @Query("SELECT dt FROM DepartmentTask dt WHERE dt.expectedCompletionTime < :currentTime AND dt.status IN ('ASSIGNED', 'IN_PROGRESS') AND dt.hotelId = :hotelId")
    List<DepartmentTask> findOverdueTasks(@Param("currentTime") LocalDateTime currentTime, @Param("hotelId") Long hotelId);

    /**
     * 统计各状态任务数量
     */
    @Query("SELECT dt.status, COUNT(dt) FROM DepartmentTask dt WHERE dt.hotelId = :hotelId GROUP BY dt.status")
    List<Object[]> countTasksByStatus(@Param("hotelId") Long hotelId);

    /**
     * 根据原始任务单ID查询部门任务
     */
    List<DepartmentTask> findByTaskOrderId(Long taskOrderId);

    /**
     * 查询指定时间范围内创建的任务
     */
    @Query("SELECT dt FROM DepartmentTask dt WHERE dt.createdAt BETWEEN :startTime AND :endTime AND dt.hotelId = :hotelId")
    List<DepartmentTask> findTasksByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, @Param("hotelId") Long hotelId);
}