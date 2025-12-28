package com.hotel.hotel.repository;

import com.hotel.hotel.entity.TaskOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务单数据访问接口
 */
public interface TaskOrderRepository extends JpaRepository<TaskOrder, Long> {
    /**
     * 根据状态查询任务单（按创建时间倒序）
     */
    List<TaskOrder> findByStatusOrderByCreateTimeDesc(String status);

    /**
     * 根据状态统计数量
     */
    Long countByStatus(String status);

    /**
     * 根据部门统计任务数量
     */
    @Query("SELECT t.assignedDepartment.deptName, COUNT(t) FROM TaskOrder t GROUP BY t.assignedDepartment.deptName")
    List<Object[]> countByDepartment();

    /**
     * 统计指定时间之后创建的任务数量
     */
    Long countByCreateTimeAfter(LocalDateTime time);

    /**
     * 统计指定状态且已超时的任务数量
     */
    Long countByStatusAndDueTimeBefore(String status, LocalDateTime time);

    /**
     * 计算平均处理时间（分钟）
     */
    @Query(value = "SELECT AVG(TIMESTAMPDIFF(MINUTE, t.create_time, t.completed_time)) " +
           "FROM task_order t WHERE t.status = 'COMPLETED' AND t.completed_time IS NOT NULL",
           nativeQuery = true)
    Double getAverageProcessTime();

    /**
     * 批量更新任务状态
     */
    @Modifying
    @Query("UPDATE TaskOrder t SET t.status = :status WHERE t.taskId IN :taskIds")
    int updateStatusByIds(@Param("taskIds") List<Long> taskIds, @Param("status") String status);

    /**
     * 根据酒店ID查询任务（租户隔离）
     */
    @Query("SELECT t FROM TaskOrder t WHERE t.hotelId = :hotelId ORDER BY t.createTime DESC")
    List<TaskOrder> findByHotelIdOrderByCreateTimeDesc(@Param("hotelId") Long hotelId);

    /**
     * 根据酒店ID和状态查询任务
     */
    @Query("SELECT t FROM TaskOrder t WHERE t.hotelId = :hotelId AND t.status = :status ORDER BY t.createTime DESC")
    List<TaskOrder> findByHotelIdAndStatus(@Param("hotelId") Long hotelId, @Param("status") String status);

    /**
     * 根据酒店ID和会员ID查询任务
     */
    @Query("SELECT t FROM TaskOrder t WHERE t.hotelId = :hotelId AND t.guestMemberId = :memberId ORDER BY t.createTime DESC")
    List<TaskOrder> findByHotelIdAndMemberId(@Param("hotelId") Long hotelId, @Param("memberId") String memberId);

    /**
     * 根据部门名称和状态查询任务（租户隔离）
     */
    @Query("SELECT t FROM TaskOrder t WHERE t.assignedDepartment.deptName = :deptName AND t.status = :status AND t.hotelId = :hotelId ORDER BY t.createTime DESC")
    List<TaskOrder> findByAssignedDepartment_DeptNameAndStatusAndHotelIdOrderByCreateTimeDesc(@Param("deptName") String deptName, @Param("status") String status, @Param("hotelId") Long hotelId);

    /**
     * 根据部门名称查询任务（租户隔离）
     */
    @Query("SELECT t FROM TaskOrder t WHERE t.assignedDepartment.deptName = :deptName AND t.hotelId = :hotelId ORDER BY t.createTime DESC")
    List<TaskOrder> findByAssignedDepartment_DeptNameAndHotelIdOrderByCreateTimeDesc(@Param("deptName") String deptName, @Param("hotelId") Long hotelId);

    /**
     * 根据部门名称查询所有任务（用于统计）
     */
    @Query("SELECT t FROM TaskOrder t WHERE t.assignedDepartment.deptName = :deptName AND t.hotelId = :hotelId")
    List<TaskOrder> findByAssignedDepartment_DeptNameAndHotelId(@Param("deptName") String deptName, @Param("hotelId") Long hotelId);

    /**
     * 批量分配任务给部门
     */
    @Modifying
    @Query("UPDATE TaskOrder t SET t.assignedDepartment.deptId = :deptId WHERE t.taskId IN :taskIds AND t.hotelId = :hotelId")
    int assignTasksToDepartment(@Param("taskIds") List<Long> taskIds, @Param("deptId") Long deptId, @Param("hotelId") Long hotelId);
}
