package com.hotel.hotel.repository;

import com.hotel.hotel.entity.OverbookingDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OverbookingDecisionRepository extends JpaRepository<OverbookingDecision, Long> {

    /**
     * 查询指定日期范围的决策记录
     */
    List<OverbookingDecision> findByHotelIdAndDecisionDateBetween(
        Long hotelId,
        LocalDate startDate,
        LocalDate endDate
    );

    /**
     * 统计总收益
     */
    @Query("SELECT SUM(d.reward) FROM OverbookingDecision d WHERE d.hotelId = :hotelId AND d.decisionDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal sumRewardByDateRange(
        @Param("hotelId") Long hotelId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * 统计成功和失败次数
     */
    @Query("SELECT COUNT(d) FROM OverbookingDecision d WHERE d.hotelId = :hotelId AND d.decisionDate BETWEEN :startDate AND :endDate AND d.wasSuccessful = :success")
    Long countBySuccessAndDateRange(
        @Param("hotelId") Long hotelId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("success") Boolean success
    );
}
