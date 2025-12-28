package com.hotel.hotel.repository;

import com.hotel.hotel.entity.QLearningState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QLearningStateRepository extends JpaRepository<QLearningState, Long> {

    /**
     * 根据状态键查询
     */
    Optional<QLearningState> findByHotelIdAndRoomTypeIdAndStateKey(Long hotelId, Long roomTypeId, String stateKey);

    /**
     * 获取酒店和房型所有状态
     */
    List<QLearningState> findByHotelIdAndRoomTypeId(Long hotelId, Long roomTypeId);

    /**
     * 获取所有状态数量
     */
    @Query("SELECT COUNT(s) FROM QLearningState s WHERE s.hotelId = :hotelId AND s.roomTypeId = :roomTypeId")
    Long countByHotelIdAndRoomTypeId(@Param("hotelId") Long hotelId, @Param("roomTypeId") Long roomTypeId);
}
