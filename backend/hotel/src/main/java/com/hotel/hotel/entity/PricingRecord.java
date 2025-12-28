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
    private BigDecimal originalPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal adjustedPrice;

    @Column(length = 255)
    private String adjustFactor;

    @Column(nullable = false, updatable = false)
    private LocalDateTime adjustTime;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "base_price", precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "status", length = 20)
    private String status;

    @PrePersist
    protected void onCreate() {
        this.adjustTime = LocalDateTime.now();
    }
}
// Repository: PricingRecordRepository extends JpaRepository<PricingRecord, Long>