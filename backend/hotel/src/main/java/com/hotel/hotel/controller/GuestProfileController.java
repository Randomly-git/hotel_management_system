package com.hotel.hotel.controller;

import com.hotel.hotel.common.Response;
import com.hotel.hotel.entity.GuestProfile;
import com.hotel.hotel.service.GuestProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 客户画像管理控制器
 * 负责管理客户画像数据和偏好标签
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/guest-profile")
@Tag(name = "客户画像管理", description = "管理客户画像、偏好标签和历史数据")
public class GuestProfileController {

    private final GuestProfileService guestProfileService;

    @Autowired
    public GuestProfileController(GuestProfileService guestProfileService) {
        this.guestProfileService = guestProfileService;
    }

    /**
     * DTO用于更新客户画像
     */
    @Data
    public static class UpdateProfileRequest {
        @Parameter(description = "偏好标签列表")
        private List<String> preferences;

        @Parameter(description = "客户标签列表")
        private List<String> tags;

        @Parameter(description = "最近入住房间号")
        private String lastStayRoom;

        @Parameter(description = "备注信息")
        private String remarks;
    }

    /**
     * [GET] 获取客户画像
     */
    @Operation(summary = "查询客户画像", description = "根据会员ID查询客户详细画像")
    @GetMapping("/{memberId}")
    public ResponseEntity<Response<GuestProfile>> getGuestProfile(
            @Parameter(description = "会员ID", required = true)
            @PathVariable String memberId) {
        try {
            GuestProfile profile = guestProfileService.getGuestProfileByMemberId(memberId);
            if (profile == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(Response.success(profile));
        } catch (Exception e) {
            log.error("查询客户画像失败: memberId={}", memberId, e);
            return ResponseEntity.internalServerError()
                .body(Response.error("查询失败: " + e.getMessage()));
        }
    }

    /**
     * [PUT] 更新客户画像
     */
    @Operation(summary = "更新客户画像", description = "更新客户的偏好标签和信息")
    @PutMapping("/{memberId}")
    public ResponseEntity<Response<GuestProfile>> updateGuestProfile(
            @Parameter(description = "会员ID", required = true)
            @PathVariable String memberId,
            @Valid @RequestBody UpdateProfileRequest request) {
        try {
            GuestProfile updatedProfile = guestProfileService.updateGuestProfile(
                memberId, request.getPreferences(), request.getTags(),
                request.getLastStayRoom(), request.getRemarks()
            );

            return ResponseEntity.ok(Response.success("更新成功", updatedProfile));
        } catch (Exception e) {
            log.error("更新客户画像失败: memberId={}", memberId, e);
            return ResponseEntity.internalServerError()
                .body(Response.error("更新失败: " + e.getMessage()));
        }
    }

    /**
     * [POST] 添加客户偏好
     */
    @Operation(summary = "添加客户偏好", description = "为客户添加新的偏好标签")
    @PostMapping("/{memberId}/preferences")
    public ResponseEntity<Response<GuestProfile>> addPreference(
            @Parameter(description = "会员ID", required = true)
            @PathVariable String memberId,
            @Parameter(description = "偏好标签", required = true)
            @RequestParam String preference) {
        try {
            GuestProfile profile = guestProfileService.addPreference(memberId, preference);
            return ResponseEntity.ok(Response.success("偏好添加成功", profile));
        } catch (Exception e) {
            log.error("添加客户偏好失败: memberId={}, preference={}", memberId, preference, e);
            return ResponseEntity.internalServerError()
                .body(Response.error("添加失败: " + e.getMessage()));
        }
    }

    /**
     * [GET] 获取客户历史偏好
     */
    @Operation(summary = "查询历史偏好", description = "获取客户的历史偏好和消费记录")
    @GetMapping("/{memberId}/history")
    public ResponseEntity<Response<Map<String, Object>>> getGuestHistory(
            @Parameter(description = "会员ID", required = true)
            @PathVariable String memberId) {
        try {
            Map<String, Object> history = guestProfileService.getGuestHistory(memberId);
            return ResponseEntity.ok(Response.success(history));
        } catch (Exception e) {
            log.error("查询客户历史失败: memberId={}", memberId, e);
            return ResponseEntity.internalServerError()
                .body(Response.error("查询失败: " + e.getMessage()));
        }
    }

    /**
     * [GET] 搜索客户画像
     */
    @Operation(summary = "搜索客户", description = "根据条件搜索客户画像")
    @GetMapping("/search")
    public ResponseEntity<Response<List<GuestProfile>>> searchGuests(
            @Parameter(description = "搜索关键词")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "偏好标签")
            @RequestParam(required = false) String preference) {
        try {
            List<GuestProfile> results = guestProfileService.searchGuestProfiles(keyword, preference);
            return ResponseEntity.ok(Response.success(results));
        } catch (Exception e) {
            log.error("搜索客户失败: keyword={}, preference={}", keyword, preference, e);
            return ResponseEntity.internalServerError()
                .body(Response.error("搜索失败: " + e.getMessage()));
        }
    }
}