package com.hotel.hotel.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 超售建议响应 DTO
 */
@Data
@Builder
public class OverbookingRecommendationDTO {
    private Long hotelId;
    private Long roomTypeId;
    private LocalDate targetDate;
    private String stateKey;
    private Integer recommendedOverbook;
    private BigDecimal qValue;
    private BigDecimal confidence;
    private Integer visitCount;
    private String reason;

    // 新增字段
    private BigDecimal currentOccupancy;
    private BigDecimal expectedNoShow;
    private BigDecimal expectedRevenue;
    private String riskLevel;
    private StateInfo state;

    @Data
    @Builder
    public static class StateInfo {
        private String occupancyLevel;
        private String dayType;
        private String leadTimeCategory;
    }
}
