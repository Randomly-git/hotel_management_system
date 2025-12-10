package com.hotel.hotel.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "task_order")
public class TaskOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId;

    @Column(nullable = false, length = 50)
    private String guestMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_dept_id", nullable = false)
    private Department assignedDepartment; // 关联部门

    @Column(nullable = false, length = 20)
    private String taskType;

    @Lob
    @Column(nullable = false)
    private String taskContent;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    private LocalDateTime dueTime;

    @PrePersist
    protected void onCreate() {
        this.createTime = LocalDateTime.now();
    }
}
// Repository: TaskOrderRepository extends JpaRepository<TaskOrder, Long>