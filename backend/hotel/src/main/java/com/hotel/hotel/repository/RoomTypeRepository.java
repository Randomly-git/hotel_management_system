package com.hotel.hotel.repository;

import com.hotel.hotel.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomTypeRepository extends JpaRepository<RoomType, Integer> {
    Optional<RoomType> findByTypeName(String typeName);
}
