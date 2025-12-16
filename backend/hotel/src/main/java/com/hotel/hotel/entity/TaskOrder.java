package com.hotel.hotel.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 任务单实体类
 * 存储客户服务任务的详细信息
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "task_order")
public class TaskOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId;

    @Column(name = "hotel_id")
    private Long hotelId;  // 租户隔离字段

    @Column(name = "guest_member_id", nullable = false, length = 50)
    private String guestMemberId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_dept_id", nullable = false)
    private Department assignedDepartment; // 关联部门

    @Column(name = "task_type", nullable = false, length = 20)
    private String taskType;

    @Lob
    @Column(name = "task_content", nullable = false)
    private String taskContent;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "room_number", length = 20)
    private String roomNumber;  // 房间号

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "due_time")
    private LocalDateTime dueTime;

    @Column(name = "completed_time")
    private LocalDateTime completedTime;  // 完成时间

    @PrePersist
    protected void onCreate() {
        this.createTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        // 当状态变为COMPLETED时，记录完成时间
        if ("COMPLETED".equals(this.status) && this.completedTime == null) {
            this.completedTime = LocalDateTime.now();
        }
    }
}
// Repository: TaskOrderRepository extends JpaRepository<TaskOrder, Long>