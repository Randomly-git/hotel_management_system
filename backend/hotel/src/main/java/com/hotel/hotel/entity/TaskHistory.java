package com.hotel.hotel.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 任务历史记录实体类
 * 用于记录任务的状态变更历史，实现审计追踪
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "task_history")
public class TaskHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    @Column(name = "task_order_id", nullable = false)
    private Long taskOrderId;

    @Column(name = "old_status", length = 20)
    private String oldStatus;

    @Column(name = "new_status", length = 20, nullable = false)
    private String newStatus;

    @Column(name = "operation_type", length = 20, nullable = false)
    private String operationType; // CREATE, UPDATE, CANCEL, COMPLETE, ASSIGN

    @Column(name = "operation_reason", columnDefinition = "TEXT")
    private String operationReason;

    @Column(name = "operator_id", length = 50)
    private String operatorId; // 操作人ID

    @Column(name = "operator_name", length = 100)
    private String operatorName; // 操作人姓名

    @Column(name = "operator_type", length = 20)
    private String operatorType; // CUSTOMER, STAFF, SYSTEM

    @Column(name = "operation_time", nullable = false)
    private LocalDateTime operationTime;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "department_name", length = 50)
    private String departmentName;

    @Column(name = "hotel_id", nullable = false)
    private Long hotelId;

    @PrePersist
    protected void onCreate() {
        this.operationTime = LocalDateTime.now();
    }

    /**
     * 操作类型枚举
     */
    public enum OperationType {
        CREATE("CREATE", "创建任务"),
        UPDATE("UPDATE", "更新任务"),
        CANCEL("CANCEL", "取消任务"),
        COMPLETE("COMPLETE", "完成任务"),
        ASSIGN("ASSIGN", "分配任务"),
        ACCEPT("ACCEPT", "接受任务"),
        REJECT("REJECT", "拒绝任务");

        private final String code;
        private final String description;

        OperationType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 操作人类型枚举
     */
    public enum OperatorType {
        CUSTOMER("CUSTOMER", "客户"),
        STAFF("STAFF", "员工"),
        SYSTEM("SYSTEM", "系统"),
        MANAGER("MANAGER", "管理员");

        private final String code;
        private final String description;

        OperatorType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }
}