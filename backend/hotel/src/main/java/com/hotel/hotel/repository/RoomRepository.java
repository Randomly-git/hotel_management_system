package com.hotel.hotel.repository;

import com.hotel.hotel.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {
    // 对应老数据库中的 category_id 字段查询
    List<Room> findByRoomType_TypeId(Integer typeId);

    // 用于校验房间号是否重复
    boolean existsByRoomNum(Integer roomNum);
    long countByRoomType_TypeId(Integer typeId);
}