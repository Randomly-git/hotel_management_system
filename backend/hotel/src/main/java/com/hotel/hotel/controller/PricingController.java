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
     * [POST] 生成调价建议
     * 保持接口不变：/api/v1/pricing/adjust
     */
    @PostMapping("/adjust")
    public ResponseEntity<Map<String, String>> triggerPriceAdjustment() {
        // 内部逻辑已适配 HotelRoomType 和 2023-2025 数据平移
        pricingService.calculateAndAdjustPricesForFutureWeek();

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "调价建议生成成功，已关联 A-H 房型，请在'待审批'列表查看。");

        return ResponseEntity.ok(response);
    }

    /**
     * [GET] 获取所有待审批记录
     * 保持接口不变：/api/v1/pricing/pending
     */
    @GetMapping("/pending")
    public ResponseEntity<List<PricingRecord>> getPendingProposals() {
        // 修正：确保 PricingRecord 现在包含的是 HotelRoomType 信息
        List<PricingRecord> pendingRecords = pricingService.getRecordsByStatus("PENDING");
        return ResponseEntity.ok(pendingRecords);
    }

    /**
     * [POST] 审批价格
     * 保持接口不变：/api/v1/pricing/approve/{recordId}
     */
    @PostMapping("/approve/{recordId}")
    public ResponseEntity<Map<String, Object>> approvePrice(@PathVariable Long recordId) {
        boolean success = pricingService.applyPriceRecord(recordId);

        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("status", "success");
            response.put("message", "价格已批准。房型 A-H 的动态定价已生效。");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "记录不存在。");
            return ResponseEntity.status(404).body(response);
        }
    }

    /**
     * [GET] 查询已生效价格
     * 保持接口不变：/api/v1/pricing/current?date=yyyy-MM-dd
     */
    @GetMapping("/current")
    public ResponseEntity<List<PricingRecord>> getCurrentPrices(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        // 内部降级逻辑已适配：若无 APPLIED 则返回 HotelRoomType 的基准价
        List<PricingRecord> records = pricingService.getAppliedPricesByDate(date);
        return ResponseEntity.ok(records);
    }
}