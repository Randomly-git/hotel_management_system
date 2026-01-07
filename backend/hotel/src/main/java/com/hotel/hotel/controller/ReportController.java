package com.hotel.hotel.controller;

import com.hotel.hotel.entity.Booking;
import com.hotel.hotel.entity.HotelRoomType;
import com.hotel.hotel.entity.Customer;
import com.hotel.hotel.repository.BookingRepository;
import com.hotel.hotel.repository.HotelRoomTypeRepository;
import com.hotel.hotel.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 报表数据 API
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ReportController {

    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final HotelRoomTypeRepository roomTypeRepository;

    /**
     * 获取营收报表数据
     */
    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueReport(
            @RequestParam Long hotelId,
            @RequestParam(defaultValue = "month") String period) {

        LocalDateTime startDate;
        LocalDateTime endDate = LocalDateTime.now();

        switch (period.toLowerCase()) {
            case "today":
                startDate = endDate.toLocalDate().atStartOfDay();
                break;
            case "week":
                startDate = endDate.minusDays(7);
                break;
            case "month":
                startDate = endDate.minusDays(30);
                break;
            case "year":
                startDate = endDate.minusDays(365);
                break;
            default:
                startDate = endDate.minusDays(30);
        }

        // 获取指定时间范围内的已完成预订
        List<Booking> completedBookings = bookingRepository.findByHotelIdAndStatus(hotelId, Booking.BookingStatus.completed)
                .stream()
                .filter(booking -> booking.getUpdatedAt() != null &&
                        booking.getUpdatedAt().isAfter(startDate) &&
                        booking.getUpdatedAt().isBefore(endDate))
                .collect(Collectors.toList());

        BigDecimal totalRevenue = completedBookings.stream()
                .map(Booking::getTotalPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 计算客房收入（假设所有收入都是客房收入，因为目前没有餐饮等其他收入）
        BigDecimal roomRevenue = totalRevenue;
        BigDecimal foodRevenue = BigDecimal.ZERO;
        BigDecimal otherRevenue = BigDecimal.ZERO;

        // 按日期分组的明细数据
        Map<String, Map<String, Object>> dailyData = new LinkedHashMap<>();

        for (Booking booking : completedBookings) {
            if (booking.getUpdatedAt() != null) {
                String dateKey = booking.getUpdatedAt().toLocalDate().toString();

                dailyData.computeIfAbsent(dateKey, k -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("date", k);
                    data.put("roomRevenue", BigDecimal.ZERO);
                    data.put("foodRevenue", BigDecimal.ZERO);
                    data.put("otherRevenue", BigDecimal.ZERO);
                    data.put("totalRevenue", BigDecimal.ZERO);
                    data.put("occupancyRate", 0);
                    data.put("avgRoomRate", BigDecimal.ZERO);
                    return data;
                });

                Map<String, Object> dayData = dailyData.get(dateKey);
                BigDecimal currentTotal = (BigDecimal) dayData.get("totalRevenue");
                dayData.put("totalRevenue", currentTotal.add(booking.getTotalPrice() != null ? booking.getTotalPrice() : BigDecimal.ZERO));
                dayData.put("roomRevenue", dayData.get("totalRevenue"));
            }
        }

        List<Map<String, Object>> details = dailyData.values().stream()
                .sorted((a, b) -> ((String) b.get("date")).compareTo((String) a.get("date")))
                .limit(30) // 最多返回30天的数据
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("totalRevenue", totalRevenue);
        result.put("roomRevenue", roomRevenue);
        result.put("foodRevenue", foodRevenue);
        result.put("otherRevenue", otherRevenue);
        result.put("details", details);

        return ResponseEntity.ok(result);
    }

    /**
     * 获取客房报表数据
     */
    @GetMapping("/rooms")
    public ResponseEntity<Map<String, Object>> getRoomReport(
            @RequestParam Long hotelId,
            @RequestParam(defaultValue = "month") String period) {

        LocalDateTime startDate;
        LocalDateTime endDate = LocalDateTime.now();

        switch (period.toLowerCase()) {
            case "today":
                startDate = endDate.toLocalDate().atStartOfDay();
                break;
            case "week":
                startDate = endDate.minusDays(7);
                break;
            case "month":
                startDate = endDate.minusDays(30);
                break;
            case "year":
                startDate = endDate.minusDays(365);
                break;
            default:
                startDate = endDate.minusDays(30);
        }

        // 获取所有房型
        List<HotelRoomType> roomTypes = roomTypeRepository.findByHotelId(hotelId);

        // 获取指定时间范围内的预订数据
        List<Booking> bookings = bookingRepository.findByHotelId(hotelId)
                .stream()
                .filter(booking -> booking.getCreatedAt() != null &&
                        booking.getCreatedAt().isAfter(startDate) &&
                        booking.getCreatedAt().isBefore(endDate))
                .collect(Collectors.toList());

        // 按房型统计数据
        List<Map<String, Object>> roomStats = new ArrayList<>();
        for (HotelRoomType roomType : roomTypes) {
            List<Booking> roomTypeBookings = bookings.stream()
                    .filter(booking -> Objects.equals(booking.getRoomTypeId(), roomType.getId()))
                    .collect(Collectors.toList());

            long totalBookings = roomTypeBookings.size();
            long completedBookings = roomTypeBookings.stream()
                    .filter(booking -> booking.getStatus() == Booking.BookingStatus.completed)
                    .count();

            BigDecimal totalRevenue = roomTypeBookings.stream()
                    .filter(booking -> booking.getStatus() == Booking.BookingStatus.completed)
                    .map(Booking::getTotalPrice)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal avgPrice = completedBookings > 0 ?
                    totalRevenue.divide(BigDecimal.valueOf(completedBookings), 2, BigDecimal.ROUND_HALF_UP) :
                    BigDecimal.ZERO;

            Map<String, Object> stat = new HashMap<>();
            stat.put("roomType", roomType.getTypeName());
            stat.put("totalBookings", totalBookings);
            stat.put("completedBookings", completedBookings);
            stat.put("totalRevenue", totalRevenue);
            stat.put("avgPrice", avgPrice);
            stat.put("occupancyRate", 0.0); // 暂时设为0，后续可以计算真实的入住率

            roomStats.add(stat);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("roomStats", roomStats);

        return ResponseEntity.ok(result);
    }

    /**
     * 获取客户报表数据
     */
    @GetMapping("/customers")
    public ResponseEntity<Map<String, Object>> getCustomerReport(
            @RequestParam Long hotelId,
            @RequestParam(defaultValue = "month") String period) {

        LocalDateTime startDate;
        LocalDateTime endDate = LocalDateTime.now();

        switch (period.toLowerCase()) {
            case "today":
                startDate = endDate.toLocalDate().atStartOfDay();
                break;
            case "week":
                startDate = endDate.minusDays(7);
                break;
            case "month":
                startDate = endDate.minusDays(30);
                break;
            case "year":
                startDate = endDate.minusDays(365);
                break;
            default:
                startDate = endDate.minusDays(30);
        }

        // 获取指定时间范围内的客户
        List<Customer> customers = customerRepository.findByHotelId(hotelId)
                .stream()
                .filter(customer -> customer.getCreatedAt() != null &&
                        customer.getCreatedAt().isAfter(startDate) &&
                        customer.getCreatedAt().isBefore(endDate))
                .collect(Collectors.toList());

        // 获取这些客户的预订数据
        List<Long> customerIds = customers.stream()
                .map(Customer::getId)
                .collect(Collectors.toList());

        List<Booking> customerBookings = new ArrayList<>();
        if (!customerIds.isEmpty()) {
            customerBookings = bookingRepository.findByHotelId(hotelId)
                    .stream()
                    .filter(booking -> booking.getCustomerId() != null &&
                            customerIds.contains(booking.getCustomerId()) &&
                            booking.getCreatedAt() != null &&
                            booking.getCreatedAt().isAfter(startDate) &&
                            booking.getCreatedAt().isBefore(endDate))
                    .collect(Collectors.toList());
        }

        // 按客户统计数据
        List<Map<String, Object>> customerStats = new ArrayList<>();
        for (Customer customer : customers) {
            List<Booking> customerBookingList = customerBookings.stream()
                    .filter(booking -> Objects.equals(booking.getCustomerId(), customer.getId()))
                    .collect(Collectors.toList());

            long totalBookings = customerBookingList.size();
            long completedBookings = customerBookingList.stream()
                    .filter(booking -> booking.getStatus() == Booking.BookingStatus.completed)
                    .count();

            BigDecimal totalSpent = customerBookingList.stream()
                    .filter(booking -> booking.getStatus() == Booking.BookingStatus.completed)
                    .map(Booking::getTotalPrice)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal avgSpent = completedBookings > 0 ?
                    totalSpent.divide(BigDecimal.valueOf(completedBookings), 2, BigDecimal.ROUND_HALF_UP) :
                    BigDecimal.ZERO;

            Map<String, Object> stat = new HashMap<>();
            stat.put("customerId", customer.getId());
            stat.put("customerName", customer.getName());
            stat.put("totalBookings", totalBookings);
            stat.put("completedBookings", completedBookings);
            stat.put("totalSpent", totalSpent);
            stat.put("avgSpent", avgSpent);
            stat.put("vipLevel", customer.getVipLevel());

            customerStats.add(stat);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("customerStats", customerStats);

        return ResponseEntity.ok(result);
    }
}
