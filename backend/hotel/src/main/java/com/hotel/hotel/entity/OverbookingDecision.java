package com.hotel.hotel.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 超售决策记录实体
 */
@Data
@Entity
@Table(name = "overbooking_decisions")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OverbookingDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hotel_id", nullable = false)
    private Long hotelId;

    @Column(name = "room_type_id", nullable = false)
    private Long roomTypeId;

    @Column(name = "decision_date", nullable = false)
    private LocalDate decisionDate;

    @Column(name = "state_key", nullable = false, length = 100)
    private String stateKey;

    @Column(name = "action_chosen", nullable = false)
    private Integer actionChosen; // 选择的超售量

    @Column(name = "q_value", precision = 10, scale = 2)
    private BigDecimal qValue;

    @Column(name = "total_rooms", nullable = false)
    private Integer totalRooms;

    @Column(name = "confirmed_bookings", nullable = false)
    private Integer confirmedBookings;

    @Column(name = "actual_cancellations")
    @Builder.Default
    private Integer actualCancellations = 0;

    @Column(name = "actual_no_shows")
    @Builder.Default
    private Integer actualNoShows = 0;

    @Column(name = "was_successful")
    private Boolean wasSuccessful;

    @Column(name = "reward", precision = 12, scale = 2)
    private BigDecimal reward;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
