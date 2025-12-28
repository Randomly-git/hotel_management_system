package com.hotel.hotel.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 客户实体
 */
@Data
@Entity
@Table(name = "customers")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hotel_id", nullable = false)
    private Long hotelId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 2)
    private String country;

    @Column(name = "id_card_number", length = 50)
    private String idCardNumber;

    @Column(name = "is_repeated_guest")
    @Builder.Default
    private Boolean isRepeatedGuest = false;

    @Column(name = "total_stays")
    @Builder.Default
    private Integer totalStays = 0;

    @Column(name = "total_cancellations")
    @Builder.Default
    private Integer totalCancellations = 0;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private VipLevel vipLevel = VipLevel.normal;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 关联关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", insertable = false, updatable = false)
    private Hotel hotel;

    /**
     * VIP等级枚举
     */
    public enum VipLevel {
        normal,    // 普通
        silver,    // 银卡
        gold,      // 金卡
        platinum   // 白金卡
    }
}
