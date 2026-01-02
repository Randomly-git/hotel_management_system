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
     * 根据房型ID（roomType.id）和生效日期查找最新的一条调价建议
     * 方法名中的 RoomType_Id 对应实体中的关联对象 roomType 及其主键 id
     */
    Optional<PricingRecord> findTopByRoomType_IdAndEffectiveDateOrderByAdjustTimeDesc(
            Long roomTypeId, LocalDate effectiveDate);

    /**
     * 根据生效日期和状态查询记录（用于降级逻辑或展示）
     */
    List<PricingRecord> findByEffectiveDateAndStatus(LocalDate date, String status);

    /**
     * 根据状态查询（用于审批列表展示）
     */
    List<PricingRecord> findByStatus(String status);
}