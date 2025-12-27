package com.hotel.hotel.controller;

import com.hotel.hotel.dto.BookingRequest;
import com.hotel.hotel.dto.BookingResponse;
import com.hotel.hotel.entity.Booking;
import com.hotel.hotel.entity.HotelRoomType;
import com.hotel.hotel.entity.Customer;
import com.hotel.hotel.repository.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 预订管理 API
 */
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final HotelRoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;

    /**
     * 创建预订
     */
    @PostMapping
    public ResponseEntity<?> createBooking(@Valid @RequestBody BookingRequest request) {
        // 验证客户是否存在
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElse(null);
        if (customer == null) {
            return ResponseEntity.badRequest().body("客户不存在");
        }

        // 验证房型是否存在
        HotelRoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElse(null);
        if (roomType == null) {
            return ResponseEntity.badRequest().body("房型不存在");
        }

        // 验证入住人数
        if (request.getAdults() + request.getChildren() > roomType.getMaxOccupancy()) {
            return ResponseEntity.badRequest().body("入住人数超过房型最大容量");
        }

        // 计算晚数和价格
        int totalNights = (int) ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        if (totalNights <= 0) {
            return ResponseEntity.badRequest().body("退房日期必须晚于入住日期");
        }

        BigDecimal totalPrice = roomType.getBasePrice()
                .multiply(java.math.BigDecimal.valueOf(totalNights));
        BigDecimal adr = roomType.getBasePrice();

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
                .status(Booking.BookingStatus.confirmed)
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
     * 获取酒店所有预订
     */
    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByHotel(
            @PathVariable Long hotelId,
            @RequestParam(required = false) Booking.BookingStatus status) {

        List<Booking> bookings;
        if (status != null) {
            bookings = bookingRepository.findByHotelIdAndStatus(hotelId, status);
        } else {
            bookings = bookingRepository.findByHotelId(hotelId);
        }

        // 加载关联信息
        bookings.forEach(this::loadBookingRelations);

        List<BookingResponse> responses = bookings.stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
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

        bookings.forEach(this::loadBookingRelations);

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
                .map(booking -> {
                    loadBookingRelations(booking);
                    return ResponseEntity.ok(BookingResponse.fromEntity(booking));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据预订编号查询
     */
    @GetMapping("/number/{bookingNumber}")
    public ResponseEntity<BookingResponse> getBookingByNumber(@PathVariable String bookingNumber) {
        return bookingRepository.findByBookingNumber(bookingNumber)
                .map(booking -> {
                    loadBookingRelations(booking);
                    return ResponseEntity.ok(BookingResponse.fromEntity(booking));
                })
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
                    if (booking.getStatus() == Booking.BookingStatus.checked_out) {
                        return ResponseEntity.badRequest().body("已退房，无法取消");
                    }

                    booking.setStatus(Booking.BookingStatus.canceled);
                    booking.setIsCanceled(true);
                    booking.setCancelDate(LocalDateTime.now());

                    Booking savedBooking = bookingRepository.save(booking);
                    loadBookingRelations(savedBooking);

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

        bookings.forEach(this::loadBookingRelations);

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

        bookings.forEach(this::loadBookingRelations);

        List<BookingResponse> responses = bookings.stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 生成预订编号
     */
    private String generateBookingNumber() {
        return "BK" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    /**
     * 加载预订的关联信息
     */
    private void loadBookingRelations(Booking booking) {
        customerRepository.findById(booking.getCustomerId()).ifPresent(booking::setCustomer);
        roomTypeRepository.findById(booking.getRoomTypeId()).ifPresent(booking::setRoomType);
        if (booking.getAssignedRoomId() != null) {
            roomRepository.findById(booking.getAssignedRoomId()).ifPresent(booking::setAssignedRoom);
        }
    }
}
