package com.hotel.hotel.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "guest_profile")
public class GuestProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long guestId;

    @Column(nullable = false, unique = true, length = 50)
    private String memberId;

    private String preferenceTags;

    private LocalDate lastCheckIn;

    @Column(precision = 10, scale = 2)
    private BigDecimal avgSpend;

    @Lob
    private String predictionModelData;
}
// Repository: GuestProfileRepository extends JpaRepository<GuestProfile, Long>