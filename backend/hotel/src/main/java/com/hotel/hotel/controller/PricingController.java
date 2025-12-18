package com.hotel.hotel.controller;

import com.hotel.hotel.entity.CompetitorPrice;
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
     * [POST] 手动触发动态调价计算
     * 接口路径: /api/v1/pricing/adjust
     */
    @PostMapping("/adjust")
    public ResponseEntity<Map<String, String>> triggerPriceAdjustment() {
        System.out.println(">>> 接收到手动调价请求，开始执行多因子动态定价模型...");

        // 调用 Service 核心方法（内部已包含 50% 偏离度检查逻辑）
        pricingService.calculateAndAdjustPricesForFutureWeek();

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "动态调价计算完成。偏离度过高的价格已转入人工审批，其余已自动同步。");
        return ResponseEntity.ok(response);
    }

    /**
     * [GET] 查询指定生效日期的“已生效”房间价格
     * 接口路径: /api/v1/pricing/current?date=2025-12-20
     */
    @GetMapping("/current")
    public ResponseEntity<List<PricingRecord>> getCurrentPrices(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date)
    {
        if (date == null) {
            return ResponseEntity.badRequest().build();
        }

        // 注意：Service 的 getPricesByEffectiveDate 内部应只返回 status='APPROVED' 的记录
        List<PricingRecord> records = pricingService.getPricesByEffectiveDate(date);

        if (records.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(records);
    }

    // --- 以下为新增接口，用于支持 F3 动态定价任务 ---

    /**
     * [POST] 模拟执行竞品数据抓取
     * 接口路径: /api/v1/pricing/competitors/collect
     */
    @PostMapping("/competitors/collect")
    public ResponseEntity<String> collectCompetitorPrices() {
        pricingService.mockCrawlCompetitorPrices();
        return ResponseEntity.ok("竞品数据模拟抓取成功，已更新市场行情库。");
    }

    /**
     * [GET] 查询当前采集到的所有竞品价格
     * 接口路径: /api/v1/pricing/competitors
     */
    @GetMapping("/competitors")
    public ResponseEntity<List<CompetitorPrice>> getCompetitorPrices() {
        return ResponseEntity.ok(pricingService.getAllCompetitorPrices());
    }

    /**
     * [GET] 获取所有待审核的价格记录（偏离度 > 50% 的记录）
     * 接口路径: /api/v1/pricing/review
     */
    @GetMapping("/review")
    public ResponseEntity<List<PricingRecord>> getPendingPrices() {
        // 调用 Service 中新增的查询待审核方法
        List<PricingRecord> pendingRecords = pricingService.getPendingPrices();
        return ResponseEntity.ok(pendingRecords);
    }

    /**
     * [POST] 审批并通过特定价格记录
     * 接口路径: /api/v1/pricing/approve?recordId=101
     */
    @PostMapping("/approve")
    public ResponseEntity<Map<String, String>> approvePrice(@RequestParam Long recordId) {
        pricingService.approvePrice(recordId);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "价格 ID: " + recordId + " 已被管理员批准并生效。");
        return ResponseEntity.ok(response);
    }
}