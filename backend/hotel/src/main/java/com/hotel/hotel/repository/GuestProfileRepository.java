package com.hotel.hotel.repository;

import com.hotel.hotel.entity.GuestProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuestProfileRepository extends JpaRepository<GuestProfile, Long> {
    Optional<GuestProfile> findByMemberId(String memberId);
}
