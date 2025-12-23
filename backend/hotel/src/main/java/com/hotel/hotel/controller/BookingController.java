package com.hotel.hotel.controller;

import com.hotel.hotel.entity.Booking;
import com.hotel.hotel.entity.Room;
import com.hotel.hotel.service.BookingService;
import com.hotel.hotel.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/booking")
public class BookingController {

    private final BookingService bookingService;
    private final RoomService roomService;

    @Autowired
    public BookingController(BookingService bookingService, RoomService roomService) {
        this.bookingService = bookingService;
        this.roomService = roomService;
    }

    /**
     * [GET] 查询房态日历
     * 用于前端日历展示，显示哪些日期已被占用
     * 接口路径: /api/v1/booking/room-calendar/{roomId}
     */
    @GetMapping("/room-calendar/{roomId}")
    public ResponseEntity<Room> getRoomCalendar(@PathVariable Integer roomId) {
        Room room = roomService.findById(roomId);
        if (room == null) {
            return ResponseEntity.notFound().build();
        }
        // 返回的 Room 对象中包含 notUseDateList 和 canUse
        return ResponseEntity.ok(room);
    }

    /**
     * [POST] 执行智能下单 (支持每日价格明细展示)
     * 接口路径: /api/v1/booking/reserve
     */
    @PostMapping("/reserve")
    public ResponseEntity<Map<String, Object>> reserveRoom(
            @RequestParam Integer roomId,
            @RequestParam(required = false) Integer memberId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date checkInDate,
            @RequestParam Integer days)
    {
        Map<String, Object> response = new HashMap<>();
        try {
            // 1. 调用 Service 执行核心预订逻辑
            Booking booking = bookingService.bookRoom(roomId, memberId, checkInDate, days);

            // 2. 构造更加丰富的返回数据
            response.put("status", "success");
            response.put("message", "预订成功！");

            // 订单基本信息
            Map<String, Object> orderInfo = new HashMap<>();
            orderInfo.put("orderId", booking.getId());
            orderInfo.put("checkInDate", checkInDate);
            orderInfo.put("stayDays", days);
            orderInfo.put("totalAmount", booking.getMoney());

            // 价格明细：直接从刚才 Service 存入的备注中提取，展示给前端看
            // 备注格式示例："智能每日调价下单。明细：[2025-12-23:283.50] ..."
            orderInfo.put("priceBreakdown", booking.getRemark());

            response.put("data", orderInfo);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // 业务异常拦截（如房间冲突、价格未审批等）
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}