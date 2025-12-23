package com.hotel.hotel.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "category") // 关键：指向老数据库的房型表
@NoArgsConstructor
@AllArgsConstructor
public class RoomType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // 老数据库的主键叫 id
    private Integer typeId;

    @Column(name = "category_name", nullable = false, length = 50) // 对齐列名
    private String typeName;

    @Column(name = "price", nullable = false, precision = 10, scale = 2) // 对齐列名
    private BigDecimal basePrice;

    // 老数据库 category 表里没有 totalCount。
    // 我们将其标记为 @Transient，意味着它不属于数据库表，由我们逻辑计算得出
    @Transient
    private Integer totalCount;
}