package com.hotel.hotel.dto;

import com.hotel.hotel.entity.Booking;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预订响应 DTO
 */
@Data
public class BookingResponse {
    private Long id;
    private Long hotelId;
    private Long customerId;
    private Long roomTypeId;
    private String bookingNumber;
    private Integer leadTime;
    private LocalDateTime bookingDate;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private LocalDate actualCheckOutDate;
    private Integer totalNights;
    private Integer adults;
    private Integer children;
    private Integer babies;
    private BigDecimal totalPrice;
    private BigDecimal adr;
    private Booking.DepositType depositType;
    private Booking.MealType mealType;
    private String marketSegment;
    private String distributionChannel;
    private Booking.BookingStatus status;
    private Boolean isCanceled;
    private Integer specialRequests;
    private String requestsText;
    private Long assignedRoomId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 扩展信息
    private String customerName;
    private String customerPhone;
    private String typeName;
    private String typeCode;
    private String roomNumber;

    public static BookingResponse fromEntity(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setHotelId(booking.getHotelId());
        response.setCustomerId(booking.getCustomerId());
        response.setRoomTypeId(booking.getRoomTypeId());
        response.setBookingNumber(booking.getBookingNumber());
        response.setLeadTime(booking.getLeadTime());
        response.setBookingDate(booking.getBookingDate());
        response.setCheckInDate(booking.getCheckInDate());
        response.setCheckOutDate(booking.getCheckOutDate());
        response.setActualCheckOutDate(booking.getActualCheckOutDate());
        response.setTotalNights(booking.getTotalNights());
        response.setAdults(booking.getAdults());
        response.setChildren(booking.getChildren());
        response.setBabies(booking.getBabies());
        response.setTotalPrice(booking.getTotalPrice());
        response.setAdr(booking.getAdr());
        response.setDepositType(booking.getDepositType());
        response.setMealType(booking.getMealType());
        response.setMarketSegment(booking.getMarketSegment());
        response.setDistributionChannel(booking.getDistributionChannel());
        response.setStatus(booking.getStatus());
        response.setIsCanceled(booking.getIsCanceled());
        response.setSpecialRequests(booking.getSpecialRequests());
        response.setRequestsText(booking.getRequestsText());
        response.setAssignedRoomId(booking.getAssignedRoomId());
        response.setCreatedAt(booking.getCreatedAt());
        response.setUpdatedAt(booking.getUpdatedAt());

        // 客户信息
        if (booking.getCustomer() != null) {
            response.setCustomerName(booking.getCustomer().getName());
            response.setCustomerPhone(booking.getCustomer().getPhone());
        }

        // 房型信息
        if (booking.getRoomType() != null) {
            response.setTypeName(booking.getRoomType().getTypeName());
            response.setTypeCode(booking.getRoomType().getTypeCode());
        }

        // 房间信息
        if (booking.getAssignedRoom() != null) {
            response.setRoomNumber(booking.getAssignedRoom().getRoomNumber());
        }

        return response;
    }
}
