package com.hotel.hotel.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预订实体
 */
@Data
@Entity
@Table(name = "bookings")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hotel_id", nullable = false)
    private Long hotelId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "room_type_id", nullable = false)
    private Long roomTypeId;

    @Column(name = "booking_number", nullable = false, unique = true, length = 50)
    private String bookingNumber;

    // 预订时间信息
    @Column(name = "lead_time", nullable = false)
    private Integer leadTime;

    @Column(name = "booking_date", nullable = false)
    private LocalDateTime bookingDate;

    // 入住时间信息
    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Column(name = "stays_in_weekend_nights")
    @Builder.Default
    private Integer staysInWeekendNights = 0;

    @Column(name = "stays_in_week_nights")
    @Builder.Default
    private Integer staysInWeekNights = 0;

    @Column(name = "total_nights", nullable = false)
    private Integer totalNights;

    // 客人信息
    @Column(nullable = false)
    @Builder.Default
    private Integer adults = 1;

    @Column
    @Builder.Default
    private Integer children = 0;

    @Column
    @Builder.Default
    private Integer babies = 0;

    @Column(name = "required_car_parking_spaces")
    @Builder.Default
    private Integer requiredCarParkingSpaces = 0;

    // 价格信息
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "adr", nullable = false, precision = 10, scale = 2)
    private BigDecimal adr;

    @Enumerated(EnumType.STRING)
    @Column(name = "deposit_type", length = 20)
    @Builder.Default
    private DepositType depositType = DepositType.no_deposit;

    // 餐饮
    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", length = 10)
    @Builder.Default
    private MealType mealType = MealType.bb;

    // 渠道信息
    @Column(name = "market_segment", length = 50)
    private String marketSegment;

    @Column(name = "distribution_channel", length = 50)
    private String distributionChannel;

    // 状态
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BookingStatus status = BookingStatus.booked;

    @Column(name = "is_canceled")
    @Builder.Default
    private Boolean isCanceled = false;

    @Column(name = "cancel_date")
    private LocalDateTime cancelDate;

    // 特殊需求
    @Column(name = "special_requests")
    @Builder.Default
    private Integer specialRequests = 0;

    @Column(name = "requests_text", columnDefinition = "TEXT")
    private String requestsText;

    // 变更记录
    @Column(name = "booking_changes")
    @Builder.Default
    private Integer bookingChanges = 0;

    @Column(name = "days_in_waiting_list")
    @Builder.Default
    private Integer daysInWaitingList = 0;

    // 实际分配
    @Column(name = "assigned_room_id")
    private Long assignedRoomId;

    @Column(name = "actual_room_type", length = 1)
    private String actualRoomType;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", insertable = false, updatable = false)
    private HotelRoomType roomType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_room_id", insertable = false, updatable = false)
    private Room assignedRoom;

    /**
     * 押金类型枚举
     */
    public enum DepositType {
        no_deposit,     // 无押金
        non_refund,     // 不可退
        refundable      // 可退
    }

    /**
     * 餐型枚举
     */
    public enum MealType {
        bb,     // 含早 Bed & Breakfast
        hb,     // 含早晚餐 Half Board
        fb,     // 全含 Full Board
        sc      // 无餐 Self Catering
    }

    /**
     * 预订状态枚举
     */
    public enum BookingStatus {
        booked,         // 已预订
        checked_in,     // 已入住
        canceled,       // 已取消
        completed       // 已完成
    }
}
