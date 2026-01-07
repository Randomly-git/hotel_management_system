package com.hotel.hotel.dto;

import com.hotel.hotel.entity.Room;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 房间响应 DTO
 */
@Data
public class RoomResponse {
    private Long id;
    private Long hotelId;
    private Long roomTypeId;
    private String roomNumber;
    private Integer floor;
    private Room.RoomStatus status;
    private Boolean hasAc;
    private Boolean hasTv;
    private Boolean hasWifi;
    private Boolean hasBalcony;
    private Boolean hasKitchen;
    private Integer parkingSpaces;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 扩展信息（来自关联表）
    private String typeName;
    private String typeCode;
    private BigDecimal basePrice;
    private List<String> facilities;

    // 入住信息（当房间状态为occupied时）
    private String guestName;
    private String guestPhone;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private String bookingNumber;

    public static RoomResponse fromEntity(Room room) {
        RoomResponse response = new RoomResponse();
        response.setId(room.getId());
        response.setHotelId(room.getHotelId());
        response.setRoomTypeId(room.getRoomTypeId());
        response.setRoomNumber(room.getRoomNumber());
        response.setFloor(room.getFloor());
        response.setStatus(room.getStatus());
        response.setHasAc(room.getHasAc());
        response.setHasTv(room.getHasTv());
        response.setHasWifi(room.getHasWifi());
        response.setHasBalcony(room.getHasBalcony());
        response.setHasKitchen(room.getHasKitchen());
        response.setParkingSpaces(room.getParkingSpaces());
        response.setDescription(room.getDescription());
        response.setCreatedAt(room.getCreatedAt());
        response.setUpdatedAt(room.getUpdatedAt());

        // 如果有关联的房型信息
        if (room.getRoomType() != null) {
            response.setTypeName(room.getRoomType().getTypeName());
            response.setTypeCode(room.getRoomType().getTypeCode());
            response.setBasePrice(room.getRoomType().getBasePrice());
            response.setFacilities(room.getRoomType().getFacilities());
        }

        return response;
    }
}
