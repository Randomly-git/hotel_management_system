package com.hotel.hotel.controller;

import com.hotel.hotel.dto.RoomResponse;
import com.hotel.hotel.entity.Room;
import com.hotel.hotel.repository.HotelRoomTypeRepository;
import com.hotel.hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 房间管理 API
 */
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RoomController {

    private final RoomRepository roomRepository;
    private final HotelRoomTypeRepository roomTypeRepository;

    /**
     * 获取酒店所有房间
     */
    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<RoomResponse>> getRoomsByHotel(
            @PathVariable Long hotelId,
            @RequestParam(required = false) Room.RoomStatus status) {

        List<Room> rooms;
        if (status != null) {
            rooms = roomRepository.findByHotelIdAndStatus(hotelId, status);
        } else {
            rooms = roomRepository.findByHotelId(hotelId);
        }

        // 加载关联的房型信息
        rooms.forEach(room -> {
            roomTypeRepository.findById(room.getRoomTypeId()).ifPresent(room::setRoomType);
        });

        List<RoomResponse> responses = rooms.stream()
                .map(RoomResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 根据房型获取可用房间
     */
    @GetMapping("/available")
    public ResponseEntity<List<RoomResponse>> getAvailableRooms(
            @RequestParam Long roomTypeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate) {

        List<Room> rooms = roomRepository.findAvailableRoomsByType(roomTypeId);

        List<RoomResponse> responses = rooms.stream()
                .map(RoomResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 获取房间详情
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable Long roomId) {
        return roomRepository.findById(roomId)
                .map(room -> {
                    roomTypeRepository.findById(room.getRoomTypeId()).ifPresent(room::setRoomType);
                    return ResponseEntity.ok(RoomResponse.fromEntity(room));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 更新房间状态
     */
    @PatchMapping("/{roomId}/status")
    public ResponseEntity<RoomResponse> updateRoomStatus(
            @PathVariable Long roomId,
            @RequestBody Map<String, String> request) {

        return roomRepository.findById(roomId)
                .map(room -> {
                    String statusStr = request.get("status");
                    room.setStatus(Room.RoomStatus.valueOf(statusStr.toLowerCase()));
                    Room savedRoom = roomRepository.save(room);

                    roomTypeRepository.findById(savedRoom.getRoomTypeId())
                            .ifPresent(savedRoom::setRoomType);

                    return ResponseEntity.ok(RoomResponse.fromEntity(savedRoom));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 获取房间统计信息
     */
    @GetMapping("/hotel/{hotelId}/statistics")
    public ResponseEntity<Map<String, Object>> getRoomStatistics(@PathVariable Long hotelId) {
        List<Room> allRooms = roomRepository.findByHotelId(hotelId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", allRooms.size());

        Map<String, Long> statusCount = allRooms.stream()
                .collect(Collectors.groupingBy(
                        room -> room.getStatus().name(),
                        Collectors.counting()
                ));

        stats.put("available", statusCount.getOrDefault("available", 0L));
        stats.put("occupied", statusCount.getOrDefault("occupied", 0L));
        stats.put("maintenance", statusCount.getOrDefault("maintenance", 0L));
        stats.put("cleaning", statusCount.getOrDefault("cleaning", 0L));

        return ResponseEntity.ok(stats);
    }

    /**
     * 根据房型获取房间列表
     */
    @GetMapping("/hotel/{hotelId}/type/{roomTypeId}")
    public ResponseEntity<List<RoomResponse>> getRoomsByType(
            @PathVariable Long hotelId,
            @PathVariable Long roomTypeId) {

        List<Room> rooms = roomRepository.findByHotelIdAndRoomTypeId(hotelId, roomTypeId);

        rooms.forEach(room -> {
            roomTypeRepository.findById(room.getRoomTypeId()).ifPresent(room::setRoomType);
        });

        List<RoomResponse> responses = rooms.stream()
                .map(RoomResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }
}
