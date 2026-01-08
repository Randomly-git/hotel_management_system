package com.hotel.hotel.service;

import com.hotel.hotel.dto.OverbookingRecommendationDTO;
import com.hotel.hotel.entity.Booking;
import com.hotel.hotel.entity.OverbookingConfig;
import com.hotel.hotel.entity.QLearningState;
import com.hotel.hotel.repository.BookingRepository;
import com.hotel.hotel.repository.HotelRoomTypeRepository;
import com.hotel.hotel.repository.OverbookingConfigRepository;
import com.hotel.hotel.repository.QLearningStateRepository;
import com.hotel.hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

/**
 * Q-Learning 强化学习服务
 * 实现智能超售决策系统
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QLearningService {

    private final QLearningStateRepository qLearningStateRepository;
    private final OverbookingConfigRepository overbookingConfigRepository;
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final HotelRoomTypeRepository roomTypeRepository;

    // Q-Learning 超参数
    private static final double ALPHA = 0.1;      // 学习率
    private static final double GAMMA = 0.9;      // 折扣因子
    private static final double EPSILON = 0.1;    // 探索率
    private static final Random random = new Random();

    /**
     * 获取超售建议
     * 使用 epsilon-greedy 策略选择动作
     */
    @Transactional
    public OverbookingRecommendationDTO getRecommendation(Long hotelId, Long roomTypeId, LocalDate targetDate) {
        // 获取配置
        OverbookingConfig config = getConfig(hotelId, roomTypeId);
        if (!config.getEnabled()) {
            return OverbookingRecommendationDTO.builder()
                    .hotelId(hotelId)
                    .roomTypeId(roomTypeId)
                    .recommendedOverbook(0)
                    .confidence(BigDecimal.ZERO)
                    .reason("超售功能未启用")
                    .build();
        }

        // 计算状态
        String stateKey = encodeState(hotelId, roomTypeId, targetDate);

        // 获取或创建Q表条目
        QLearningState qState = getOrCreateQState(hotelId, roomTypeId, stateKey);

        // Epsilon-greedy 策略
        int action;
        if (random.nextDouble() < EPSILON) {
            // 探索：随机选择动作
            action = random.nextInt(config.getMaxOverbook() + 1);
            log.debug("探索模式：随机选择动作 {}", action);
        } else {
            // 利用：选择Q值最大的动作
            action = qState.getBestAction();
            log.debug("利用模式：选择最佳动作 {}", action);
        }

        // 计算置信度（基于访问次数）
        BigDecimal confidence = calculateConfidence(qState.getVisitCount());

        // 获取对应Q值
        BigDecimal qValue = qState.getQValue(action);

        // 计算额外的统计信息
        BigDecimal currentOccupancy = calculateCurrentOccupancy(hotelId, roomTypeId, targetDate);
        BigDecimal expectedNoShow = calculateExpectedNoShow(hotelId, roomTypeId, targetDate);
        BigDecimal expectedRevenue = calculateExpectedRevenue(action, expectedNoShow, roomTypeId);
        String riskLevel = calculateRiskLevel(action, currentOccupancy);

        // 解析状态信息
        OverbookingRecommendationDTO.StateInfo stateInfo = parseStateInfo(stateKey);

        return OverbookingRecommendationDTO.builder()
                .hotelId(hotelId)
                .roomTypeId(roomTypeId)
                .targetDate(targetDate)
                .stateKey(stateKey)
                .recommendedOverbook(action)
                .qValue(qValue)
                .confidence(confidence)
                .visitCount(qState.getVisitCount())
                .reason(generateReason(stateKey, action, confidence))
                .currentOccupancy(currentOccupancy)
                .expectedNoShow(expectedNoShow)
                .expectedRevenue(expectedRevenue)
                .riskLevel(riskLevel)
                .state(stateInfo)
                .build();
    }

    /**
     * 更新Q值（基于实际结果）
     * Q(s,a) = Q(s,a) + α * [r + γ * max Q(s',a') - Q(s,a)]
     */
    @Transactional
    public void updateQValue(
            Long hotelId,
            Long roomTypeId,
            String stateKey,
            int action,
            BigDecimal reward,
            String nextStateKey
    ) {
        // 获取当前状态
        QLearningState currentQState = getOrCreateQState(hotelId, roomTypeId, stateKey);
        BigDecimal currentQ = currentQState.getQValue(action);

        // 获取下一状态的最大Q值
        QLearningState nextQState = getOrCreateQState(hotelId, roomTypeId, nextStateKey);
        BigDecimal maxNextQ = nextQState.getMaxQValue();

        // Bellman方程更新
        BigDecimal newQ = currentQ.add(
                BigDecimal.valueOf(ALPHA).multiply(
                        reward.add(
                                BigDecimal.valueOf(GAMMA).multiply(maxNextQ)
                        ).subtract(currentQ)
                )
        ).setScale(2, RoundingMode.HALF_UP);

        // 更新Q值
        currentQState.setQValue(action, newQ);
        currentQState.setVisitCount(currentQState.getVisitCount() + 1);
        qLearningStateRepository.save(currentQState);

        log.info("更新Q值: state={}, action={}, reward={}, oldQ={}, newQ={}",
                stateKey, action, reward, currentQ, newQ);
    }

    /**
     * 计算奖励
     * 收益 = 额外预订收入 - 溢出赔偿成本
     */
    public BigDecimal calculateReward(
            int overbookAmount,
            int actualCancellations,
            int actualNoShows,
            BigDecimal roomPrice,
            BigDecimal compensationRate
    ) {
        // 成功情况：超售量正好抵消取消/未到
        if (actualCancellations + actualNoShows >= overbookAmount) {
            // 全部超售成功，获得额外收入
            return roomPrice.multiply(BigDecimal.valueOf(overbookAmount));
        }

        // 溢出情况：需要赔偿
        int overflow = overbookAmount - (actualCancellations + actualNoShows);
        BigDecimal extraRevenue = roomPrice.multiply(BigDecimal.valueOf(actualCancellations + actualNoShows));
        BigDecimal compensationCost = roomPrice.multiply(BigDecimal.valueOf(overflow)).multiply(compensationRate);

        BigDecimal reward = extraRevenue.subtract(compensationCost);
        log.info("计算奖励: 超售={}, 取消={}, 未到={}, 溢出={}, 奖励={}",
                overbookAmount, actualCancellations, actualNoShows, overflow, reward);

        return reward;
    }

    /**
     * 编码状态
     * 格式: {入住率等级}_{星期类型}_{提前期类别}
     * 示例: OCC_HIGH_SAT_2
     */
    private String encodeState(Long hotelId, Long roomTypeId, LocalDate targetDate) {
        // 获取当前预订情况（这里简化处理，实际应查询数据库）
        int currentBookings = getCurrentBookingCount(hotelId, roomTypeId, targetDate);
        int totalRooms = getTotalRoomCount(hotelId, roomTypeId);

        // 入住率等级
        String occupancyLevel;
        double occupancyRate = (double) currentBookings / totalRooms;
        if (occupancyRate >= 0.9) {
            occupancyLevel = "VERY_HIGH";
        } else if (occupancyRate >= 0.7) {
            occupancyLevel = "HIGH";
        } else if (occupancyRate >= 0.5) {
            occupancyLevel = "MEDIUM";
        } else {
            occupancyLevel = "LOW";
        }

        // 星期类型
        DayOfWeek dayOfWeek = targetDate.getDayOfWeek();
        String dayType = dayOfWeek.toString().substring(0, 3).toUpperCase(); // MON, TUE, etc.

        // 提前期类别（距离目标日期的天数）
        long daysUntil = LocalDate.now().until(targetDate).getDays();
        String leadCategory;
        if (daysUntil <= 7) {
            leadCategory = "0";  // 一周内
        } else if (daysUntil <= 30) {
            leadCategory = "1";  // 一个月内
        } else {
            leadCategory = "2";  // 一个月以上
        }

        return String.format("OCC_%s_%s_%s", occupancyLevel, dayType, leadCategory);
    }

    /**
     * 获取或创建Q状态
     */
    private QLearningState getOrCreateQState(Long hotelId, Long roomTypeId, String stateKey) {
        return qLearningStateRepository
                .findByHotelIdAndRoomTypeIdAndStateKey(hotelId, roomTypeId, stateKey)
                .orElseGet(() -> {
                    QLearningState newState = QLearningState.builder()
                            .hotelId(hotelId)
                            .roomTypeId(roomTypeId)
                            .stateKey(stateKey)
                            .action0(BigDecimal.ZERO)
                            .action1(BigDecimal.ZERO)
                            .action2(BigDecimal.ZERO)
                            .action3(BigDecimal.ZERO)
                            .action4(BigDecimal.ZERO)
                            .action5(BigDecimal.ZERO)
                            .visitCount(0)
                            .build();
                    return qLearningStateRepository.save(newState);
                });
    }

    /**
     * 获取配置
     */
    private OverbookingConfig getConfig(Long hotelId, Long roomTypeId) {
        return overbookingConfigRepository
                .findByHotelIdAndRoomTypeId(hotelId, roomTypeId)
                .orElse(OverbookingConfig.builder()
                        .hotelId(hotelId)
                        .roomTypeId(roomTypeId)
                        .maxOverbook(5)
                        .compensationRate(new BigDecimal("1.5"))
                        .enabled(true)
                        .build());
    }

    /**
     * 计算置信度（基于访问次数）
     */
    private BigDecimal calculateConfidence(Integer visitCount) {
        if (visitCount == null || visitCount == 0) {
            return BigDecimal.ZERO;
        }
        // 使用对数函数，100次访问达到约0.86的置信度
        double confidence = Math.min(1.0, Math.log(visitCount + 1) / Math.log(100));
        return BigDecimal.valueOf(confidence).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 生成建议理由
     */
    private String generateReason(String stateKey, int action, BigDecimal confidence) {
        String[] parts = stateKey.split("_");
        String occupancy = parts[1];
        String day = parts[2];

        StringBuilder reason = new StringBuilder();
        reason.append("基于").append(occupancy).append("入住率");
        reason.append("，").append(day).append("情况");

        if (action > 0) {
            reason.append("，建议超售").append(action).append("间房");
        } else {
            reason.append("，不建议超售");
        }

        reason.append("（置信度：").append(confidence.multiply(BigDecimal.valueOf(100))).append("%）");

        return reason.toString();
    }

    /**
     * 获取当前预订数（查询指定日期和房型的确认预订数）
     */
    private int getCurrentBookingCount(Long hotelId, Long roomTypeId, LocalDate date) {
        try {
            // 统计指定日期、房型和状态为booked的预订数
            long count = bookingRepository.countByHotelIdAndRoomTypeIdAndCheckInDateAndStatus(
                    hotelId, roomTypeId, date, Booking.BookingStatus.booked
            );
            log.debug("当前预订数: hotelId={}, roomTypeId={}, date={}, count={}",
                    hotelId, roomTypeId, date, count);
            return (int) count;
        } catch (Exception e) {
            log.error("查询当前预订数失败: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 获取总房间数（查询指定酒店和房型的房间总数）
     */
    private int getTotalRoomCount(Long hotelId, Long roomTypeId) {
        try {
            long count = roomRepository.countByHotelIdAndRoomTypeId(hotelId, roomTypeId);
            log.debug("总房间数: hotelId={}, roomTypeId={}, count={}", hotelId, roomTypeId, count);
            return (int) count;
        } catch (Exception e) {
            log.error("查询总房间数失败: {}", e.getMessage());
            return 10; // 降级返回默认值
        }
    }

    /**
     * 计算当前入住率（基于实际预订数据）
     */
    private BigDecimal calculateCurrentOccupancy(Long hotelId, Long roomTypeId, LocalDate targetDate) {
        try {
            int currentBookings = getCurrentBookingCount(hotelId, roomTypeId, targetDate);
            int totalRooms = getTotalRoomCount(hotelId, roomTypeId);

            if (totalRooms == 0) {
                return BigDecimal.ZERO;
            }

            double occupancyRate = (double) currentBookings / totalRooms * 100;
            return BigDecimal.valueOf(occupancyRate).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.error("计算当前入住率失败: {}", e.getMessage());
            return BigDecimal.valueOf(75); // 降级返回默认值
        }
    }

    /**
     * 计算预计No-show率（基于历史数据）
     * 使用取消率作为No-show率的代理指标
     */
    private BigDecimal calculateExpectedNoShow(Long hotelId, Long roomTypeId, LocalDate targetDate) {
        try {
            // 查询过去30天同一房型的所有预订
            LocalDate startDate = targetDate.minusDays(30);
            List<Booking> historicalBookings = bookingRepository.findBookingsByRoomTypeAndDateRange(
                    hotelId, roomTypeId, startDate, targetDate
            );

            if (historicalBookings.isEmpty()) {
                // 没有历史数据，返回行业平均值
                return BigDecimal.valueOf(10);
            }

            // 统计取消数量（使用取消率作为No-show率的代理）
            long totalBookings = historicalBookings.size();
            long canceledCount = historicalBookings.stream()
                    .filter(b -> b.getIsCanceled() != null && b.getIsCanceled())
                    .count();

            // No-show率通常略高于取消率，我们使用取消率的1.2倍作为估计
            double cancelRate = (double) canceledCount / totalBookings;
            double estimatedNoShowRate = cancelRate * 1.2 * 100;

            // 限制在合理范围内（5%-25%）
            estimatedNoShowRate = Math.max(5, Math.min(25, estimatedNoShowRate));

            return BigDecimal.valueOf(estimatedNoShowRate).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.error("计算预计No-show率失败: {}", e.getMessage());
            return BigDecimal.valueOf(10); // 降级返回行业平均值
        }
    }

    /**
     * 计算预期增收
     */
    private BigDecimal calculateExpectedRevenue(int recommendedOverbook, BigDecimal expectedNoShow, Long roomTypeId) {
        // 获取房型实际价格
        try {
            com.hotel.hotel.entity.HotelRoomType roomType = roomTypeRepository.findById(roomTypeId).orElse(null);
            BigDecimal roomPrice = roomType != null ? roomType.getBasePrice() : BigDecimal.valueOf(500);

            BigDecimal successRate = BigDecimal.ONE.subtract(expectedNoShow.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            return BigDecimal.valueOf(recommendedOverbook).multiply(roomPrice).multiply(successRate).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.error("获取房型价格失败: {}", e.getMessage());
            // 降级使用默认价格
            BigDecimal roomPrice = BigDecimal.valueOf(500);
            BigDecimal successRate = BigDecimal.ONE.subtract(expectedNoShow.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            return BigDecimal.valueOf(recommendedOverbook).multiply(roomPrice).multiply(successRate).setScale(2, RoundingMode.HALF_UP);
        }
    }

    /**
     * 计算风险等级
     */
    private String calculateRiskLevel(int recommendedOverbook, BigDecimal currentOccupancy) {
        if (recommendedOverbook > 3 || currentOccupancy.compareTo(BigDecimal.valueOf(90)) > 0) {
            return "高";
        } else if (recommendedOverbook > 1 || currentOccupancy.compareTo(BigDecimal.valueOf(80)) > 0) {
            return "中";
        } else {
            return "低";
        }
    }

    /**
     * 解析状态信息
     */
    private OverbookingRecommendationDTO.StateInfo parseStateInfo(String stateKey) {
        // 解析状态键，格式如：OCC_HIGH_MON_1
        String[] parts = stateKey.split("_");
        if (parts.length >= 3) {
            String occupancyLevel = parts[1];
            String dayType = parts[2];
            String leadTimeCategory = parts.length > 3 ? parts[3] : "1";

            // 转换显示格式
            String occupancyDisplay = switch (occupancyLevel) {
                case "LOW" -> "低";
                case "MEDIUM" -> "中";
                case "HIGH" -> "高";
                case "VERY_HIGH" -> "很高";
                default -> "未知";
            };

            String dayTypeDisplay = switch (dayType) {
                case "MON", "TUE", "WED", "THU", "FRI" -> "工作日";
                case "SAT", "SUN" -> "周末";
                default -> "未知";
            };

            return OverbookingRecommendationDTO.StateInfo.builder()
                    .occupancyLevel(occupancyDisplay)
                    .dayType(dayTypeDisplay)
                    .leadTimeCategory(leadTimeCategory)
                    .build();
        }

        return OverbookingRecommendationDTO.StateInfo.builder()
                .occupancyLevel("未知")
                .dayType("未知")
                .leadTimeCategory("未知")
                .build();
    }
}
