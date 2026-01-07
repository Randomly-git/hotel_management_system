package com.hotel.hotel.service;

import com.hotel.hotel.dto.OverbookingRecommendationDTO;
import com.hotel.hotel.entity.Booking;
import com.hotel.hotel.entity.HotelRoomType;
import com.hotel.hotel.entity.OverbookingConfig;
import com.hotel.hotel.entity.OverbookingDecision;
import com.hotel.hotel.repository.BookingRepository;
import com.hotel.hotel.repository.HotelRoomTypeRepository;
import com.hotel.hotel.repository.OverbookingConfigRepository;
import com.hotel.hotel.repository.OverbookingDecisionRepository;
import com.hotel.hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * 超售管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OverbookingService {

    private final QLearningService qLearningService;
    private final OverbookingConfigRepository overbookingConfigRepository;
    private final OverbookingDecisionRepository overbookingDecisionRepository;
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final HotelRoomTypeRepository hotelRoomTypeRepository;

    /**
     * 获取超售建议
     */
    public OverbookingRecommendationDTO getRecommendation(Long hotelId, Long roomTypeId, LocalDate targetDate) {
        return qLearningService.getRecommendation(hotelId, roomTypeId, targetDate);
    }

    /**
     * 应用超售决策
     */
    @Transactional
    public OverbookingDecision applyDecision(Long hotelId, Long roomTypeId, LocalDate decisionDate, int overbookAmount) {
        // 获取配置,如果不存在则创建默认配置
        OverbookingConfig config = overbookingConfigRepository
                .findByHotelIdAndRoomTypeId(hotelId, roomTypeId)
                .orElseGet(() -> {
                    // 创建默认配置
                    OverbookingConfig newConfig = OverbookingConfig.builder()
                            .hotelId(hotelId)
                            .roomTypeId(roomTypeId)
                            .maxOverbook(5)
                            .compensationRate(new BigDecimal("1.5"))
                            .enabled(true)
                            .build();
                    return overbookingConfigRepository.save(newConfig);
                });

        if (overbookAmount > config.getMaxOverbook()) {
            throw new IllegalArgumentException("超售量超过最大限制");
        }

        // 获取当前状态
        OverbookingRecommendationDTO recommendation = qLearningService.getRecommendation(hotelId, roomTypeId, decisionDate);

        // 获取房间统计
        int totalRooms = (int) roomRepository.countByHotelIdAndRoomTypeId(hotelId, roomTypeId);

        // 获取已预订的房间数 - 使用booked状态
        int confirmedBookings = (int) bookingRepository.countByHotelIdAndRoomTypeIdAndCheckInDateAndStatus(
                hotelId, roomTypeId, decisionDate, Booking.BookingStatus.booked
        );

        // 创建决策记录
        OverbookingDecision decision = OverbookingDecision.builder()
                .hotelId(hotelId)
                .roomTypeId(roomTypeId)
                .decisionDate(decisionDate)
                .stateKey(recommendation.getStateKey())
                .actionChosen(overbookAmount)
                .qValue(recommendation.getQValue())
                .totalRooms(totalRooms)
                .confirmedBookings(confirmedBookings)
                .actualCancellations(0)
                .actualNoShows(0)
                .wasSuccessful(null)
                .reward(null)
                .build();

        return overbookingDecisionRepository.save(decision);
    }

    /**
     * 记录实际结果并更新Q值
     */
    @Transactional
    public void recordOutcome(Long decisionId, int cancellations, int noShows) {
        OverbookingDecision decision = overbookingDecisionRepository.findById(decisionId)
                .orElseThrow(() -> new IllegalArgumentException("决策记录不存在"));

        decision.setActualCancellations(cancellations);
        decision.setActualNoShows(noShows);

        // 判断是否成功（没有溢出）
        boolean wasSuccessful = (cancellations + noShows) >= decision.getActionChosen();
        decision.setWasSuccessful(wasSuccessful);

        // 获取房型实际价格
        HotelRoomType roomType = hotelRoomTypeRepository.findById(decision.getRoomTypeId())
                .orElse(null);

        BigDecimal roomPrice;
        if (roomType == null) {
            log.warn("房型ID {} 不存在，使用默认价格 500", decision.getRoomTypeId());
            roomPrice = BigDecimal.valueOf(500);
        } else {
            roomPrice = roomType.getBasePrice();
        }

        // 计算奖励
        BigDecimal reward = qLearningService.calculateReward(
                decision.getActionChosen(),
                cancellations,
                noShows,
                roomPrice,
                new BigDecimal("1.5")
        );
        decision.setReward(reward);

        overbookingDecisionRepository.save(decision);

        // 更新Q值
        String nextStateKey = qLearningService.getRecommendation(
                decision.getHotelId(),
                decision.getRoomTypeId(),
                decision.getDecisionDate().plusDays(1)
        ).getStateKey();

        qLearningService.updateQValue(
                decision.getHotelId(),
                decision.getRoomTypeId(),
                decision.getStateKey(),
                decision.getActionChosen(),
                reward,
                nextStateKey
        );

        log.info("记录决策结果: decisionId={}, 成功={}, 奖励={}", decisionId, wasSuccessful, reward);
    }

    /**
     * 获取决策历史
     */
    public List<OverbookingDecision> getDecisionHistory(Long hotelId, LocalDate startDate, LocalDate endDate) {
        return overbookingDecisionRepository.findByHotelIdAndDecisionDateBetween(
                hotelId, startDate, endDate
        );
    }

    /**
     * 获取性能统计
     */
    public PerformanceStats getPerformanceStats(Long hotelId, LocalDate startDate, LocalDate endDate) {
        BigDecimal totalReward = overbookingDecisionRepository.sumRewardByDateRange(hotelId, startDate, endDate);
        if (totalReward == null) {
            totalReward = BigDecimal.ZERO;
        }

        Long successCount = overbookingDecisionRepository.countBySuccessAndDateRange(hotelId, startDate, endDate, true);
        Long failureCount = overbookingDecisionRepository.countBySuccessAndDateRange(hotelId, startDate, endDate, false);

        long total = (successCount != null ? successCount : 0) + (failureCount != null ? failureCount : 0);
        double successRate = total > 0
                ? (double) (successCount != null ? successCount : 0) / total * 100
                : 0;

        return PerformanceStats.builder()
                .totalReward(totalReward)
                .successCount(successCount != null ? successCount : 0)
                .failureCount(failureCount != null ? failureCount : 0)
                .successRate(BigDecimal.valueOf(successRate).setScale(2, RoundingMode.HALF_UP))
                .totalDecisions(total)
                .build();
    }

    /**
     * 性能统计 DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class PerformanceStats {
        private BigDecimal totalReward;
        private Long successCount;
        private Long failureCount;
        private BigDecimal successRate;
        private Long totalDecisions;
    }
}
