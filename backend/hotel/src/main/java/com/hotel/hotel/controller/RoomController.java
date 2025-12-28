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

    /**
     * 创建新房间
     */
    @PostMapping("/hotel/{hotelId}")
    public ResponseEntity<RoomResponse> createRoom(
            @PathVariable Long hotelId,
            @RequestBody CreateRoomRequest request) {

        // 验证房型是否存在
        roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new IllegalArgumentException("房型不存在"));

        // 检查房间号是否已存在
        if (roomRepository.existsByHotelIdAndRoomNumber(hotelId, request.getRoomNumber())) {
            throw new IllegalArgumentException("房间号已存在");
        }

        // 创建房间
        Room room = Room.builder()
                .hotelId(hotelId)
                .roomTypeId(request.getRoomTypeId())
                .roomNumber(request.getRoomNumber())
                .floor(request.getFloor())
                .status(Room.RoomStatus.valueOf(request.getStatus().toLowerCase()))
                .hasAc(request.getHasAc() != null ? request.getHasAc() : true)
                .hasTv(request.getHasTv() != null ? request.getHasTv() : true)
                .hasWifi(request.getHasWifi() != null ? request.getHasWifi() : true)
                .hasBalcony(request.getHasBalcony() != null ? request.getHasBalcony() : false)
                .hasKitchen(request.getHasKitchen() != null ? request.getHasKitchen() : false)
                .parkingSpaces(request.getParkingSpaces() != null ? request.getParkingSpaces() : 0)
                .description(request.getDescription())
                .build();

        Room savedRoom = roomRepository.save(room);

        // 加载房型信息
        roomTypeRepository.findById(savedRoom.getRoomTypeId())
                .ifPresent(savedRoom::setRoomType);

        return ResponseEntity.ok(RoomResponse.fromEntity(savedRoom));
    }

    /**
     * 获取酒店所有房型
     */
    @GetMapping("/room-types/hotel/{hotelId}")
    public ResponseEntity<List<Map<String, Object>>> getRoomTypesByHotel(
            @PathVariable Long hotelId) {

        List<com.hotel.hotel.entity.HotelRoomType> roomTypes = roomTypeRepository.findByHotelId(hotelId);

        // 转换为简单的Map，避免Jackson序列化时触发LAZY加载
        List<Map<String, Object>> result = roomTypes.stream()
                .map(rt -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", rt.getId());
                    map.put("hotelId", rt.getHotelId());
                    map.put("typeCode", rt.getTypeCode());
                    map.put("typeName", rt.getTypeName());
                    map.put("description", rt.getDescription());
                    map.put("maxOccupancy", rt.getMaxOccupancy());
                    map.put("basePrice", rt.getBasePrice());
                    map.put("facilities", rt.getFacilities());
                    map.put("createdAt", rt.getCreatedAt());
                    map.put("updatedAt", rt.getUpdatedAt());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // DTOs
    @lombok.Data
    public static class CreateRoomRequest {
        private Long roomTypeId;
        private String roomNumber;
        private Integer floor;
        private String status = "available";
        private Boolean hasAc;
        private Boolean hasTv;
        private Boolean hasWifi;
        private Boolean hasBalcony;
        private Boolean hasKitchen;
        private Integer parkingSpaces;
        private String description;
    }
}
