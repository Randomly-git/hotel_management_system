package com.hotel.hotel.repository;

import com.hotel.hotel.entity.PricingRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PricingRecordRepository extends JpaRepository<PricingRecord, Long> {
    // 查询某个房型在某个生效日期前的最新价格记录
    Optional<PricingRecord> findTopByRoomTypeTypeIdAndEffectiveDateBeforeOrderByAdjustTimeDesc(
            Integer roomTypeId,
            LocalDate effectiveDate
    );

    // 查询某个房型某个生效日期的最新价格 (可能用于检查是否已调整)
    Optional<PricingRecord> findTopByRoomTypeTypeIdAndEffectiveDateOrderByAdjustTimeDesc(
            Integer roomTypeId,
            LocalDate effectiveDate
    );

    // 查询指定生效日期和状态的价格记录
    List<PricingRecord> findByEffectiveDateAndStatus(LocalDate effectiveDate, String status);
}
