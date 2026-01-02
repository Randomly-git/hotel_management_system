package com.hotel.hotel.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "pricing_record")
public class PricingRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recordId;

    // 关键修改：建立与 HotelRoomType 的关联，废弃旧的 RoomType
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_type_id", nullable = false)
    private HotelRoomType roomType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal originalPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal adjustedPrice;

    // 增长长度至 500，以容纳 PricingService 生成的详细因子说明
    @Column(length = 500)
    private String adjustFactor;

    // 使用 @UpdateTimestamp 自动管理时间，更加安全
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime adjustTime;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "base_price", precision = 10, scale = 2)
    private BigDecimal basePrice;

    // 状态字段，对应 PricingService 中的 "PENDING" 或 "APPLIED"
    @Column(name = "status", length = 20)
    private String status;

    @PrePersist
    protected void onCreate() {
        if (this.adjustTime == null) {
            this.adjustTime = LocalDateTime.now();
        }
    }
}