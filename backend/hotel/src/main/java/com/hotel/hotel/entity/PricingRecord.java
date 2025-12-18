package com.hotel.hotel.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pricing_record")
public class PricingRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recordId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType; // 关联房型

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal originalPrice; // 保持原样：调价前的价格

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal adjustedPrice; // 保持原样：系统计算后的建议价格

    @Column(length = 255)
    private String adjustFactor; // 保持原样：记录调价因子

    @Column(nullable = false, updatable = false)
    private LocalDateTime adjustTime; // 保持原样

    @Column(nullable = false)
    private LocalDate effectiveDate; // 保持原样

    // --- 新增字段 1：用于审批流状态管理 ---
    // PENDING: 待审批（偏离度过高时）, APPROVED: 已应用, REJECTED: 已驳回
    @Column(nullable = false, length = 20)
    private String status;

    // --- 新增字段 2：基准价 ---
    // 专门用于计算 50% 偏离度的参照物。
    // 如果算法建议的 adjustedPrice 与此 basePrice 偏差 > 50%，则 status 设为 PENDING
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @PrePersist
    protected void onCreate() {
        this.adjustTime = LocalDateTime.now();
        // 默认逻辑：如果 Service 层没有显式设置状态，初始化为已通过
        // 我们会在 Service 逻辑中判断偏离度，若偏离过高则在存入前设为 PENDING
        if (this.status == null) {
            this.status = "APPROVED";
        }
    }
}