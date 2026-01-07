package com.hotel.hotel.service;

import com.hotel.hotel.entity.Booking;
import com.hotel.hotel.entity.Customer;
import com.hotel.hotel.repository.BookingRepository;
import com.hotel.hotel.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 客户画像服务
 * 基于预订历史生成个性化客户标签
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerProfileService {

    private final CustomerRepository customerRepository;
    private final BookingRepository bookingRepository;

    /**
     * 更新单个客户的标签
     */
    @Transactional
    public void updateCustomerTags(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("客户不存在: " + customerId));

        List<Booking> bookings = bookingRepository.findByHotelIdAndCustomerId(
                customer.getHotelId(), customerId);

        if (bookings.isEmpty()) {
            log.info("客户 {} 没有预订历史", customerId);
            return;
        }

        String tags = generateTags(bookings);
        String preferenceTags = generatePreferenceTags(bookings);

        customer.setTags(tags);
        customer.setPreferenceTags(preferenceTags);
        customerRepository.save(customer);

        log.info("更新客户 {} 的标签: {}", customerId, tags);
    }

    /**
     * 批量更新所有客户的标签
     */
    @Transactional
    public Map<String, Object> batchUpdateAllCustomerTags(Long hotelId) {
        List<Customer> customers = customerRepository.findByHotelId(hotelId);
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();

        for (Customer customer : customers) {
            try {
                updateCustomerTags(customer.getId());
                successCount++;
            } catch (Exception e) {
                failCount++;
                errors.add("客户ID " + customer.getId() + ": " + e.getMessage());
                log.error("更新客户 {} 标签失败", customer.getId(), e);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", customers.size());
        result.put("success", successCount);
        result.put("failed", failCount);
        result.put("errors", errors);

        log.info("批量更新完成: 总数 {}, 成功 {}, 失败 {}", customers.size(), successCount, failCount);
        return result;
    }

    /**
     * 生成行为标签
     * 基于客户的预订行为模式
     */
    private String generateTags(List<Booking> bookings) {
        Set<String> tags = new LinkedHashSet<>();

        // 1. 入住频率标签
        long completedBookings = bookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.completed)
                .count();
        if (completedBookings >= 10) {
            tags.add("常客");
        } else if (completedBookings >= 5) {
            tags.add("回头客");
        } else if (completedBookings >= 2) {
            tags.add("再次光临");
        }

        // 2. 取消行为标签
        long canceledBookings = bookings.stream()
                .filter(b -> b.getIsCanceled() != null && b.getIsCanceled())
                .count();
        double cancelRate = bookings.isEmpty() ? 0 : (double) canceledBookings / bookings.size();
        if (cancelRate > 0.5) {
            tags.add("频繁取消");
        } else if (cancelRate > 0.2) {
            tags.add("偶尔取消");
        } else if (cancelRate == 0 && bookings.size() > 3) {
            tags.add("守信客户");
        }

        // 3. 预订提前期标签
        double avgLeadTime = bookings.stream()
                .filter(b -> b.getLeadTime() != null)
                .mapToInt(Booking::getLeadTime)
                .average()
                .orElse(0);
        if (avgLeadTime > 60) {
            tags.add("提前规划者");
        } else if (avgLeadTime > 30) {
            tags.add("提前预订");
        } else if (avgLeadTime > 7) {
            tags.add("提前一周");
        } else if (avgLeadTime > 0) {
            tags.add("临时决定");
        }

        // 4. 入住时长标签
        double avgNights = bookings.stream()
                .filter(b -> b.getTotalNights() != null)
                .mapToInt(Booking::getTotalNights)
                .average()
                .orElse(0);
        if (avgNights >= 7) {
            tags.add("长期住客");
        } else if (avgNights >= 4) {
            tags.add("中等停留");
        } else if (avgNights > 0 && avgNights <= 2) {
            tags.add("短期停留");
        }

        // 5. 入住时间偏好标签
        Map<DayOfWeek, Long> weekdayCounts = bookings.stream()
                .filter(b -> b.getCheckInDate() != null)
                .collect(Collectors.groupingBy(
                        b -> b.getCheckInDate().getDayOfWeek(),
                        Collectors.counting()
                ));

        long weekendCount = weekdayCounts.getOrDefault(DayOfWeek.FRIDAY, 0L) +
                           weekdayCounts.getOrDefault(DayOfWeek.SATURDAY, 0L) +
                           weekdayCounts.getOrDefault(DayOfWeek.SUNDAY, 0L);
        long weekdayCount = weekdayCounts.values().stream().mapToLong(Long::longValue).sum() - weekendCount;

        if (weekendCount > weekdayCount * 1.5) {
            tags.add("周末偏好");
        } else if (weekdayCount > weekendCount * 2) {
            tags.add("工作日偏好");
        }

        // 6. 季节偏好标签
        Map<Month, Long> monthCounts = bookings.stream()
                .filter(b -> b.getCheckInDate() != null)
                .collect(Collectors.groupingBy(
                        b -> b.getCheckInDate().getMonth(),
                        Collectors.counting()
                ));

        Month peakMonth = monthCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if (peakMonth != null && !monthCounts.isEmpty()) {
            long peakCount = monthCounts.get(peakMonth);
            if (peakCount > bookings.size() * 0.4) {
                switch (peakMonth) {
                    case DECEMBER, JANUARY, FEBRUARY:
                        tags.add("冬季偏好");
                        break;
                    case MARCH, APRIL, MAY:
                        tags.add("春季偏好");
                        break;
                    case JUNE, JULY, AUGUST:
                        tags.add("夏季偏好");
                        break;
                    case SEPTEMBER, OCTOBER, NOVEMBER:
                        tags.add("秋季偏好");
                        break;
                }
            }
        }

        // 7. 特殊需求标签
        long specialRequestsCount = bookings.stream()
                .filter(b -> b.getSpecialRequests() != null && b.getSpecialRequests() > 0)
                .count();
        if (specialRequestsCount > bookings.size() * 0.5) {
            tags.add("需求多样");
        } else if (specialRequestsCount > 0) {
            tags.add("特殊需求");
        }

        // 8. 变更行为标签
        long changesCount = bookings.stream()
                .filter(b -> b.getBookingChanges() != null && b.getBookingChanges() > 0)
                .count();
        if (changesCount > bookings.size() * 0.5) {
            tags.add("频繁变更");
        }

        // 9. 餐饮偏好标签
        Map<String, Long> mealCounts = bookings.stream()
                .filter(b -> b.getMealType() != null)
                .collect(Collectors.groupingBy(
                        b -> b.getMealType().name(),
                        Collectors.counting()
                ));

        String preferredMeal = mealCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if (preferredMeal != null && !mealCounts.isEmpty()) {
            long mealCount = mealCounts.get(preferredMeal);
            if (mealCount > bookings.size() * 0.5) {
                switch (preferredMeal) {
                    case "FB":
                        tags.add("全餐偏好");
                        break;
                    case "HB":
                        tags.add("半餐偏好");
                        break;
                    case "BB":
                        tags.add("早餐偏好");
                        break;
                    case "SC":
                        tags.add("无餐偏好");
                        break;
                }
            }
        }

        // 10. 押金类型标签
        Map<Booking.DepositType, Long> depositCounts = bookings.stream()
                .filter(b -> b.getDepositType() != null)
                .collect(Collectors.groupingBy(
                        Booking::getDepositType,
                        Collectors.counting()
                ));

        if (depositCounts.getOrDefault(Booking.DepositType.non_refund, 0L) > bookings.size() * 0.5) {
            tags.add("不退押金");
        }

        // 11. 儿童出行标签
        long withChildren = bookings.stream()
                .filter(b -> b.getChildren() != null && b.getChildren() > 0)
                .count();
        if (withChildren > bookings.size() * 0.5) {
            tags.add("家庭出行");
        } else if (withChildren > 0) {
            tags.add("携带儿童");
        }

        // 12. 婴儿出行标签
        long withBabies = bookings.stream()
                .filter(b -> b.getBabies() != null && b.getBabies() > 0)
                .count();
        if (withBabies > 0) {
            tags.add("携婴儿出行");
        }

        // 13. 停车需求标签
        long withParking = bookings.stream()
                .filter(b -> b.getRequiredCarParkingSpaces() != null && b.getRequiredCarParkingSpaces() > 0)
                .count();
        if (withParking > bookings.size() * 0.5) {
            tags.add("自驾客户");
        }

        // 14. 价格敏感度标签（基于平均每日房价）
        OptionalDouble avgAdr = bookings.stream()
                .filter(b -> b.getAdr() != null && b.getAdr().compareTo(BigDecimal.ZERO) > 0)
                .mapToDouble(b -> b.getAdr().doubleValue())
                .average();

        if (avgAdr.isPresent()) {
            double adr = avgAdr.getAsDouble();
            if (adr > 1000) {
                tags.add("高端消费");
            } else if (adr > 500) {
                tags.add("中高消费");
            } else if (adr < 200) {
                tags.add("经济实惠");
            }
        }

        // 15. 渠道偏好标签
        Map<String, Long> channelCounts = bookings.stream()
                .filter(b -> b.getDistributionChannel() != null)
                .collect(Collectors.groupingBy(
                        Booking::getDistributionChannel,
                        Collectors.counting()
                ));

        if (!channelCounts.isEmpty()) {
            String preferredChannel = channelCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            if (preferredChannel != null) {
                long channelCount = channelCounts.get(preferredChannel);
                if (channelCount > bookings.size() * 0.6) {
                    switch (preferredChannel.toLowerCase()) {
                        case "online":
                            tags.add("线上偏好");
                            break;
                        case "offline":
                            tags.add("线下偏好");
                            break;
                        case "direct":
                            tags.add("直客偏好");
                            break;
                        case "ta", "travel agent":
                            tags.add("旅行社偏好");
                            break;
                        case "corporate":
                            tags.add("企业客户");
                            break;
                    }
                }
            }
        }

        // 如果没有任何标签，添加默认标签
        if (tags.isEmpty()) {
            tags.add("新客户");
        }

        return String.join(",", tags);
    }

    /**
     * 生成偏好标签
     * 基于客户对房型、设施等的偏好
     */
    private String generatePreferenceTags(List<Booking> bookings) {
        Set<String> preferences = new LinkedHashSet<>();

        // 1. 房型偏好（从房型名称中分析）
        Map<Long, Long> roomTypeCounts = bookings.stream()
                .filter(b -> b.getRoomTypeId() != null)
                .collect(Collectors.groupingBy(
                        Booking::getRoomTypeId,
                        Collectors.counting()
                ));

        if (!roomTypeCounts.isEmpty()) {
            Long preferredRoomType = roomTypeCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            if (preferredRoomType != null) {
                long count = roomTypeCounts.get(preferredRoomType);
                if (count > bookings.size() * 0.5) {
                    preferences.add("房型固定");
                }
            }
        }

        // 2. 楼层偏好（从房间号分析）
        Set<String> floors = bookings.stream()
                .filter(b -> b.getAssignedRoomId() != null)
                .map(b -> {
                    // 从房间ID或实际房间类型推断楼层
                    String roomType = b.getActualRoomType();
                    return roomType != null ? "楼层" + roomType : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (!floors.isEmpty()) {
            preferences.addAll(floors);
        }

        // 3. 价格区间偏好
        OptionalDouble avgPrice = bookings.stream()
                .filter(b -> b.getTotalPrice() != null)
                .mapToDouble(b -> b.getTotalPrice().doubleValue())
                .average();

        if (avgPrice.isPresent()) {
            double price = avgPrice.getAsDouble();
            if (price > 5000) {
                preferences.add("高价位偏好");
            } else if (price > 2000) {
                preferences.add("中价位偏好");
            } else if (price > 0) {
                preferences.add("经济价位偏好");
            }
        }

        // 4. 成人人数偏好
        OptionalDouble avgAdults = bookings.stream()
                .filter(b -> b.getAdults() != null)
                .mapToInt(Booking::getAdults)
                .average();

        if (avgAdults.isPresent()) {
            double adults = avgAdults.getAsDouble();
            if (adults >= 3) {
                preferences.add("多人出行");
            } else if (adults == 1) {
                preferences.add("单人出行");
            } else if (adults == 2) {
                preferences.add("双人出行");
            }
        }

        // 5. 最近活跃度
        Optional<Booking> latestBooking = bookings.stream()
                .filter(b -> b.getBookingDate() != null)
                .max(Comparator.comparing(Booking::getBookingDate));

        if (latestBooking.isPresent()) {
            long daysSinceLastBooking = ChronoUnit.DAYS.between(
                    latestBooking.get().getBookingDate().toLocalDate(),
                    LocalDate.now()
            );

            if (daysSinceLastBooking <= 30) {
                preferences.add("活跃客户");
            } else if (daysSinceLastBooking <= 90) {
                preferences.add("近期活跃");
            } else if (daysSinceLastBooking > 365) {
                preferences.add("流失风险");
            }
        }

        // 6. 提前预订习惯
        OptionalDouble avgLeadTime = bookings.stream()
                .filter(b -> b.getLeadTime() != null)
                .mapToInt(Booking::getLeadTime)
                .average();

        if (avgLeadTime.isPresent()) {
            double leadTime = avgLeadTime.getAsDouble();
            if (leadTime > 30) {
                preferences.add("提前规划");
            } else if (leadTime < 7) {
                preferences.add("临时预订");
            }
        }

        // 7. 客单价标签
        OptionalDouble avgTotalPrice = bookings.stream()
                .filter(b -> b.getTotalPrice() != null)
                .mapToDouble(b -> b.getTotalPrice().doubleValue())
                .average();

        if (avgTotalPrice.isPresent()) {
            double avgTotalPriceValue = avgTotalPrice.getAsDouble();
            if (avgTotalPriceValue > 10000) {
                preferences.add("高客单价");
            } else if (avgTotalPriceValue > 5000) {
                preferences.add("中高客单价");
            } else if (avgTotalPriceValue > 2000) {
                preferences.add("中等客单价");
            } else if (avgTotalPriceValue > 0) {
                preferences.add("经济型客单价");
            }
        }

        // 8. 特殊请求文本分析（如果有）
        Set<String> requestTypes = bookings.stream()
                .filter(b -> b.getRequestsText() != null && !b.getRequestsText().isEmpty())
                .map(b -> {
                    String text = b.getRequestsText().toLowerCase();
                    if (text.contains("高楼层") || text.contains("高层")) {
                        return "高层偏好";
                    } else if (text.contains("低楼层") || text.contains("低层")) {
                        return "低层偏好";
                    } else if (text.contains("安静") || text.contains("隔音")) {
                        return "安静偏好";
                    } else if (text.contains("风景") || text.contains("景观")) {
                        return "景观偏好";
                    } else if (text.contains("大床") || text.contains("特大")) {
                        return "大床偏好";
                    } else if (text.contains("双床") || text.contains("标间")) {
                        return "双床偏好";
                    } else if (text.contains("无烟")) {
                        return "无烟偏好";
                    } else if (text.contains("连通")) {
                        return "连通房需求";
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        preferences.addAll(requestTypes);

        return preferences.isEmpty() ? "暂无偏好" : String.join(",", preferences);
    }
}
