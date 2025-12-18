package com.hotel.hotel.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "customer_feedback")
public class CustomerFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feedbackId; // 对应 feedback_id

    @Column(nullable = false, length = 100)
    private String customerName; // 对应 customer_name

    @Lob // 用于映射 TEXT 或较大的 VARCHAR 字段
    @Column(nullable = false)
    private String feedbackContent; // 对应 feedback_content

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal sentimentScore; // 对应 sentiment_score (情感分析得分)

    // 关联映射：一个部门可以有多个反馈，一个反馈属于一个部门
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attributed_dept_id", nullable = false) // 对应 attributed_dept_id 字段
    private Department department;

    @Column(nullable = false)
    private Boolean isProcessed = false; // 对应 is_processed

    @Column(nullable = false)
    private LocalDateTime feedbackTime; // 对应 feedback_time

    @Column(nullable = false)
    private Boolean needsReview = false; // 是否触发预警审核

    @Column(length = 20)
    private String reviewStatus; // PENDING/APPROVED/REJECTED

    private String reviewComment; // 审核人的备注

    private String ipAddress; // 用于恶意评论频率检测

    @Column(nullable = false)
    private String hotelId; // 租户隔离字段
}