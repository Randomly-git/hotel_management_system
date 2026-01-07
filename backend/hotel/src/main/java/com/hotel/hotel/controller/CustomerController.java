package com.hotel.hotel.controller;

import com.hotel.hotel.entity.Customer;
import com.hotel.hotel.entity.Booking;
import com.hotel.hotel.repository.CustomerRepository;
import com.hotel.hotel.repository.BookingRepository;
import com.hotel.hotel.service.CustomerProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 客户管理 API
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
@Tag(name = "客户管理", description = "客户信息管理相关接口")
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final BookingRepository bookingRepository;
    private final CustomerProfileService customerProfileService;

    /**
     * 客户请求DTO
     */
    @Data
    public static class CustomerRequest {
        @Parameter(description = "酒店ID")
        private Long hotelId;

        @Parameter(description = "客户姓名")
        private String name;

        @Parameter(description = "邮箱")
        private String email;

        @Parameter(description = "电话")
        private String phone;

        @Parameter(description = "国家代码")
        private String country;

        @Parameter(description = "身份证号")
        private String idCardNumber;

        @Parameter(description = "VIP等级")
        private Customer.VipLevel vipLevel;

        @Parameter(description = "备注")
        private String notes;
    }

    /**
     * 获取酒店所有客户（支持分页）
     */
    @GetMapping("/hotel/{hotelId}")
    @Operation(summary = "获取酒店客户列表", description = "支持分页、筛选")
    public ResponseEntity<Map<String, Object>> getCustomersByHotel(
            @PathVariable Long hotelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String vipLevel, // 支持逗号分隔的多个VIP等级
            @RequestParam(required = false) Boolean isRepeatedGuest) {

        List<Customer> customers = customerRepository.findByHotelId(hotelId);

        // 应用筛选条件
        if (name != null && !name.isEmpty()) {
            customers = customers.stream()
                    .filter(c -> c.getName() != null && c.getName().contains(name))
                    .collect(Collectors.toList());
        }
        if (phone != null && !phone.isEmpty()) {
            customers = customers.stream()
                    .filter(c -> c.getPhone() != null && c.getPhone().contains(phone))
                    .collect(Collectors.toList());
        }
        if (vipLevel != null && !vipLevel.isEmpty()) {
            // 支持单个VIP等级筛选
            try {
                Customer.VipLevel targetLevel = Customer.VipLevel.valueOf(vipLevel.trim());
                customers = customers.stream()
                        .filter(c -> c.getVipLevel() != null && c.getVipLevel().equals(targetLevel))
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                // 忽略无效的VIP等级
            }
        }
        if (isRepeatedGuest != null) {
            customers = customers.stream()
                    .filter(c -> Boolean.TRUE.equals(c.getIsRepeatedGuest()) == isRepeatedGuest)
                    .collect(Collectors.toList());
        }


        // 手动分页
        int totalElements = customers.size();
        int totalPages = totalElements > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        int startIndex = Math.min(page * size, totalElements);
        int endIndex = Math.min(startIndex + size, totalElements);

        List<Customer> pageContent = startIndex < endIndex ? customers.subList(startIndex, endIndex) : new ArrayList<>();

        Map<String, Object> response = new HashMap<>();
        response.put("content", pageContent);
        response.put("totalElements", totalElements);
        response.put("totalPages", totalPages);
        response.put("currentPage", page);
        response.put("pageSize", size);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取客户详情
     */
    @GetMapping("/{customerId}")
    @Operation(summary = "获取客户详情")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long customerId) {
        return customerRepository.findById(customerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 创建客户
     */
    @PostMapping
    @Operation(summary = "创建客户")
    public ResponseEntity<?> createCustomer(@Valid @RequestBody CustomerRequest request) {
        // 检查用户名是否已存在
        List<Customer> existingCustomers = customerRepository.findByHotelId(request.getHotelId());
        log.info("检查用户名是否存在: '{}', 酒店ID: {}, 现有客户数量: {}", request.getName(), request.getHotelId(), existingCustomers.size());

        // 更简单的检查逻辑
        boolean nameExists = false;
        Customer existingCustomer = null;
        for (Customer c : existingCustomers) {
            if (c.getName() != null && c.getName().trim().equals(request.getName().trim())) {
                nameExists = true;
                existingCustomer = c;
                break;
            }
        }

        log.info("用户名检查结果: {}, 现有客户ID: {}", nameExists, existingCustomer != null ? existingCustomer.getId() : "null");

        if (nameExists) {
            return ResponseEntity.badRequest()
                    .body("用户名 '" + request.getName() + "' 已存在，请使用其他用户名或直接选择现有用户进行预订");
        }

        Customer customer = Customer.builder()
                .hotelId(request.getHotelId())
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .country(request.getCountry() != null ? request.getCountry() : "CN")
                .idCardNumber(request.getIdCardNumber())
                .vipLevel(request.getVipLevel() != null ? request.getVipLevel() : Customer.VipLevel.normal)
                .notes(request.getNotes())
                .isRepeatedGuest(false)
                .totalStays(0)
                .totalCancellations(0)
                .build();

        Customer savedCustomer = customerRepository.save(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCustomer);
    }

    /**
     * 更新客户
     */
    @PutMapping("/{customerId}")
    @Operation(summary = "更新客户信息")
    public ResponseEntity<Customer> updateCustomer(
            @PathVariable Long customerId,
            @Valid @RequestBody CustomerRequest request) {
        return customerRepository.findById(customerId)
                .map(customer -> {
                    if (request.getName() != null) customer.setName(request.getName());
                    if (request.getEmail() != null) customer.setEmail(request.getEmail());
                    if (request.getPhone() != null) customer.setPhone(request.getPhone());
                    if (request.getCountry() != null) customer.setCountry(request.getCountry());
                    if (request.getIdCardNumber() != null) customer.setIdCardNumber(request.getIdCardNumber());
                    if (request.getVipLevel() != null) customer.setVipLevel(request.getVipLevel());
                    if (request.getNotes() != null) customer.setNotes(request.getNotes());
                    customer.setUpdatedAt(LocalDateTime.now());
                    return ResponseEntity.ok(customerRepository.save(customer));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 删除客户（只有当客户没有活跃预订时才能删除）
     */
    @DeleteMapping("/{customerId}")
    @Operation(summary = "删除客户", description = "只有当客户没有活跃预订（未取消且未完成的预订）时才能删除")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long customerId) {
        try {
            // 检查客户是否存在
            Customer customer = customerRepository.findById(customerId).orElse(null);
            if (customer == null) {
                return ResponseEntity.notFound().build();
            }

            // 检查客户是否有活跃预订（未取消且未完成的预订）
               List<Booking> activeBookings = bookingRepository.findByHotelIdAndCustomerId(customer.getHotelId(), customerId)
                       .stream()
                       .filter(booking -> booking.getStatus() != Booking.BookingStatus.canceled && booking.getStatus() != Booking.BookingStatus.completed)
                       .collect(Collectors.toList());

            if (!activeBookings.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("无法删除客户：客户还有" + activeBookings.size() + "个活跃预订需要处理");
            }

            // 删除客户
            customerRepository.deleteById(customerId);
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("删除客户失败, customerId: {}, error: {}", customerId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("删除客户失败: " + e.getMessage());
        }
    }

    /**
     * 获取客户统计信息
     */
    @GetMapping("/hotel/{hotelId}/statistics")
    @Operation(summary = "获取客户统计信息")
    public ResponseEntity<Map<String, Object>> getCustomerStatistics(@PathVariable Long hotelId) {
        List<Customer> allCustomers = customerRepository.findByHotelId(hotelId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCustomers", allCustomers.size());
        stats.put("vipCustomers", allCustomers.stream()
                .filter(c -> c.getVipLevel() != Customer.VipLevel.normal)
                .count());
        stats.put("repeatedGuests", allCustomers.stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsRepeatedGuest()))
                .count());

        // 本月新增客户
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long newThisMonth = allCustomers.stream()
                .filter(c -> c.getCreatedAt() != null && c.getCreatedAt().isAfter(startOfMonth))
                .count();
        stats.put("newThisMonth", newThisMonth);

        return ResponseEntity.ok(stats);
    }

    /**
     * 根据会员ID获取客户画像
     */
    @GetMapping("/profile/{memberId}")
    @Operation(summary = "获取客户画像", description = "根据会员ID获取完整的客户画像信息")
    public ResponseEntity<Customer> getCustomerProfile(@PathVariable String memberId) {
        return customerRepository.findByMemberId(memberId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 更新客户偏好标签
     */
    @PutMapping("/{customerId}/preferences")
    @Operation(summary = "更新客户偏好标签")
    public ResponseEntity<Customer> updateCustomerPreferences(
            @PathVariable Long customerId,
            @RequestParam String preferenceTags) {
        return customerRepository.findById(customerId)
                .map(customer -> {
                    customer.setPreferenceTags(preferenceTags);
                    customer.setUpdatedAt(LocalDateTime.now());
                    return ResponseEntity.ok(customerRepository.save(customer));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 更新客户标签
     */
    @PutMapping("/{customerId}/tags")
    @Operation(summary = "更新客户标签")
    public ResponseEntity<Customer> updateCustomerTags(
            @PathVariable Long customerId,
            @RequestParam String tags) {
        return customerRepository.findById(customerId)
                .map(customer -> {
                    customer.setTags(tags);
                    customer.setUpdatedAt(LocalDateTime.now());
                    return ResponseEntity.ok(customerRepository.save(customer));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 搜索客户（支持按偏好标签搜索）
     */
    @GetMapping("/search")
    @Operation(summary = "搜索客户", description = "支持按姓名、偏好标签等搜索客户")
    public ResponseEntity<List<Customer>> searchCustomers(
            @RequestParam Long hotelId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String preference) {

        List<Customer> customers = customerRepository.findByHotelId(hotelId);

        // 按关键词搜索（姓名或会员ID）
        if (keyword != null && !keyword.isEmpty()) {
            customers = customers.stream()
                    .filter(c -> (c.getName() != null && c.getName().toLowerCase().contains(keyword.toLowerCase())) ||
                               (c.getMemberId() != null && c.getMemberId().toLowerCase().contains(keyword.toLowerCase())))
                    .collect(Collectors.toList());
        }

        // 按偏好标签搜索
        if (preference != null && !preference.isEmpty()) {
            customers = customers.stream()
                    .filter(c -> c.getPreferenceTags() != null &&
                               c.getPreferenceTags().toLowerCase().contains(preference.toLowerCase()))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(customers);
    }

    /**
     * 获取客户画像特征（用于AI预测）
     */
    @GetMapping("/{customerId}/features")
    @Operation(summary = "获取客户画像特征", description = "提取客户画像特征用于AI预测")
    public ResponseEntity<Map<String, Object>> getCustomerFeatures(@PathVariable Long customerId) {
        return customerRepository.findById(customerId)
                .map(customer -> {
                    Map<String, Object> features = new HashMap<>();
                    features.put("customerId", customer.getId());
                    features.put("memberId", customer.getMemberId());
                    features.put("vipLevel", customer.getVipLevel());
                    features.put("totalStays", customer.getTotalStays());
                    features.put("avgSpend", customer.getAvgSpend());
                    features.put("lastCheckIn", customer.getLastCheckIn());
                    features.put("preferenceTags", customer.getPreferenceTags());
                    features.put("tags", customer.getTags());
                    features.put("isRepeatedGuest", customer.getIsRepeatedGuest());
                    return ResponseEntity.ok(features);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 更新客户AI预测模型数据
     */
    @PutMapping("/{customerId}/prediction-model")
    @Operation(summary = "更新AI预测模型数据")
    public ResponseEntity<Customer> updatePredictionModel(
            @PathVariable Long customerId,
            @RequestBody String predictionModelData) {
        return customerRepository.findById(customerId)
                .map(customer -> {
                    customer.setPredictionModelData(predictionModelData);
                    customer.setUpdatedAt(LocalDateTime.now());
                    return ResponseEntity.ok(customerRepository.save(customer));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 重新生成单个客户的标签
     */
    @PostMapping("/{customerId}/regenerate-tags")
    @Operation(summary = "重新生成客户标签", description = "基于预订历史重新生成客户的个性化标签")
    public ResponseEntity<?> regenerateCustomerTags(@PathVariable Long customerId) {
        try {
            customerProfileService.updateCustomerTags(customerId);
            return ResponseEntity.ok(Map.of(
                    "message", "客户标签生成成功",
                    "customerId", customerId
            ));
        } catch (Exception e) {
            log.error("生成客户标签失败, customerId: {}, error: {}", customerId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "生成标签失败: " + e.getMessage()));
        }
    }

    /**
     * 批量重新生成所有客户的标签
     */
    @PostMapping("/hotel/{hotelId}/regenerate-all-tags")
    @Operation(summary = "批量生成客户标签", description = "基于预订历史批量重新生成所有客户的个性化标签")
    public ResponseEntity<?> batchRegenerateCustomerTags(@PathVariable Long hotelId) {
        try {
            Map<String, Object> result = customerProfileService.batchUpdateAllCustomerTags(hotelId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("批量生成客户标签失败, hotelId: {}, error: {}", hotelId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "批量生成标签失败: " + e.getMessage()));
        }
    }
}









