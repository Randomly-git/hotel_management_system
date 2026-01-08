package com.hotel.hotel.repository;

import com.hotel.hotel.entity.PricingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.hotel.hotel.entity.HotelRoomType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying; // 新增引入
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

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
    /**
     * 新增：用于精准查找某房型在某日期还未审批的记录
     * 对应逻辑：RoomType(属性) -> Id(HotelRoomType内部的ID)
     */
    Optional<PricingRecord> findTopByRoomType_IdAndEffectiveDateAndStatusOrderByAdjustTimeDesc(
            Long roomTypeId, LocalDate effectiveDate, String status);

    /**
     * 安全批量删除：删除指定房型下所有处于特定状态（如 PENDING）的记录
     * 使用 @Modifying 告知 JPA 这是一个删除操作
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PricingRecord p WHERE p.roomType = :roomType AND p.status = :status")
    void deleteByRoomTypeAndStatus(HotelRoomType roomType, String status);


}