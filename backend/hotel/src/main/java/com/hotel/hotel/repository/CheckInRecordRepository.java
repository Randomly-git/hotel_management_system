package com.hotel.hotel.repository;

import com.hotel.hotel.entity.CheckInRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 入住记录 Repository
 */
@Repository
public interface CheckInRecordRepository extends JpaRepository<CheckInRecord, Long> {

    /**
     * 根据预订ID查询入住记录
     */
    Optional<CheckInRecord> findByBookingId(Long bookingId);

    /**
     * 根据房间ID查询活跃入住记录
     */
    Optional<CheckInRecord> findByRoomIdAndStatus(Long roomId, CheckInRecord.CheckInStatus status);

    /**
     * 根据酒店ID查询所有入住记录
     */
    List<CheckInRecord> findByHotelId(Long hotelId);

    /**
     * 根据酒店ID和状态查询入住记录
     */
    List<CheckInRecord> findByHotelIdAndStatus(Long hotelId, CheckInRecord.CheckInStatus status);

    /**
     * 查询指定时间范围内的活跃入住记录
     */
    @Query("SELECT cir FROM CheckInRecord cir WHERE cir.hotelId = :hotelId " +
           "AND cir.checkInTime <= :endTime " +
           "AND (cir.checkOutTime IS NULL OR cir.checkOutTime >= :startTime) " +
           "AND cir.status = 'active'")
    List<CheckInRecord> findActiveCheckInsInDateRange(
        @Param("hotelId") Long hotelId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 统计当前在住人数
     */
    @Query("SELECT COUNT(cir) FROM CheckInRecord cir WHERE cir.hotelId = :hotelId AND cir.status = 'active'")
    long countActiveGuests(@Param("hotelId") Long hotelId);

    /**
     * 根据房间ID查询最新的入住记录
     */
    Optional<CheckInRecord> findFirstByRoomIdOrderByCheckInTimeDesc(Long roomId);
}
