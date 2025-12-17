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
@Table(name = "sys_department")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deptId; // 对应 dept_id

    @Column(nullable = false, unique = true, length = 50)
    private String deptName; // 对应 dept_name

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal weight; // 对应 weight (绩效计算权重)

    // 使用 PrePersist/PreUpdate 自动维护时间戳，此处简化为直接映射
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime; // 对应 create_time

    // 在对象创建时自动设置时间
    @PrePersist
    protected void onCreate() {
        this.createTime = LocalDateTime.now();
    }

    // 租户隔离字段
    @Column(nullable = false)
    private String hotelId;
}