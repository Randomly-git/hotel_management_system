package com.hotel.hotel.controller;

import com.hotel.hotel.entity.Booking;
import com.hotel.hotel.entity.HotelRoomType;
import com.hotel.hotel.entity.Customer;
import com.hotel.hotel.repository.BookingRepository;
import com.hotel.hotel.repository.HotelRoomTypeRepository;
import com.hotel.hotel.repository.CustomerRepository;
import com.hotel.hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final RoomRepository roomRepository;

    /**
     * 获取营收报表数据
     */
    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueReport(
            @RequestParam Long hotelId,
            @RequestParam(defaultValue = "month") String period) {

        LocalDateTime trendStartDate;
        LocalDateTime trendEndDate = LocalDateTime.now();

        switch (period.toLowerCase()) {
            case "today":
                trendStartDate = trendEndDate.toLocalDate().atStartOfDay();
                break;
            case "week":
                trendStartDate = trendEndDate.minusDays(7);
                break;
            case "month":
                trendStartDate = trendEndDate.minusDays(30);
                break;
            case "year":
                trendStartDate = trendEndDate.minusDays(365);
                break;
            default:
                trendStartDate = trendEndDate.minusDays(30);
        }

        // 获取指定时间范围内的已完成预订
        List<Booking> completedBookings = bookingRepository.findByHotelIdAndStatus(hotelId, Booking.BookingStatus.completed)
                .stream()
                .filter(booking -> booking.getUpdatedAt() != null &&
                        booking.getUpdatedAt().isAfter(trendStartDate) &&
                        booking.getUpdatedAt().isBefore(trendEndDate))
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

        LocalDateTime trendStartDate;
        LocalDateTime trendEndDate = LocalDateTime.now();

        switch (period.toLowerCase()) {
            case "today":
                trendStartDate = trendEndDate.toLocalDate().atStartOfDay();
                break;
            case "week":
                trendStartDate = trendEndDate.minusDays(7);
                break;
            case "month":
                trendStartDate = trendEndDate.minusDays(30);
                break;
            case "year":
                trendStartDate = trendEndDate.minusDays(365);
                break;
            default:
                trendStartDate = trendEndDate.minusDays(30);
        }

        // 获取所有房型
        List<HotelRoomType> roomTypes = roomTypeRepository.findByHotelId(hotelId);

        // 获取当前所有房间，按房型分组统计房间数量
        List<com.hotel.hotel.entity.Room> allRooms = roomRepository.findByHotelId(hotelId);
        Map<Long, Long> roomCountByType = allRooms.stream()
                .filter(room -> room.getRoomTypeId() != null)
                .collect(Collectors.groupingBy(
                        com.hotel.hotel.entity.Room::getRoomTypeId,
                        Collectors.counting()
                ));

        // 获取当前入住的房间（状态为occupied）
        Map<Long, Long> occupiedRoomCountByType = allRooms.stream()
                .filter(room -> room.getRoomTypeId() != null &&
                        room.getStatus() == com.hotel.hotel.entity.Room.RoomStatus.occupied)
                .collect(Collectors.groupingBy(
                        com.hotel.hotel.entity.Room::getRoomTypeId,
                        Collectors.counting()
                ));



        // 获取指定时间范围内的预订数据（排除已取消和已完成的）
        List<Booking> activeBookings = new ArrayList<>();
        activeBookings.addAll(bookingRepository.findByHotelIdAndStatus(hotelId, Booking.BookingStatus.booked));
        activeBookings.addAll(bookingRepository.findByHotelIdAndStatus(hotelId, Booking.BookingStatus.checked_in));

        // 扩大查询范围到90天，以便与趋势图保持一致
        LocalDateTime extendedStartDate = trendEndDate.minusDays(90);

        List<Booking> bookings = activeBookings.stream()
                .filter(booking -> booking.getCreatedAt() != null &&
                        booking.getCreatedAt().isAfter(extendedStartDate) &&
                        booking.getCreatedAt().isBefore(trendEndDate))
                .collect(Collectors.toList());

        // 按房型统计数据
        List<Map<String, Object>> roomStats = new ArrayList<>();
        long totalOccupiedFromRoomTypes = 0;
        long totalRoomsFromRoomTypes = 0;

        for (HotelRoomType roomType : roomTypes) {
            Long roomTypeId = roomType.getId();

            // 获取该房型的房间总数
            long totalRooms = roomCountByType.getOrDefault(roomTypeId, 0L);

            // 获取该房型的当前入住房间数
            long occupiedRooms = occupiedRoomCountByType.getOrDefault(roomTypeId, 0L);

            // 计算入住率
            double occupancyRate = totalRooms > 0 ? Math.round((occupiedRooms * 100.0 / totalRooms) * 10.0) / 10.0 : 0.0;

            totalOccupiedFromRoomTypes += occupiedRooms;
            totalRoomsFromRoomTypes += totalRooms;

            // 获取该房型的预订数据
            List<Booking> roomTypeBookings = bookings.stream()
                    .filter(booking -> Objects.equals(booking.getRoomTypeId(), roomTypeId))
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
                    totalRevenue.divide(BigDecimal.valueOf(completedBookings), 2, RoundingMode.HALF_UP) :
                    BigDecimal.ZERO;

            Map<String, Object> stat = new HashMap<>();
            stat.put("typeName", roomType.getTypeName());
            stat.put("totalRooms", totalRooms);
            stat.put("occupiedRooms", occupiedRooms);
            stat.put("occupancyRate", occupancyRate);
            stat.put("avgPrice", avgPrice);
            stat.put("revenue", totalRevenue);

            roomStats.add(stat);
        }

        // 按入住率降序排序，添加排名
        roomStats.sort((a, b) -> Double.compare((Double) b.get("occupancyRate"), (Double) a.get("occupancyRate")));
        for (int i = 0; i < roomStats.size(); i++) {
            roomStats.get(i).put("rank", i + 1);
        }

        // 计算过去3个月每种房型的每日入住情况（包括12月数据）
        LocalDate occupancyTrendEndDate = LocalDate.now();
        LocalDate occupancyTrendStartDate = occupancyTrendEndDate.minusDays(90);

        // 获取房型ID到名称的映射
        Map<Long, String> roomTypeIdToName = roomTypes.stream()
                .collect(Collectors.toMap(HotelRoomType::getId, HotelRoomType::getTypeName));

        // 查询所有与这个月有关联的预订
        List<Booking> relevantBookings = new ArrayList<>();

        // 1. 已完成的预订
        relevantBookings.addAll(bookingRepository.findByHotelIdAndStatus(hotelId, Booking.BookingStatus.completed));

        // 2. 正在进行的预订（checked_in状态）
        relevantBookings.addAll(bookingRepository.findByHotelIdAndStatus(hotelId, Booking.BookingStatus.checked_in));

        // 3. 已预订但还未入住的（booked状态），如果入住时间在本月范围内
        relevantBookings.addAll(bookingRepository.findByHotelIdAndStatus(hotelId, Booking.BookingStatus.booked));

        // 过滤出与本月有关联的预订（实际退房时间 > 本月开始，以及入住时间 < 今天）
        relevantBookings = relevantBookings.stream()
                .filter(booking -> booking.getCheckInDate() != null && booking.getCheckOutDate() != null)
                .filter(booking -> !booking.getCheckOutDate().isBefore(occupancyTrendStartDate) &&
                        !booking.getCheckInDate().isAfter(occupancyTrendEndDate))
                .collect(Collectors.toList());


        // 计算每日每种房型的入住情况
        Map<String, Map<String, Object>> roomTypeTrendData = new LinkedHashMap<>();

        // 初始化每种房型的趋势数据结构
        for (HotelRoomType roomType : roomTypes) {
            String typeName = roomType.getTypeName();
            Map<String, Object> trend = new HashMap<>();
            trend.put("roomType", typeName);
            trend.put("data", new ArrayList<Map<String, Object>>());
            roomTypeTrendData.put(typeName, trend);
        }

        // 按照预订的起止时间，给他们"经过"的每一天的房型入住量+1
        for (LocalDate date = occupancyTrendStartDate; !date.isAfter(occupancyTrendEndDate); date = date.plusDays(1)) {
            final LocalDate currentDate = date;
            String dateStr = currentDate.toString();

            // 统计每个房型在这一天的入住数量
            Map<Long, Long> occupiedByType = relevantBookings.stream()
                    .filter(booking -> !booking.getCheckInDate().isAfter(currentDate) &&
                            booking.getCheckOutDate().isAfter(currentDate))
                    .filter(booking -> booking.getRoomTypeId() != null)
                    .collect(Collectors.groupingBy(Booking::getRoomTypeId, Collectors.counting()));

            // 为每种房型添加当天的数据
            for (HotelRoomType roomType : roomTypes) {
                Long roomTypeId = roomType.getId();
                String typeName = roomType.getTypeName();

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) roomTypeTrendData.get(typeName).get("data");
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", dateStr);
                dayData.put("occupied", occupiedByType.getOrDefault(roomTypeId, 0L));
                dataList.add(dayData);
            }
        }

        // 计算总体入住率趋势
        List<Map<String, Object>> overallOccupancyTrend = new ArrayList<>();
        long totalRooms = allRooms.size();

        for (LocalDate date = occupancyTrendStartDate; !date.isAfter(occupancyTrendEndDate); date = date.plusDays(1)) {
            final LocalDate currentDate = date;
            String dateStr = currentDate.toString();

            // 统计在这个日期"经过"的预订数量（入住时间 <= 日期 < 退房时间）
            long occupiedRooms = relevantBookings.stream()
                    .filter(booking -> !booking.getCheckInDate().isAfter(currentDate) &&
                            booking.getCheckOutDate().isAfter(currentDate))
                    .count();

            double occupancyRate = totalRooms > 0 ? Math.round((occupiedRooms * 100.0 / totalRooms) * 10.0) / 10.0 : 0.0;

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", dateStr);
            dayData.put("occupancyRate", occupancyRate);
            dayData.put("occupiedRooms", occupiedRooms);
            dayData.put("totalRooms", totalRooms);
            overallOccupancyTrend.add(dayData);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("roomStats", roomStats);
        result.put("roomTypeTrends", new ArrayList<>(roomTypeTrendData.values()));
        result.put("overallOccupancyTrend", overallOccupancyTrend);


        return ResponseEntity.ok(result);
    }

    /**
     * 获取客户增长趋势数据
     */
    @GetMapping("/customers/growth-trend")
    public ResponseEntity<Map<String, Object>> getCustomerGrowthTrend(@RequestParam Long hotelId) {
        // 获取过去3个月的日期范围
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(90);

        // 获取所有预订记录，按客户分组，找到每个客户第一次入住的时间
        List<Booking> allBookings = bookingRepository.findAll().stream()
                .filter(booking -> booking.getHotelId().equals(hotelId) &&
                        booking.getCustomerId() != null &&
                        booking.getCheckInDate() != null)
                .collect(Collectors.toList());

        // 按客户ID分组，找到每个客户第一次入住的日期
        Map<Long, LocalDate> firstStayByCustomer = new HashMap<>();
        for (Booking booking : allBookings) {
            Long customerId = booking.getCustomerId();
            LocalDate checkInDate = booking.getCheckInDate();

            if (!firstStayByCustomer.containsKey(customerId) ||
                checkInDate.isBefore(firstStayByCustomer.get(customerId))) {
                firstStayByCustomer.put(customerId, checkInDate);
            }
        }

        // 按月份统计新增客户数量
        Map<String, Long> monthlyGrowth = new LinkedHashMap<>();

        // 初始化每个月的计数为0
        LocalDate current = startDate.withDayOfMonth(1);
        while (!current.isAfter(endDate)) {
            String monthKey = current.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            monthlyGrowth.put(monthKey, 0L);
            current = current.plusMonths(1);
        }

        // 统计每个月第一次入住的客户数量
        for (Map.Entry<Long, LocalDate> entry : firstStayByCustomer.entrySet()) {
            LocalDate firstStayDate = entry.getValue();
            if (!firstStayDate.isBefore(startDate) && !firstStayDate.isAfter(endDate)) {
                LocalDate customerMonth = firstStayDate.withDayOfMonth(1);
                String monthKey = customerMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));

                if (monthlyGrowth.containsKey(monthKey)) {
                    monthlyGrowth.put(monthKey, monthlyGrowth.get(monthKey) + 1);
                }
            }
        }

        // 转换为前端需要的格式
        List<Map<String, Object>> trendData = monthlyGrowth.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("month", entry.getKey());
                    data.put("newCustomers", entry.getValue());
                    return data;
                })
                .collect(Collectors.toList());

        // 计算累计客户数量（基于实际入住过的客户）
        long cumulativeCount = 0;
        for (Map<String, Object> data : trendData) {
            cumulativeCount += (Long) data.get("newCustomers");
            data.put("totalCustomers", cumulativeCount);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("growthTrend", trendData);

        return ResponseEntity.ok(result);
    }

    /**
     * 获取客户报表数据
     */
    @GetMapping("/customers")
    public ResponseEntity<Map<String, Object>> getCustomerReport(
            @RequestParam Long hotelId,
            @RequestParam(defaultValue = "month") String period) {

        LocalDateTime trendStartDate;
        LocalDateTime trendEndDate = LocalDateTime.now();

        switch (period.toLowerCase()) {
            case "today":
                trendStartDate = trendEndDate.toLocalDate().atStartOfDay();
                break;
            case "week":
                trendStartDate = trendEndDate.minusDays(7);
                break;
            case "month":
                trendStartDate = trendEndDate.minusDays(30);
                break;
            case "year":
                trendStartDate = trendEndDate.minusDays(365);
                break;
            default:
                trendStartDate = trendEndDate.minusDays(30);
        }

        // 获取指定时间范围内的客户
        List<Customer> customers = customerRepository.findByHotelId(hotelId)
                .stream()
                .filter(customer -> customer.getCreatedAt() != null &&
                        customer.getCreatedAt().isAfter(trendStartDate) &&
                        customer.getCreatedAt().isBefore(trendEndDate))
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
                            booking.getCreatedAt().isAfter(trendStartDate) &&
                            booking.getCreatedAt().isBefore(trendEndDate))
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
                    totalSpent.divide(BigDecimal.valueOf(completedBookings), 2, RoundingMode.HALF_UP) :
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
