package com.hotel.hotel.repository;

import com.hotel.hotel.entity.PricingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PricingRecordRepository extends JpaRepository<PricingRecord, Long> {

    /**
     * [重要] 查询某个房型在某个生效日期的最新调价记录
     * 用于幂等性检查，防止同一天为同一房型重复生成建议。
     */
    Optional<PricingRecord> findTopByRoomTypeTypeIdAndEffectiveDateOrderByAdjustTimeDesc(
            Integer roomTypeId,
            LocalDate effectiveDate
    );

    /**
     * [新增] 用于店长工作台：按状态查询调价记录
     * 例如：status = 'PENDING' (待审批) 或 'APPLIED' (已应用)
     */
    List<PricingRecord> findByStatus(String status);

    /**
     * [新增] 用于前台/预订系统：查询指定日期且【已生效】的价格记录
     * 只有状态为 APPLIED 的记录才会被提取展示。
     */
    List<PricingRecord> findByEffectiveDateAndStatus(LocalDate effectiveDate, String status);

    /**
     * 查询某个房型在某个生效日期前的最新价格记录
     * 常用于计算调价前的“原始价格”对比。
     */
    Optional<PricingRecord> findTopByRoomTypeTypeIdAndEffectiveDateBeforeOrderByAdjustTimeDesc(
            Integer roomTypeId,
            LocalDate effectiveDate
    );

    /**
     * [新增] 按日期范围查询已生效的价格（常用于价格走势图）
     */
    List<PricingRecord> findByEffectiveDateBetweenAndStatus(
            LocalDate startDate,
            LocalDate endDate,
            String status
    );
}