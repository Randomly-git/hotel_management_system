package com.hotel.hotel.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "department_task")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_order_id", nullable = false)
    private Long taskOrderId;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @Column(name = "task_title", nullable = false, length = 200)
    private String taskTitle;

    @Column(name = "task_description", columnDefinition = "TEXT")
    private String taskDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private TaskStatus status = TaskStatus.ASSIGNED;

    @Column(name = "assigned_to", length = 100)
    private String assignedTo;

    @Column(name = "room_number", length = 20)
    private String roomNumber;

    @Column(name = "customer_id", length = 50)
    private String customerId;

    @Column(name = "expected_completion_time")
    private LocalDateTime expectedCompletionTime;

    @Column(name = "actual_completion_time")
    private LocalDateTime actualCompletionTime;

    @Column(name = "completion_remark", columnDefinition = "TEXT")
    private String completionRemark;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "hotel_id", nullable = false)
    @Builder.Default
    private Long hotelId = 1L;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.hotelId == null) {
            this.hotelId = 1L;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 枚举定义
    public enum TaskPriority {
        LOW, MEDIUM, HIGH, URGENT
    }

    public enum TaskStatus {
        ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED
    }
}