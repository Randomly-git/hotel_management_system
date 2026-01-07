package com.hotel.hotel.scheduler;

import com.hotel.hotel.entity.Booking;
import com.hotel.hotel.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingStatusScheduler {

    private final BookingRepository bookingRepository;

    /**
     * 每天中午12点执行预约状态检查和更新
     */
    @Scheduled(cron = "0 0 12 * * ?")
    public void checkAndUpdateBookingStatuses() {
        log.info("开始执行每日预约状态检查任务...");

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        try {
            // 1. 自动取消逾期未入住的预约
            // 预约日期是昨天或更早，且状态还是booked的，自动取消
            LocalDate yesterday = today.minusDays(1);
            cancelExpiredBookings(yesterday);

            // 2. 自动完成超期未退房的预约
            // 退房日期是昨天或更早，且状态还是checked_in的，自动完成
            completeExpiredCheckouts(yesterday);

            log.info("每日预约状态检查任务执行成功");
        } catch (Exception e) {
            log.error("每日预约状态检查任务执行失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 取消逾期未入住的预约
     * @param cutoffDate 截止日期（包含这一天）
     */
    private void cancelExpiredBookings(LocalDate cutoffDate) {
        log.info("检查逾期未入住的预约，截止日期: {}", cutoffDate);

        List<Booking> expiredBookings = bookingRepository.findAll().stream()
                .filter(booking -> {
                    // 只处理已预约状态的
                    if (!"booked".equals(booking.getStatus())) {
                        return false;
                    }

                    // 检查预约日期是否已过期
                    LocalDate checkInDate = booking.getCheckInDate();
                    return checkInDate.isBefore(cutoffDate) || checkInDate.isEqual(cutoffDate);
                })
                .toList();

        log.info("发现 {} 个逾期未入住的预约", expiredBookings.size());

        for (Booking booking : expiredBookings) {
            try {
                log.info("自动取消逾期预约: 预订号={}, 入住日期={}, 客户ID={}",
                        booking.getBookingNumber(), booking.getCheckInDate(), booking.getCustomerId());

                booking.setStatus(Booking.BookingStatus.canceled);
                booking.setIsCanceled(true);
                booking.setCancelDate(LocalDateTime.now());

                // 清除客户和房间关联
                booking.setCustomerId(null);
                booking.setAssignedRoomId(null);

                bookingRepository.save(booking);
            } catch (Exception e) {
                log.error("取消逾期预约失败, 预订ID: {}, 错误: {}", booking.getId(), e.getMessage());
            }
        }
    }

    /**
     * 自动完成超期未退房的预约
     * @param cutoffDate 截止日期（包含这一天）
     */
    private void completeExpiredCheckouts(LocalDate cutoffDate) {
        log.info("检查超期未退房的预约，截止日期: {}", cutoffDate);

        List<Booking> expiredCheckouts = bookingRepository.findAll().stream()
                .filter(booking -> {
                    // 只处理已入住状态的
                    if (!"checked_in".equals(booking.getStatus())) {
                        return false;
                    }

                    // 检查退房日期是否已过期
                    LocalDate checkOutDate = booking.getCheckOutDate();
                    return checkOutDate.isBefore(cutoffDate) || checkOutDate.isEqual(cutoffDate);
                })
                .toList();

        log.info("发现 {} 个超期未退房的预约", expiredCheckouts.size());

        for (Booking booking : expiredCheckouts) {
            try {
                log.info("自动完成超期退房: 预订号={}, 退房日期={}, 客户ID={}",
                        booking.getBookingNumber(), booking.getCheckOutDate(), booking.getCustomerId());

                booking.setStatus(Booking.BookingStatus.completed);
                booking.setActualCheckOutDate(cutoffDate);

                bookingRepository.save(booking);
            } catch (Exception e) {
                log.error("自动完成超期退房失败, 预订ID: {}, 错误: {}", booking.getId(), e.getMessage());
            }
        }
    }
}
