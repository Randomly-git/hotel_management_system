package com.hotel.hotel.repository;

import com.hotel.hotel.entity.PricingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PricingRecordRepository extends JpaRepository<PricingRecord, Long> {

    // --- 原有查询逻辑（保持不动） ---

    // 查询某个房型在某个生效日期前的最新价格记录
    Optional<PricingRecord> findTopByRoomTypeTypeIdAndEffectiveDateBeforeOrderByAdjustTimeDesc(
            Integer roomTypeId,
            LocalDate effectiveDate
    );

    // 查询某个房型某个生效日期的最新价格
    Optional<PricingRecord> findTopByRoomTypeTypeIdAndEffectiveDateOrderByAdjustTimeDesc(
            Integer roomTypeId,
            LocalDate effectiveDate
    );

    // --- 新增查询逻辑：支持 F3 动态定价任务 ---

    /**
     * 根据生效日期和状态查询所有记录
     * 用于：GET /api/v1/pricing/current (仅查询已通过 APPROVED 的价格)
     * 用于：GET /api/v1/pricing/review  (仅查询待审核 PENDING 的价格)
     */
    List<PricingRecord> findByEffectiveDateAndStatus(LocalDate effectiveDate, String status);

    /**
     * 根据状态查询所有记录
     * 用于：管理员查看所有待处理的审批申请
     */
    List<PricingRecord> findByStatus(String status);

    /**
     * 批量查询某个时间段内所有已通过的价格
     * 用于：生成价格趋势图或导出报表
     */
    List<PricingRecord> findByEffectiveDateBetweenAndStatus(LocalDate startDate, LocalDate endDate, String status);
}