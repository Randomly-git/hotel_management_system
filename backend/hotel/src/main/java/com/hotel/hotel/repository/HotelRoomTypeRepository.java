package com.hotel.hotel.repository;

import com.hotel.hotel.entity.HotelRoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 房型配置 Repository
 */
@Repository
public interface HotelRoomTypeRepository extends JpaRepository<HotelRoomType, Long> {

    /**
     * 根据酒店ID查询房型列表
     */
    List<HotelRoomType> findByHotelId(Long hotelId);

    /**
     * 根据酒店ID和房型代码查询
     */
    Optional<HotelRoomType> findByHotelIdAndTypeCode(Long hotelId, String typeCode);

    /**
     * 根据房型代码查询
     */
    List<HotelRoomType> findByTypeCode(String typeCode);
}
