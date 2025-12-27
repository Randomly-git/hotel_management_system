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
 * 房间实体
 */
@Data
@Entity
@Table(name = "rooms")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hotel_id", nullable = false)
    private Long hotelId;

    @Column(name = "room_type_id", nullable = false)
    private Long roomTypeId;

    @Column(name = "room_number", nullable = false, length = 20)
    private String roomNumber;

    @Column(nullable = false)
    private Integer floor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomStatus status;

    @Builder.Default
    @Column(name = "has_ac")
    private Boolean hasAc = true;

    @Builder.Default
    @Column(name = "has_tv")
    private Boolean hasTv = true;

    @Builder.Default
    @Column(name = "has_wifi")
    private Boolean hasWifi = true;

    @Builder.Default
    @Column(name = "has_balcony")
    private Boolean hasBalcony = false;

    @Builder.Default
    @Column(name = "has_kitchen")
    private Boolean hasKitchen = false;

    @Builder.Default
    @Column(name = "parking_spaces")
    private Integer parkingSpaces = 0;

    @Column(columnDefinition = "TEXT")
    private String description;

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
    @JoinColumn(name = "room_type_id", insertable = false, updatable = false)
    private HotelRoomType roomType;

    /**
     * 房间状态枚举
     */
    public enum RoomStatus {
        available,    // 可用
        occupied,     // 已入住
        maintenance,  // 维护中
        cleaning      // 清洁中
    }
}
