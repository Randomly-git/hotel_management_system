package com.hotel.hotel.repository;

import com.hotel.hotel.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 房间 Repository
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * 根据酒店ID查询所有房间
     */
    List<Room> findByHotelId(Long hotelId);

    /**
     * 根据酒店ID和状态查询房间
     */
    List<Room> findByHotelIdAndStatus(Long hotelId, Room.RoomStatus status);

    /**
     * 根据酒店ID和房型查询房间
     */
    List<Room> findByHotelIdAndRoomTypeId(Long hotelId, Long roomTypeId);

    /**
     * 根据酒店ID和房间号查询
     */
    Optional<Room> findByHotelIdAndRoomNumber(Long hotelId, String roomNumber);

    /**
     * 检查房间号是否已存在
     */
    boolean existsByHotelIdAndRoomNumber(Long hotelId, String roomNumber);

    /**
     * 根据房型ID查询可用房间
     */
    @Query("SELECT r FROM Room r WHERE r.roomTypeId = :roomTypeId AND r.status = 'available'")
    List<Room> findAvailableRoomsByType(@Param("roomTypeId") Long roomTypeId);

    /**
     * 统计各状态的房间数量
     */
    @Query("SELECT r.status, COUNT(r) FROM Room r WHERE r.hotelId = :hotelId GROUP BY r.status")
    List<Object[]> countByStatusGroupBy(@Param("hotelId") Long hotelId);

    /**
     * 统计指定酒店和房型的房间总数
     */
    long countByHotelIdAndRoomTypeId(Long hotelId, Long roomTypeId);

    /**
     * 根据房间号查询（跨酒店）
     */
    Optional<Room> findByRoomNumber(String roomNumber);

    /**
     * 根据酒店ID、状态和房型查询可用房间
     */
    List<Room> findByHotelIdAndStatusAndRoomTypeId(Long hotelId, Room.RoomStatus status, Long roomTypeId);
}
