package com.hotel.hotel.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "department_performance")
public class DepartmentPerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recordId; // 对应 record_id

    // 关联映射：一个部门可以有多条绩效记录
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id", nullable = false) // 对应 dept_id 字段
    private Department department;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal scoreIndex; // 对应 score_index (部门绩效指数)

    @Column(nullable = false)
    private Integer totalReviews = 0; // 对应 total_reviews

    @Column(nullable = false, length = 10)
    private String alertLevel; // 对应 alert_level (预警等级)

    @Column(nullable = false)
    private LocalDate statisticsDate; // 对应 statistics_date
}