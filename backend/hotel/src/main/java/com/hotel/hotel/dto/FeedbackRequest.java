package com.hotel.hotel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 接收客户反馈信息的请求DTO
 */
@Data
public class FeedbackRequest {

    // 确保昵称不为空
    @NotBlank(message = "客户昵称不能为空")
    private String customerName;

    // 确保反馈内容不为空，且至少有一定的长度（例如 5 个字符）
    @NotBlank(message = "反馈内容不能为空")
    @Size(min = 5, message = "反馈内容不能少于5个字符")
    private String feedbackContent;
}