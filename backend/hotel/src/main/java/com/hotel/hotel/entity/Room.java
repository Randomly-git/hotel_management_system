package com.hotel.hotel.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "room")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "room_num")
    private Integer roomNum;

    @Column(name = "status")
    private Integer status; // 1-正常 2-停用

    // 关联到房型
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private RoomType roomType;

    // --- 以下是为兼容老系统逻辑新增的字段 ---

    /**
     * @Transient 的意思是：这个字段仅存在于 Java 对象中，
     * JPA 不会尝试去数据库表里找对应的列，也不会去创建它。
     */
    @Transient
    private List<String> notUseDateList; // 已被占用的日期列表 (yyyy-MM-dd)

    @Transient
    private Boolean canUse; // 今天是否可用
}