package com.hotel.hotel.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 封装NLP服务返回的情感得分和归因部门名称
 */
@Data
public class NlpResult {

    /** 情感分析得分，范围 [-1.0, 1.0] */
    private BigDecimal sentimentScore;

    /** 文本归因到的部门名称 (如: "房务部", "服务部") */
    private String attributedDeptName;

    /** 归因分析是否成功 */
    private boolean success = true;
}