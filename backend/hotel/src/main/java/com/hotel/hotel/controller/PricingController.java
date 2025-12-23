package com.hotel.hotel.controller;

import com.hotel.hotel.entity.CompetitorPrice;
import com.hotel.hotel.entity.PricingRecord;
import com.hotel.hotel.service.PricingService;
import com.hotel.hotel.service.RoomService; // 1. 必须导入 RoomService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal; // 2. 导入 BigDecimal 避免写长串路径
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pricing")
public class PricingController {

    private final PricingService pricingService;
    private final RoomService roomService; // 3. 声明 roomService 字段

    @Autowired
    // 4. 在构造函数中同时注入两个 Service
    public PricingController(PricingService pricingService, RoomService roomService) {
        this.pricingService = pricingService;
        this.roomService = roomService;
    }

    /**
     * [POST] 手动触发动态调价计算 (未来30天)
     */
    @PostMapping("/adjust")
    public ResponseEntity<Map<String, String>> triggerPriceAdjustment() {
        System.out.println(">>> 接收到手动调价请求，开始执行全自动全量模型...");
        pricingService.mockCrawlCompetitorPrices();
        pricingService.calculateAndAdjustPricesForFutureWeek();

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "未来30天竞品抓取 + 动态调价已一键完成！");
        return ResponseEntity.ok(response);
    }

    /**
     * [GET] 查询指定生效日期的“已生效”房间价格
     */
    @GetMapping("/current")
    public ResponseEntity<List<PricingRecord>> getCurrentPrices(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) {
            return ResponseEntity.badRequest().build();
        }
        List<PricingRecord> records = pricingService.getPricesByEffectiveDate(date);
        if (records.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(records);
    }

    /**
     * [PUT] 修改房型基础底价 (店长最高权限)
     * 接口路径: /api/v1/pricing/base-price
     */
    @PutMapping("/base-price")
    public ResponseEntity<Map<String, Object>> updateBasePrice(
            @RequestParam Integer typeId,
            @RequestParam BigDecimal newBasePrice) {

        // 调用 roomService 修改房型底价
        roomService.updateBasePrice(typeId, newBasePrice);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "房型底价已成功修改为: " + newBasePrice);
        response.put("nextStep", "由于底价变动，建议立即执行 /adjust 接口以刷新近期动态价格。");

        return ResponseEntity.ok(response);
    }

    /**
     * [PUT] 店长暴力改价 (针对特定日期记录)
     */
    @PutMapping("/manual-update")
    public ResponseEntity<Map<String, String>> manualUpdatePrice(
            @RequestParam Long recordId,
            @RequestParam BigDecimal newPrice,
            @RequestParam(required = false, defaultValue = "店长特殊调整") String reason) {

        pricingService.manualUpdatePrice(recordId, newPrice, reason);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "手动改价成功！该记录已强制生效。");
        return ResponseEntity.ok(response);
    }

    /**
     * [GET] 获取所有待审核价格
     */
    @GetMapping("/review")
    public ResponseEntity<List<PricingRecord>> getPendingPrices() {
        return ResponseEntity.ok(pricingService.getPendingPrices());
    }

    /**
     * [POST] 审批通过价格
     */
    @PostMapping("/approve")
    public ResponseEntity<Map<String, String>> approvePrice(@RequestParam Long recordId) {
        pricingService.approvePrice(recordId);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "价格 ID: " + recordId + " 已批准生效。");
        return ResponseEntity.ok(response);
    }

    /**
     * [GET] 查询竞品价格
     */
    @GetMapping("/competitors")
    public ResponseEntity<List<CompetitorPrice>> getCompetitorPrices() {
        return ResponseEntity.ok(pricingService.getAllCompetitorPrices());
    }

    /**
     * [POST] 模拟竞品采集
     */
    @PostMapping("/competitors/collect")
    public ResponseEntity<String> collectCompetitorPrices() {
        pricingService.mockCrawlCompetitorPrices();
        return ResponseEntity.ok("竞品数据模拟抓取成功。");
    }
}