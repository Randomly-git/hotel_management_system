package com.hotel.hotel.repository;

import com.hotel.hotel.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 预订 Repository
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * 根据酒店ID查询预订列表
     */
    List<Booking> findByHotelId(Long hotelId);

    /**
     * 根据酒店ID和状态查询预订
     */
    List<Booking> findByHotelIdAndStatus(Long hotelId, Booking.BookingStatus status);

    /**
     * 根据酒店ID和客户ID查询预订
     */
    List<Booking> findByHotelIdAndCustomerId(Long hotelId, Long customerId);

    /**
     * 根据预订编号查询
     */
    Optional<Booking> findByBookingNumber(String bookingNumber);

    /**
     * 查询指定日期范围内的有效预订（不包括已取消）
     */
    @Query("SELECT b FROM Booking b WHERE b.hotelId = :hotelId " +
           "AND b.checkInDate <= :endDate " +
           "AND b.checkOutDate > :startDate " +
           "AND b.status NOT IN ('canceled', 'checked_out', 'no_show')")
    List<Booking> findActiveBookingsInDateRange(
        @Param("hotelId") Long hotelId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * 查询指定房型在日期范围内的预订
     */
    @Query("SELECT b FROM Booking b WHERE b.hotelId = :hotelId " +
           "AND b.roomTypeId = :roomTypeId " +
           "AND b.checkInDate <= :endDate " +
           "AND b.checkOutDate > :startDate " +
           "AND b.status NOT IN ('canceled', 'checked_out', 'no_show')")
    List<Booking> findBookingsByRoomTypeAndDateRange(
        @Param("hotelId") Long hotelId,
        @Param("roomTypeId") Long roomTypeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * 统计各状态预订数量
     */
    @Query("SELECT b.status, COUNT(b) FROM Booking b WHERE b.hotelId = :hotelId GROUP BY b.status")
    List<Object[]> countByStatusGroupBy(@Param("hotelId") Long hotelId);

    /**
     * 查询今日入住的预订
     */
    @Query("SELECT b FROM Booking b WHERE b.hotelId = :hotelId " +
           "AND b.checkInDate = :date " +
           "AND b.status NOT IN ('canceled', 'checked_out', 'no_show')")
    List<Booking> findTodayCheckIns(@Param("hotelId") Long hotelId, @Param("date") LocalDate date);

    /**
     * 查询今日退房的预订
     */
    @Query("SELECT b FROM Booking b WHERE b.hotelId = :hotelId " +
           "AND b.checkOutDate = :date " +
           "AND b.status NOT IN ('canceled', 'checked_out', 'no_show')")
    List<Booking> findTodayCheckOuts(@Param("hotelId") Long hotelId, @Param("date") LocalDate date);
}
