package com.hotel.hotel.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 入住记录实体
 */
@Data
@Entity
@Table(name = "check_in_records")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckInRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hotel_id", nullable = false)
    private Long hotelId;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "check_in_time", nullable = false)
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Column(name = "scheduled_check_out_date", nullable = false)
    private LocalDate scheduledCheckOutDate;

    @Column(name = "actual_price", precision = 10, scale = 2)
    private BigDecimal actualPrice;

    @Column(name = "price_adjustment_reason")
    private String priceAdjustmentReason;

    @Column(name = "is_extended")
    @Builder.Default
    private Boolean isExtended = false;

    @Column(name = "original_check_out_date")
    private LocalDate originalCheckOutDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CheckInStatus status = CheckInStatus.active;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 入住状态枚举
     */
    public enum CheckInStatus {
        active,           // 在住
        completed,        // 已退房
        early_checkout    // 提前退房
    }

    // 关联关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", insertable = false, updatable = false)
    private Hotel hotel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", insertable = false, updatable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", insertable = false, updatable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private Customer customer;
}
