package com.hotel.hotel.controller;

import com.hotel.hotel.dto.BookingRequest;
import com.hotel.hotel.dto.BookingResponse;
import com.hotel.hotel.entity.Booking;
import com.hotel.hotel.entity.HotelRoomType;
import com.hotel.hotel.entity.Customer;
import com.hotel.hotel.entity.Room;
import com.hotel.hotel.entity.PricingRecord;
import com.hotel.hotel.repository.*;
import com.hotel.hotel.service.PricingService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 预订管理 API
 */
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class BookingController {

    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final HotelRoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;
    private final PricingService pricingService;

    /**
     * 创建预订（支持同时创建新客户）
     */
    @PostMapping
    @Transactional
    public ResponseEntity<?> createBooking(@Valid @RequestBody BookingRequest request) {
        Customer customer;

        // 处理客户逻辑：使用现有客户或创建新客户
        if (request.getCustomerId() != null) {
            // 使用现有客户
            customer = customerRepository.findById(request.getCustomerId()).orElse(null);
            if (customer == null) {
                return ResponseEntity.badRequest().body("客户不存在");
            }
        } else {
            // 创建新客户
            if (request.getCustomerName() == null || request.getCustomerName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("客户姓名不能为空");
            }

            // 检查用户名是否已存在
            boolean nameExists = customerRepository.findByHotelIdAndNameContaining(request.getHotelId(), request.getCustomerName())
                    .stream()
                    .anyMatch(c -> c.getName().equals(request.getCustomerName()));
            if (nameExists) {
                return ResponseEntity.badRequest().body("用户名已存在");
            }

            customer = Customer.builder()
                    .hotelId(request.getHotelId())
                    .name(request.getCustomerName())
                    .email(request.getCustomerEmail())
                    .phone(request.getCustomerPhone())
                    .country(request.getCustomerCountry())
                    .isRepeatedGuest(false)
                    .totalStays(0)
                    .totalCancellations(0)
                    .build();

            customer = customerRepository.save(customer);
        }

        // 验证房型是否存在
        HotelRoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElse(null);
        if (roomType == null) {
            return ResponseEntity.badRequest().body("房型不存在");
        }

        // 验证入住人数（成人 + 儿童，不包括婴儿）
        int totalGuests = (request.getAdults() != null ? request.getAdults() : 0) +
                         (request.getChildren() != null ? request.getChildren() : 0);
        if (totalGuests > roomType.getMaxOccupancy()) {
            return ResponseEntity.badRequest().body(
                String.format("入住人数(%d人)超过房型最大容量(%d人)", totalGuests, roomType.getMaxOccupancy())
            );
        }

        // 计算晚数
        int totalNights = (int) ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        if (totalNights <= 0) {
            return ResponseEntity.badRequest().body("退房日期必须晚于入住日期");
        }

        // 使用动态定价计算价格
        BigDecimal totalPrice = calculateBookingPrice(request.getRoomTypeId(), request.getCheckInDate(),
                                                     request.getCheckOutDate(), totalNights, roomType.getBasePrice());
        BigDecimal adr = totalPrice.divide(java.math.BigDecimal.valueOf(totalNights), 2, java.math.RoundingMode.HALF_UP);

        // 计算提前预订天数
        int leadTime = (int) ChronoUnit.DAYS.between(LocalDateTime.now(), request.getCheckInDate().atStartOfDay());

        // 创建预订
        Booking booking = Booking.builder()
                .hotelId(request.getHotelId())
                .customerId(request.getCustomerId())
                .roomTypeId(request.getRoomTypeId())
                .bookingNumber(generateBookingNumber())
                .leadTime(leadTime)
                .bookingDate(LocalDateTime.now())
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .totalNights(totalNights)
                .adults(request.getAdults())
                .children(request.getChildren())
                .babies(request.getBabies())
                .requiredCarParkingSpaces(request.getRequiredCarParkingSpaces())
                .totalPrice(totalPrice)
                .adr(adr)
                .depositType(request.getDepositType())
                .mealType(request.getMealType())
                .marketSegment(request.getMarketSegment())
                .distributionChannel(request.getDistributionChannel())
                .status(Booking.BookingStatus.booked)
                .requestsText(request.getRequestsText())
                .specialRequests(request.getRequestsText() != null ? 1 : 0)
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        // 加载关联信息
        savedBooking.setCustomer(customer);
        savedBooking.setRoomType(roomType);

        return ResponseEntity.ok(BookingResponse.fromEntity(savedBooking));
    }

    /**
     * 获取酒店所有预订（支持分页和多状态查询）
     */
    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<Map<String, Object>> getBookingsByHotel(
            @PathVariable Long hotelId,
            @RequestParam(required = false) String status, // 支持多个状态，用逗号分隔
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy, // 排序字段
            @RequestParam(defaultValue = "desc") String sortDir) { // 排序方向

        // 解析多状态查询
        List<Booking.BookingStatus> statuses = null;
        if (status != null && !status.trim().isEmpty()) {
            statuses = Arrays.stream(status.split(","))
                    .map(s -> Booking.BookingStatus.valueOf(s.trim()))
                    .collect(Collectors.toList());
        }

        // 创建排序规则
        Sort sort;
        if ("checkInDate".equals(sortBy)) {
            // 入住时间排序
            Direction direction = "asc".equals(sortDir) ? Direction.ASC : Direction.DESC;
            sort = Sort.by(direction, "checkInDate");
        } else {
            // 默认按创建时间倒序
            sort = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Booking> bookingPage;
        if (statuses != null && statuses.size() == 1) {
            // 单状态查询
            bookingPage = bookingRepository.findByHotelIdAndStatus(hotelId, statuses.get(0), pageable);
        } else if (statuses != null && statuses.size() > 1) {
            // 多状态查询
            bookingPage = bookingRepository.findByHotelIdAndStatusIn(hotelId, statuses, pageable);
        } else {
            // 无状态过滤
            bookingPage = bookingRepository.findByHotelId(hotelId, pageable);
        }

        List<Booking> bookings = bookingPage.getContent();

        if (bookings.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("content", List.of());
            response.put("totalElements", 0);
            response.put("totalPages", 0);
            response.put("currentPage", page);
            response.put("pageSize", size);
            return ResponseEntity.ok(response);
        }

        // 只获取需要的ID列表
        List<Long> customerIds = bookings.stream()
                .map(Booking::getCustomerId)
                .distinct()
                .toList();
        List<Long> roomTypeIds = bookings.stream()
                .map(Booking::getRoomTypeId)
                .distinct()
                .toList();
        List<Long> roomIds = bookings.stream()
                .map(Booking::getAssignedRoomId)
                .filter(id -> id != null)
                .distinct()
                .toList();

        // 批量查询关联数据
        Map<Long, Map<String, String>> customerData = new HashMap<>();
        for (Long cid : customerIds) {
            customerRepository.findById(cid).ifPresent(c -> {
                Map<String, String> data = new HashMap<>();
                data.put("name", c.getName());
                data.put("phone", c.getPhone());
                customerData.put(cid, data);
            });
        }

        Map<Long, Map<String, String>> roomTypeData = new HashMap<>();
        for (Long rtid : roomTypeIds) {
            roomTypeRepository.findById(rtid).ifPresent(rt -> {
                Map<String, String> data = new HashMap<>();
                data.put("typeName", rt.getTypeName());
                data.put("typeCode", rt.getTypeCode());
                roomTypeData.put(rtid, data);
            });
        }

        Map<Long, String> roomNumbers = new HashMap<>();
        for (Long rid : roomIds) {
            roomRepository.findById(rid).ifPresent(r -> {
                roomNumbers.put(rid, r.getRoomNumber());
            });
        }

        // 转换为Map避免序列化问题
        List<Map<String, Object>> content = bookings.stream()
                .map(booking -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", booking.getId());
                    map.put("hotelId", booking.getHotelId());
                    map.put("customerId", booking.getCustomerId());
                    map.put("roomTypeId", booking.getRoomTypeId());
                    map.put("bookingNumber", booking.getBookingNumber());
                    map.put("leadTime", booking.getLeadTime());
                    map.put("bookingDate", booking.getBookingDate());
                    map.put("checkInDate", booking.getCheckInDate());
                    map.put("checkOutDate", booking.getCheckOutDate());
                    map.put("totalNights", booking.getTotalNights());
                    map.put("adults", booking.getAdults());
                    map.put("children", booking.getChildren());
                    map.put("babies", booking.getBabies());
                    map.put("totalPrice", booking.getTotalPrice());
                    map.put("adr", booking.getAdr());
                    map.put("depositType", booking.getDepositType());
                    map.put("mealType", booking.getMealType());
                    map.put("marketSegment", booking.getMarketSegment());
                    map.put("distributionChannel", booking.getDistributionChannel());
                    map.put("status", booking.getStatus());
                    map.put("isCanceled", booking.getIsCanceled());
                    map.put("specialRequests", booking.getSpecialRequests());
                    map.put("requestsText", booking.getRequestsText());
                    map.put("assignedRoomId", booking.getAssignedRoomId());
                    map.put("createdAt", booking.getCreatedAt());
                    map.put("updatedAt", booking.getUpdatedAt());

                    // 添加关联数据
                    Map<String, String> customer = customerData.get(booking.getCustomerId());
                    if (customer != null) {
                        map.put("customerName", customer.get("name"));
                        map.put("customerPhone", customer.get("phone"));
                    }

                    Map<String, String> roomType = roomTypeData.get(booking.getRoomTypeId());
                    if (roomType != null) {
                        map.put("typeName", roomType.get("typeName"));
                        map.put("typeCode", roomType.get("typeCode"));
                    }

                    if (booking.getAssignedRoomId() != null) {
                        map.put("roomNumber", roomNumbers.get(booking.getAssignedRoomId()));
                    }

                    return map;
                })
                .collect(Collectors.toList());

        // 构建分页响应
        Map<String, Object> response = new HashMap<>();
        response.put("content", content);
        response.put("totalElements", bookingPage.getTotalElements());
        response.put("totalPages", bookingPage.getTotalPages());
        response.put("currentPage", page);
        response.put("pageSize", size);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取客户的预订列表
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByCustomer(
            @PathVariable Long customerId,
            @RequestParam(required = false) Booking.BookingStatus status) {

        List<Booking> bookings = bookingRepository.findByHotelIdAndCustomerId(1L, customerId);

        if (status != null) {
            bookings = bookings.stream()
                    .filter(b -> b.getStatus() == status)
                    .collect(Collectors.toList());
        }

        List<BookingResponse> responses = bookings.stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 获取预订详情
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long bookingId) {
        return bookingRepository.findById(bookingId)
                .map(booking -> ResponseEntity.ok(BookingResponse.fromEntity(booking)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据预订编号查询
     */
    @GetMapping("/number/{bookingNumber}")
    public ResponseEntity<BookingResponse> getBookingByNumber(@PathVariable String bookingNumber) {
        return bookingRepository.findByBookingNumber(bookingNumber)
                .map(booking -> ResponseEntity.ok(BookingResponse.fromEntity(booking)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 取消预订
     */
    @PatchMapping("/{bookingId}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Long bookingId) {
        return bookingRepository.findById(bookingId)
                .map(booking -> {
                    if (booking.getStatus() == Booking.BookingStatus.canceled) {
                        return ResponseEntity.badRequest().body("预订已取消");
                    }
                    if (booking.getStatus() == Booking.BookingStatus.completed) {
                        return ResponseEntity.badRequest().body("已退房，无法取消");
                    }

                    Long customerId = booking.getCustomerId();

                    // 取消预订
                    booking.setStatus(Booking.BookingStatus.canceled);
                    booking.setIsCanceled(true);
                    booking.setCancelDate(LocalDateTime.now());

                    // 抹除用户相关信息（保留基本预订信息）
                    booking.setCustomerId(null);
                    booking.setAssignedRoomId(null);

                    Booking savedBooking = bookingRepository.save(booking);

                    // 检查该用户是否还有其他活跃预订，如果没有则删除用户
                    if (customerId != null) {
                        List<Booking> allUserBookings = bookingRepository.findByHotelIdAndCustomerId(booking.getHotelId(), customerId);
                        boolean hasActiveBookings = allUserBookings.stream()
                                .anyMatch(b -> !"canceled".equals(b.getStatus()) && !"completed".equals(b.getStatus()));

                        if (!hasActiveBookings) {
                            // 用户没有活跃预订，删除用户
                            try {
                                customerRepository.deleteById(customerId);
                                log.info("由于用户 {} 没有活跃预订，已自动删除该用户", customerId);
                            } catch (Exception e) {
                                log.warn("删除用户 {} 时发生错误: {}", customerId, e.getMessage());
                            }
                        }
                    }

                    return ResponseEntity.ok(BookingResponse.fromEntity(savedBooking));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 获取今日入住预订
     */
    @GetMapping("/hotel/{hotelId}/today/checkins")
    public ResponseEntity<List<BookingResponse>> getTodayCheckIns(@PathVariable Long hotelId) {
        List<Booking> bookings = bookingRepository.findTodayCheckIns(hotelId, LocalDate.now());

        List<BookingResponse> responses = bookings.stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 获取今日退房预订
     */
    @GetMapping("/hotel/{hotelId}/today/checkouts")
    public ResponseEntity<List<BookingResponse>> getTodayCheckOuts(@PathVariable Long hotelId) {
        List<Booking> bookings = bookingRepository.findTodayCheckOuts(hotelId, LocalDate.now());

        List<BookingResponse> responses = bookings.stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 办理入住
     */
    @PatchMapping("/{bookingId}/checkin")
    @Operation(summary = "办理入住", description = "将预订状态更新为已入住，并分配房间")
    public ResponseEntity<?> checkIn(
            @PathVariable Long bookingId,
            @RequestParam(required = false) Long roomId) {
        return bookingRepository.findById(bookingId)
                .map(booking -> {
                    if (booking.getStatus() != Booking.BookingStatus.booked) {
                        return ResponseEntity.badRequest().body("只能为已确认的预订办理入住");
                    }
                    
                    booking.setStatus(Booking.BookingStatus.checked_in);
                    if (roomId != null) {
                        booking.setAssignedRoomId(roomId);
                        // 更新房间状态为已入住
                        roomRepository.findById(roomId).ifPresent(room -> {
                            room.setStatus(Room.RoomStatus.occupied);
                            roomRepository.save(room);
                        });
                    }
                    
                    Booking savedBooking = bookingRepository.save(booking);
                    return ResponseEntity.ok(BookingResponse.fromEntity(savedBooking));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 办理退房
     */
    @PatchMapping("/{bookingId}/checkout")
    @Operation(summary = "办理退房", description = "将预订状态更新为已退房，并释放房间")
    public ResponseEntity<?> checkOut(@PathVariable Long bookingId) {
        return bookingRepository.findById(bookingId)
                .map(booking -> {
                    if (booking.getStatus() != Booking.BookingStatus.checked_in) {
                        return ResponseEntity.badRequest().body("只能为已入住的预订办理退房");
                    }
                    
                    booking.setStatus(Booking.BookingStatus.completed);
                    
                    // 更新房间状态为清洁中
                    if (booking.getAssignedRoomId() != null) {
                        roomRepository.findById(booking.getAssignedRoomId()).ifPresent(room -> {
                            room.setStatus(Room.RoomStatus.cleaning);
                            roomRepository.save(room);
                        });
                    }
                    
                    Booking savedBooking = bookingRepository.save(booking);
                    return ResponseEntity.ok(BookingResponse.fromEntity(savedBooking));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 计算预订价格（使用动态定价）
     */
    private BigDecimal calculateBookingPrice(Long roomTypeId, LocalDate checkInDate,
                                           LocalDate checkOutDate, int totalNights, BigDecimal basePrice) {
        BigDecimal totalPrice = BigDecimal.ZERO;

        try {
            // 遍历每个入住日期，获取当天的动态价格
            LocalDate currentDate = checkInDate;
            while (currentDate.isBefore(checkOutDate)) {
                // 调用定价服务获取当天价格
                List<PricingRecord> pricingRecords = pricingService.getAppliedPricesByDate(currentDate);

                // 查找对应房型的定价记录
                BigDecimal dayPrice = basePrice; // 默认使用基准价格
                for (PricingRecord record : pricingRecords) {
                    if (record.getRoomType() != null &&
                        record.getRoomType().getId().equals(roomTypeId) &&
                        record.getEffectiveDate().equals(currentDate) &&
                        "applied".equals(record.getStatus())) {
                        dayPrice = record.getAdjustedPrice();
                        break;
                    }
                }

                totalPrice = totalPrice.add(dayPrice);
                currentDate = currentDate.plusDays(1);
            }
        } catch (Exception e) {
            // 如果动态定价失败，使用基准价格
            log.warn("动态定价计算失败，使用基准价格: {}", e.getMessage());
            totalPrice = basePrice.multiply(BigDecimal.valueOf(totalNights));
        }

        return totalPrice;
    }

    /**
     * 生成预订编号
     */
    private String generateBookingNumber() {
        return "BK" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
