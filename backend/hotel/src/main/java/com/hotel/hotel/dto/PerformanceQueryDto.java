package com.hotel.hotel.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

/**
 * 绩效查询的请求参数DTO
 */
@Data
public class PerformanceQueryDto {

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    // 部门ID可选，如果为空则查询所有部门
    private Long deptId;
}