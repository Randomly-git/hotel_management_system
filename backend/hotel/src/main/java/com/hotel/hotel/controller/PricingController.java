package com.hotel.hotel.controller;

import com.hotel.hotel.entity.PricingRecord;
import com.hotel.hotel.service.PricingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pricing")
public class PricingController {

    private final PricingService pricingService;

    @Autowired
    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    /**
     * [POST] 手动触发动态调价计算和全渠道同步
     * 接口路径: /api/v1/pricing/adjust
     * * 实际应用中会由定时任务触发，此接口用于测试和紧急干预。
     */
    @PostMapping("/adjust")
    public ResponseEntity<Map<String, String>> triggerPriceAdjustment() {

        System.out.println(">>> 接收到手动调价请求，开始执行动态定价模型...");

        // 调用 Service 核心方法，计算未来 7 天价格
        pricingService.calculateAndAdjustPricesForFutureWeek();

        // Service 中已模拟价格同步，这里仅返回成功信息
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "动态调价计算完成，并已触发全渠道价格同步。请查询历史接口验证结果。");

        return ResponseEntity.ok(response);
    }

    /**
     * [GET] 查询指定生效日期的房间价格
     * 接口路径: /api/v1/pricing/current?date=2025-12-20
     * * 供前端和酒店预订系统查询价格。
     *
     * @param date 价格生效日期
     * @return 价格记录列表
     */
    @GetMapping("/current")
    public ResponseEntity<List<PricingRecord>> getCurrentPrices(
            // 使用 @RequestParam 接收日期参数，并使用 @DateTimeFormat 确保格式正确解析
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date)
    {
        if (date == null) {
            return ResponseEntity.badRequest().build();
        }

        // 调用 Service 查询指定日期的价格
        List<PricingRecord> records = pricingService.getPricesByEffectiveDate(date);

        if (records.isEmpty()) {
            // 如果查询不到价格，可以返回基础价格或 204
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(records);
    }

    // 实际项目中还应有：查询历史调价记录（用于审计）、查询某个房型的价格趋势等接口
}