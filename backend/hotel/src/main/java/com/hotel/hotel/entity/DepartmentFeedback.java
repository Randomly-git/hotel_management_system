package com.hotel.hotel.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 部门服务评价实体类
 * 用于客户对部门服务进行满意度评价
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "department_feedback")
public class DepartmentFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feedbackId;

    @Column(name = "task_order_id", nullable = false)
    private Long taskOrderId;

    @Column(name = "department_task_id", nullable = false)
    private Long departmentTaskId;

    @Column(name = "customer_id", nullable = false, length = 50)
    private String customerId;

    @Column(name = "hotel_id", nullable = false)
    private Long hotelId;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @Column(name = "department_name", nullable = false, length = 50)
    private String departmentName;

    /**
     * 服务评分 1-5分
     */
    @Column(name = "service_rating", nullable = false)
    private Integer serviceRating;

    /**
     * 响应速度评分 1-5分
     */
    @Column(name = "response_speed_rating", nullable = false)
    private Integer responseSpeedRating;

    /**
     * 服务质量评分 1-5分
     */
    @Column(name = "service_quality_rating", nullable = false)
    private Integer serviceQualityRating;

    /**
     * 综合评分（数据库自动计算平均值 - GENERATED STORED 列）
     */
    @Column(name = "overall_rating", insertable = false, updatable = false)
    private BigDecimal overallRating;

    /**
     * 评价内容
     */
    @Column(name = "feedback_content", columnDefinition = "TEXT")
    private String feedbackContent;

    /**
     * 标签（用逗号分隔）：专业、及时、友好、有待改进等
     */
    @Column(name = "feedback_tags", length = 200)
    private String feedbackTags;

    /**
     * 是否推荐服务
     */
    @Column(name = "is_recommended", nullable = false)
    private Boolean isRecommended;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 评价等级枚举
     */
    public enum FeedbackLevel {
        EXCELLENT(5, "非常满意"),
        GOOD(4, "满意"),
        AVERAGE(3, "一般"),
        POOR(2, "不满意"),
        TERRIBLE(1, "非常不满意");

        private final Integer rating;
        private final String description;

        FeedbackLevel(Integer rating, String description) {
            this.rating = rating;
            this.description = description;
        }

        public Integer getRating() {
            return rating;
        }

        public String getDescription() {
            return description;
        }

        public static FeedbackLevel fromRating(Integer rating) {
            if (rating == null) return AVERAGE;
            switch (rating) {
                case 5: return EXCELLENT;
                case 4: return GOOD;
                case 3: return AVERAGE;
                case 2: return POOR;
                case 1: return TERRIBLE;
                default: return AVERAGE;
            }
        }
    }
}