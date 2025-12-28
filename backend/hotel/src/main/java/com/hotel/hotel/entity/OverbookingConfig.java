package com.hotel.hotel.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 超售配置实体
 */
@Data
@Entity
@Table(name = "overbooking_config")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OverbookingConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hotel_id", nullable = false)
    private Long hotelId;

    @Column(name = "room_type_id", nullable = false)
    private Long roomTypeId;

    @Builder.Default
    @Column(name = "max_overbook", nullable = false)
    private Integer maxOverbook = 5;

    @Builder.Default
    @Column(name = "compensation_rate", precision = 5, scale = 2)
    private BigDecimal compensationRate = new BigDecimal("1.5");

    @Builder.Default
    @Column(name = "enabled")
    private Boolean enabled = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
