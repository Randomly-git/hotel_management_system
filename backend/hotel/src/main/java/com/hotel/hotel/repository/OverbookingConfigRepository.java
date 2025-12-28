package com.hotel.hotel.repository;

import com.hotel.hotel.entity.OverbookingConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OverbookingConfigRepository extends JpaRepository<OverbookingConfig, Long> {

    /**
     * 根据酒店和房型查询配置
     */
    Optional<OverbookingConfig> findByHotelIdAndRoomTypeId(Long hotelId, Long roomTypeId);

    /**
     * 查询酒店所有启用的配置
     */
    java.util.List<OverbookingConfig> findByHotelIdAndEnabledTrue(Long hotelId);
}
