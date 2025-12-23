package com.hotel.hotel.repository;

import com.hotel.hotel.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    /**
     * 核心冲突检测：使用原生 SQL 确保对 MySQL 5.7 的完美支持
     * 逻辑：新入 < 旧出 (start + days) AND 新出 > 旧入
     */
    @Query(value = "SELECT * FROM orders WHERE room_id = :roomId " +
            "AND status IN (1, 2) " +
            "AND :checkInDate < DATE_ADD(start_time, INTERVAL days DAY) " +
            "AND :checkOutDate > start_time", nativeQuery = true)
    List<Booking> findConflictingBookings(
            @Param("roomId") Integer roomId,
            @Param("checkInDate") Date checkInDate,
            @Param("checkOutDate") Date checkOutDate);

    /**
     * 获取指定房间的所有活跃订单，用于 RoomService 计算不可用日期列表
     */
    List<Booking> findByRoomIdAndStatusIn(Integer roomId, List<Integer> statuses);

    @Query(value = "SELECT COUNT(*) FROM orders o " +
            "JOIN room r ON o.room_id = r.id " +
            "WHERE r.category_id = :typeId " +
            "AND o.status IN (1, 2) " +
            "AND :targetDate >= o.start_time " +
            "AND :targetDate < DATE_ADD(o.start_time, INTERVAL o.days DAY)", nativeQuery = true)
    long countOccupiedRoomsByDate(@Param("typeId") Integer typeId, @Param("targetDate") Date targetDate);
}