package com.hotel.hotel.repository;

import com.hotel.hotel.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 酒店信息 Repository
 */
@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    /**
     * 根据类型查询酒店
     */
    List<Hotel> findByType(String type);

    /**
     * 根据城市查询酒店
     */
    List<Hotel> findByCity(String city);

    /**
     * 根据名称模糊查询
     */
    List<Hotel> findByNameContaining(String name);
}
