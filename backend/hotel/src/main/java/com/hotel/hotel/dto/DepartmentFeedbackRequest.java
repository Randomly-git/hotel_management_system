package com.hotel.hotel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 部门客户评价请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "部门客户评价请求")
public class DepartmentFeedbackRequest {

    @NotNull(message = "任务单ID不能为空")
    @Schema(description = "任务单ID", example = "1", required = true)
    private Long taskOrderId;

    @NotNull(message = "部门任务ID不能为空")
    @Schema(description = "部门任务ID", example = "1", required = true)
    private Long departmentTaskId;

    @NotNull(message = "客户ID不能为空")
    @Schema(description = "客户ID", example = "VIP001", required = true)
    private String customerId;

    @NotNull(message = "酒店ID不能为空")
    @Schema(description = "酒店ID", example = "1", required = true)
    private Long hotelId;

    @NotNull(message = "部门ID不能为空")
    @Schema(description = "部门ID", example = "1", required = true)
    private Long departmentId;

    @NotNull(message = "部门名称不能为空")
    @Schema(description = "部门名称", example = "工程部", required = true)
    private String departmentName;

    @NotNull(message = "服务评分不能为空")
    @Min(value = 1, message = "服务评分最小为1分")
    @Max(value = 5, message = "服务评分最大为5分")
    @Schema(description = "服务评分(1-5分)", example = "5", required = true)
    private Integer serviceRating;

    @NotNull(message = "响应速度评分不能为空")
    @Min(value = 1, message = "响应速度评分最小为1分")
    @Max(value = 5, message = "响应速度评分最大为5分")
    @Schema(description = "响应速度评分(1-5分)", example = "4", required = true)
    private Integer responseSpeedRating;

    @NotNull(message = "服务质量评分不能为空")
    @Min(value = 1, message = "服务质量评分最小为1分")
    @Max(value = 5, message = "服务质量评分最大为5分")
    @Schema(description = "服务质量评分(1-5分)", example = "5", required = true)
    private Integer serviceQualityRating;

    @NotNull(message = "反馈内容不能为空")
    @Schema(description = "反馈内容", example = "服务非常专业，响应很快", required = true)
    private String feedbackContent;

    @Schema(description = "评价标签(逗号分隔)", example = "专业,快速,推荐")
    private String feedbackTags;

    @Schema(description = "是否推荐", example = "true")
    private Boolean isRecommended;
}
