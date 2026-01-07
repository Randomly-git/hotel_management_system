package com.hotel.hotel.dto;

import com.hotel.hotel.entity.Booking;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * 创建预订请求 DTO
 */
@Data
public class BookingRequest {

    @NotNull(message = "酒店ID不能为空")
    private Long hotelId;

    // 用户相关字段 - 支持创建新用户或使用现有用户
    private Long customerId;  // 现有用户ID

    // 新用户创建字段
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String customerCountry;

    @NotNull(message = "房型ID不能为空")
    private Long roomTypeId;

    @NotNull(message = "入住日期不能为空")
    @Future(message = "入住日期必须是未来日期")
    private LocalDate checkInDate;

    @NotNull(message = "退房日期不能为空")
    private LocalDate checkOutDate;

    @NotNull(message = "成人数量不能为空")
    @Min(value = 1, message = "至少1位成人")
    @Max(value = 10, message = "成人数量不能超过10")
    private Integer adults = 1;

    @Min(value = 0, message = "儿童数量不能为负")
    @Max(value = 10, message = "儿童数量不能超过10")
    private Integer children = 0;

    @Min(value = 0, message = "婴儿数量不能为负")
    @Max(value = 5, message = "婴儿数量不能超过5")
    private Integer babies = 0;

    @Min(value = 0, message = "停车位数量不能为负")
    private Integer requiredCarParkingSpaces = 0;

    private Booking.MealType mealType = Booking.MealType.bb;

    private String marketSegment;

    private String distributionChannel;

    private Booking.DepositType depositType = Booking.DepositType.no_deposit;

    private String requestsText;
}