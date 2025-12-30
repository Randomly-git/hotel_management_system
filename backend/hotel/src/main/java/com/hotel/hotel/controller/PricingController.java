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
     * [POST] 生成调价建议（店长发起或系统自动）
     * 逻辑改动：生成的记录状态为 PENDING，不会立即改变客房售价。
     */
    @PostMapping("/adjust")
    public ResponseEntity<Map<String, String>> triggerPriceAdjustment() {
        System.out.println(">>> 接收到计算请求，正在为所有房型生成 PENDING 调价建议...");

        pricingService.calculateAndAdjustPricesForFutureWeek();

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "调价建议生成成功，请在'待审批'列表查看。");

        return ResponseEntity.ok(response);
    }

    /**
     * [GET] 店长工作台：获取所有待审批（PENDING）的调价记录
     * 对应改进点：店长交互 - 审核环节
     */
    @GetMapping("/pending")
    public ResponseEntity<List<PricingRecord>> getPendingProposals() {
        // 调用 Service 获取状态为 PENDING 的记录
        // 注意：需确保 PricingService 中有对应的查询逻辑
        List<PricingRecord> pendingRecords = pricingService.getRecordsByStatus("PENDING");
        return ResponseEntity.ok(pendingRecords);
    }

    /**
     * [POST] 店长审批：批准特定的价格记录
     * 逻辑：将状态从 PENDING 改为 APPLIED，并触发渠道同步。
     *
     * @param recordId 记录ID
     */
    @PostMapping("/approve/{recordId}")
    public ResponseEntity<Map<String, Object>> approvePrice(@PathVariable Long recordId) {
        boolean success = pricingService.applyPriceRecord(recordId);

        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("status", "success");
            response.put("message", "价格已批准并正式生效。");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "未找到该调价记录或记录已过期。");
            return ResponseEntity.status(404).body(response);
        }
    }

    /**
     * [GET] 查询【已生效】的价格
     * 逻辑改动：只返回状态为 APPLIED 的记录，确保前台看到的不是"草稿价"。
     *
     * @param date 价格生效日期
     * @return 价格记录列表
     */
    @GetMapping("/current")
    public ResponseEntity<List<PricingRecord>> getCurrentPrices(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        if (date == null) {
            return ResponseEntity.badRequest().build();
        }

        // 修改 Service 逻辑，确保内部调用了 status = 'APPLIED' 的过滤
        List<PricingRecord> records = pricingService.getAppliedPricesByDate(date);

        if (records.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(records);
    }
}