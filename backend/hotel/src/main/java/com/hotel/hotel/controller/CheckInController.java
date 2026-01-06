package com.hotel.hotel.controller;

import com.hotel.hotel.entity.Booking;
import com.hotel.hotel.entity.CheckInRecord;
import com.hotel.hotel.entity.Room;
import com.hotel.hotel.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 入住/退房管理 API
 */
@RestController
@RequestMapping("/api/check-in")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CheckInController {

    private final CheckInRecordRepository checkInRecordRepository;
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;

    /**
     * 办理入住
     */
    @PostMapping("/booking/{bookingId}")
    public ResponseEntity<?> checkIn(@PathVariable Long bookingId,
                                     @RequestBody Map<String, Object> request) {
        // 1. 验证预订是否存在
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            return ResponseEntity.badRequest().body("预订不存在");
        }

        // 2. 验证预订状态
        if (booking.getStatus() == Booking.BookingStatus.canceled) {
            return ResponseEntity.badRequest().body("已取消的预订无法入住");
        }
        if (booking.getStatus() == Booking.BookingStatus.completed) {
            return ResponseEntity.badRequest().body("该预订已退房");
        }
        if (booking.getStatus() == Booking.BookingStatus.checked_in) {
            return ResponseEntity.badRequest().body("该预订已入住");
        }

        // 3. 获取房间ID（从前端传入或自动分配）
        Long roomId;
        if (request.containsKey("roomId")) {
            roomId = Long.valueOf(request.get("roomId").toString());
        } else {
            // 自动分配可用房间
            roomId = allocateAvailableRoom(booking.getHotelId(), booking.getRoomTypeId());
            if (roomId == null) {
                return ResponseEntity.badRequest().body("没有可用的房间");
            }
        }

        // 4. 验证房间是否可用
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            return ResponseEntity.badRequest().body("房间不存在");
        }

        // 检查房间是否已有入住记录
        if (checkInRecordRepository.findByRoomIdAndStatus(roomId, CheckInRecord.CheckInStatus.active).isPresent()) {
            return ResponseEntity.badRequest().body("该房间已有客人入住");
        }

        // 5. 创建入住记录
        CheckInRecord checkInRecord = CheckInRecord.builder()
                .hotelId(booking.getHotelId())
                .bookingId(booking.getId())
                .roomId(roomId)
                .customerId(booking.getCustomerId())
                .checkInTime(LocalDateTime.now())
                .scheduledCheckOutDate(booking.getCheckOutDate())
                .actualPrice(booking.getTotalPrice() != null ? booking.getTotalPrice() : java.math.BigDecimal.ZERO)
                .status(CheckInRecord.CheckInStatus.active)
                .notes(request.getOrDefault("notes", "").toString())
                .build();

        checkInRecordRepository.save(checkInRecord);

        // 6. 更新预订状态
        booking.setStatus(Booking.BookingStatus.checked_in);
        booking.setAssignedRoomId(roomId);
        bookingRepository.save(booking);

        // 7. 更新房间状态
        room.setStatus(Room.RoomStatus.occupied);
        roomRepository.save(room);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "入住办理成功");
        response.put("checkInRecordId", checkInRecord.getId());
        response.put("roomNumber", room.getRoomNumber());

        return ResponseEntity.ok(response);
    }

    /**
     * 办理退房
     */
    @PostMapping("/checkout/{checkInRecordId}")
    public ResponseEntity<?> checkOut(@PathVariable Long checkInRecordId,
                                      @RequestBody(required = false) Map<String, Object> request) {
        // 1. 查找入住记录
        CheckInRecord checkInRecord = checkInRecordRepository.findById(checkInRecordId).orElse(null);
        if (checkInRecord == null) {
            return ResponseEntity.badRequest().body("入住记录不存在");
        }

        // 2. 验证状态
        if (checkInRecord.getStatus() == CheckInRecord.CheckInStatus.completed) {
            return ResponseEntity.badRequest().body("该客人已退房");
        }
        if (checkInRecord.getStatus() != CheckInRecord.CheckInStatus.active) {
            return ResponseEntity.badRequest().body("无效的入住状态");
        }

        // 3. 更新入住记录
        checkInRecord.setCheckOutTime(LocalDateTime.now());

        // 判断是否提前退房
        if (LocalDate.now().isBefore(checkInRecord.getScheduledCheckOutDate())) {
            checkInRecord.setStatus(CheckInRecord.CheckInStatus.early_checkout);
        } else {
            checkInRecord.setStatus(CheckInRecord.CheckInStatus.completed);
        }

        // 如果有价格调整
        if (request != null && request.containsKey("actualPrice")) {
            try {
                double actualPrice = Double.parseDouble(request.get("actualPrice").toString());
                checkInRecord.setActualPrice(java.math.BigDecimal.valueOf(actualPrice));
                if (request.containsKey("priceAdjustmentReason")) {
                    checkInRecord.setPriceAdjustmentReason(request.get("priceAdjustmentReason").toString());
                }
            } catch (NumberFormatException e) {
                // 忽略价格解析错误
            }
        }

        // 如果有备注
        if (request != null && request.containsKey("notes")) {
            checkInRecord.setNotes(request.get("notes").toString());
        }

        checkInRecordRepository.save(checkInRecord);

        // 4. 更新关联的预订状态
        Booking booking = bookingRepository.findById(checkInRecord.getBookingId()).orElse(null);
        if (booking != null) {
            booking.setStatus(Booking.BookingStatus.completed);
            bookingRepository.save(booking);
        }

        // 5. 释放房间
        Room room = roomRepository.findById(checkInRecord.getRoomId()).orElse(null);
        if (room != null) {
            room.setStatus(Room.RoomStatus.available);
            roomRepository.save(room);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "退房办理成功");
        response.put("checkOutTime", checkInRecord.getCheckOutTime());

        return ResponseEntity.ok(response);
    }

    /**
     * 根据预订号办理退房
     */
    @PostMapping("/checkout/by-booking-number/{bookingNumber}")
    public ResponseEntity<?> checkOutByBookingNumber(@PathVariable String bookingNumber,
                                                      @RequestBody(required = false) Map<String, Object> request) {
        // 1. 查找预订
        Booking booking = bookingRepository.findByBookingNumber(bookingNumber).orElse(null);
        if (booking == null) {
            return ResponseEntity.badRequest().body("预订不存在");
        }

        // 2. 查找入住记录
        CheckInRecord checkInRecord = checkInRecordRepository.findByBookingId(booking.getId()).orElse(null);
        if (checkInRecord == null) {
            return ResponseEntity.badRequest().body("未找到入住记录");
        }

        // 3. 执行退房
        return checkOut(checkInRecord.getId(), request);
    }

    /**
     * 根据房间号办理退房
     */
    @PostMapping("/checkout/by-room-number/{roomNumber}")
    public ResponseEntity<?> checkOutByRoomNumber(@PathVariable String roomNumber,
                                                   @RequestBody(required = false) Map<String, Object> request) {
        // 1. 查找房间
        Room room = roomRepository.findByRoomNumber(roomNumber).orElse(null);
        if (room == null) {
            return ResponseEntity.badRequest().body("房间不存在");
        }

        // 2. 查找入住记录
        CheckInRecord checkInRecord = checkInRecordRepository.findByRoomIdAndStatus(
                room.getId(), CheckInRecord.CheckInStatus.active).orElse(null);
        if (checkInRecord == null) {
            return ResponseEntity.badRequest().body("该房间没有活跃的入住记录");
        }

        // 3. 执行退房
        return checkOut(checkInRecord.getId(), request);
    }

    /**
     * 获取当前在住的客人列表
     */
    @GetMapping("/active/{hotelId}")
    public ResponseEntity<List<Map<String, Object>>> getActiveCheckIns(@PathVariable Long hotelId) {
        List<CheckInRecord> checkIns = checkInRecordRepository.findByHotelIdAndStatus(hotelId, CheckInRecord.CheckInStatus.active);

        List<Map<String, Object>> result = checkIns.stream()
                .map(cir -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("checkInRecordId", cir.getId());
                    map.put("id", cir.getId());
                    map.put("bookingId", cir.getBookingId());
                    map.put("roomId", cir.getRoomId());
                    map.put("customerId", cir.getCustomerId());
                    map.put("checkInTime", cir.getCheckInTime());
                    map.put("scheduledCheckOutDate", cir.getScheduledCheckOutDate());
                    map.put("actualPrice", cir.getActualPrice());

                    try {
                        // 获取关联数据 - 只获取基本信息,避免懒加载问题
                        bookingRepository.findById(cir.getBookingId()).ifPresent(booking -> {
                            map.put("bookingNumber", booking.getBookingNumber());
                        });

                        roomRepository.findById(cir.getRoomId()).ifPresent(room -> {
                            map.put("roomNumber", room.getRoomNumber());
                        });
                    } catch (Exception e) {
                        // 忽略关联数据加载错误
                    }

                    return map;
                })
                .toList();

        return ResponseEntity.ok(result);
    }

    /**
     * 自动分配可用房间
     */
    private Long allocateAvailableRoom(Long hotelId, Long roomTypeId) {
        List<Room> availableRooms = roomRepository.findByHotelIdAndStatusAndRoomTypeId(
                hotelId, Room.RoomStatus.available, roomTypeId);

        if (availableRooms.isEmpty()) {
            return null;
        }

        // 返回第一个可用房间
        return availableRooms.get(0).getId();
    }

    /**
     * 根据预订号查询入住信息
     */
    @GetMapping("/booking-number/{bookingNumber}")
    public ResponseEntity<Map<String, Object>> getCheckInInfoByBookingNumber(@PathVariable String bookingNumber) {
        Booking booking = bookingRepository.findByBookingNumber(bookingNumber).orElse(null);
        if (booking == null) {
            return ResponseEntity.notFound().build();
        }

        CheckInRecord checkInRecord = checkInRecordRepository.findByBookingId(booking.getId()).orElse(null);

        Map<String, Object> response = new HashMap<>();
        response.put("bookingId", booking.getId());
        response.put("bookingNumber", booking.getBookingNumber());
        response.put("customerName", booking.getCustomer() != null ? booking.getCustomer().getName() : "");
        response.put("checkInDate", booking.getCheckInDate());
        response.put("checkOutDate", booking.getCheckOutDate());
        response.put("status", booking.getStatus());

        if (checkInRecord != null) {
            response.put("checkInRecordId", checkInRecord.getId());
            response.put("checkInTime", checkInRecord.getCheckInTime());
            response.put("roomId", checkInRecord.getRoomId());
            if (checkInRecord.getRoom() != null) {
                response.put("roomNumber", checkInRecord.getRoom().getRoomNumber());
            }
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 根据房间号查询入住信息
     */
    @GetMapping("/room-number/{roomNumber}")
    public ResponseEntity<Map<String, Object>> getCheckInInfoByRoomNumber(@PathVariable String roomNumber) {
        Room room = roomRepository.findByRoomNumber(roomNumber).orElse(null);
        if (room == null) {
            return ResponseEntity.notFound().build();
        }

        CheckInRecord checkInRecord = checkInRecordRepository.findByRoomIdAndStatus(
                room.getId(), CheckInRecord.CheckInStatus.active).orElse(null);

        if (checkInRecord == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("roomNumber", roomNumber);
            response.put("status", "available");
            response.put("message", "该房间当前可用");
            return ResponseEntity.ok(response);
        }

        Booking booking = bookingRepository.findById(checkInRecord.getBookingId()).orElse(null);

        Map<String, Object> response = new HashMap<>();
        response.put("checkInRecordId", checkInRecord.getId());
        response.put("roomNumber", roomNumber);
        response.put("status", "occupied");

        if (booking != null) {
            response.put("bookingId", booking.getId());
            response.put("bookingNumber", booking.getBookingNumber());
            response.put("customerName", booking.getCustomer() != null ? booking.getCustomer().getName() : "");
            response.put("checkInTime", checkInRecord.getCheckInTime());
            response.put("scheduledCheckOutDate", checkInRecord.getScheduledCheckOutDate());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 重置房间状态（数据一致性修复工具）
     * 将没有实际入住记录但状态为occupied的房间重置为available
     */
    @PostMapping("/reset-room-status/{hotelId}")
    public ResponseEntity<Map<String, Object>> resetRoomStatus(@PathVariable Long hotelId) {
        // 获取所有房间
        List<Room> allRooms = roomRepository.findByHotelId(hotelId);
        int resetCount = 0;

        for (Room room : allRooms) {
            // 如果房间状态是occupied，检查是否有实际的入住记录
            if (room.getStatus() == Room.RoomStatus.occupied) {
                boolean hasActiveCheckIn = checkInRecordRepository
                        .findByRoomIdAndStatus(room.getId(), CheckInRecord.CheckInStatus.active)
                        .isPresent();

                // 如果没有活跃入住记录，重置为available
                if (!hasActiveCheckIn) {
                    room.setStatus(Room.RoomStatus.available);
                    roomRepository.save(room);
                    resetCount++;
                }
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "房间状态重置完成");
        response.put("resetCount", resetCount);

        return ResponseEntity.ok(response);
    }

    /**
     * 创建测试入住数据（仅用于测试）
     */
    @PostMapping("/create-test-data/{hotelId}")
    public ResponseEntity<Map<String, Object>> createTestData(@PathVariable Long hotelId) {
        Map<String, Object> response = new HashMap<>();
        int createdCount = 0;

        try {
            // 获取一些可用的房间
            List<Room> availableRooms = roomRepository.findByHotelId(hotelId).stream()
                    .filter(r -> r.getStatus() == Room.RoomStatus.available)
                    .limit(4)
                    .toList();

            if (availableRooms.isEmpty()) {
                response.put("success", false);
                response.put("message", "没有可用的房间");
                return ResponseEntity.ok(response);
            }

            // 获取一些confirmed状态的预订
            List<Booking> confirmedBookings = bookingRepository.findByHotelIdAndStatus(hotelId, Booking.BookingStatus.booked);
            if (confirmedBookings.isEmpty()) {
                response.put("success", false);
                response.put("message", "没有已确认的预订");
                return ResponseEntity.ok(response);
            }

            // 创建4个入住记录
            for (int i = 0; i < Math.min(4, availableRooms.size()) && i < confirmedBookings.size(); i++) {
                Room room = availableRooms.get(i);
                Booking booking = confirmedBookings.get(i);

                // 使用有效的客户ID (从2开始,因为customers表ID从2开始)
                Long validCustomerId = 2L + i;

                // 创建入住记录
                CheckInRecord checkInRecord = CheckInRecord.builder()
                        .hotelId(hotelId)
                        .bookingId(booking.getId())
                        .roomId(room.getId())
                        .customerId(validCustomerId)
                        .checkInTime(LocalDateTime.now().minusHours((i + 1) * 2)) // 不同的入住时间
                        .scheduledCheckOutDate(LocalDate.now().plusDays((i + 1) * 2))
                        .actualPrice(java.math.BigDecimal.valueOf(100 + i * 50))
                        .status(CheckInRecord.CheckInStatus.active)
                        .notes("测试入住数据 " + (i + 1))
                        .build();

                checkInRecordRepository.save(checkInRecord);

                // 更新预订状态
                booking.setStatus(Booking.BookingStatus.checked_in);
                booking.setAssignedRoomId(room.getId());
                bookingRepository.save(booking);

                // 更新房间状态
                room.setStatus(Room.RoomStatus.occupied);
                roomRepository.save(room);

                createdCount++;
            }

            response.put("success", true);
            response.put("message", "测试数据创建成功");
            response.put("createdCount", createdCount);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "创建失败: " + e.getMessage());
            e.printStackTrace();
        }

        return ResponseEntity.ok(response);
    }
}
