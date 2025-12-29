package com.hotel.hotel.controller;

import com.hotel.hotel.entity.Customer;
import com.hotel.hotel.repository.CustomerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
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
@Tag(name = "客户管理", description = "客户信息管理相关接口")
public class CustomerController {

    private final CustomerRepository customerRepository;

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
            // 支持逗号分隔的多个VIP等级
            String[] vipLevels = vipLevel.split(",");
            List<Customer.VipLevel> targetLevels = new ArrayList<>();
            for (String level : vipLevels) {
                try {
                    targetLevels.add(Customer.VipLevel.valueOf(level.trim().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    // 忽略无效的VIP等级
                }
            }
            if (!targetLevels.isEmpty()) {
                customers = customers.stream()
                        .filter(c -> c.getVipLevel() != null && targetLevels.contains(c.getVipLevel()))
                        .collect(Collectors.toList());
            }
        }
        if (isRepeatedGuest != null) {
            customers = customers.stream()
                    .filter(c -> Boolean.TRUE.equals(c.getIsRepeatedGuest()) == isRepeatedGuest)
                    .collect(Collectors.toList());
        }

        // 应用筛选条件（注意：这样筛选会改变分页的准确性，建议在后端实现）
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
            // 支持逗号分隔的多个VIP等级
            String[] vipLevels = vipLevel.split(",");
            List<Customer.VipLevel> targetLevels = new ArrayList<>();
            for (String level : vipLevels) {
                try {
                    targetLevels.add(Customer.VipLevel.valueOf(level.trim().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    // 忽略无效的VIP等级
                }
            }
            if (!targetLevels.isEmpty()) {
                customers = customers.stream()
                        .filter(c -> c.getVipLevel() != null && targetLevels.contains(c.getVipLevel()))
                        .collect(Collectors.toList());
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
    public ResponseEntity<Customer> createCustomer(@Valid @RequestBody CustomerRequest request) {
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
     * 删除客户
     */
    @DeleteMapping("/{customerId}")
    @Operation(summary = "删除客户")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long customerId) {
        if (customerRepository.existsById(customerId)) {
            customerRepository.deleteById(customerId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
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
}









