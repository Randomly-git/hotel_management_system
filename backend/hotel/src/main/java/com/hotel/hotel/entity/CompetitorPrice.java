package com.hotel.hotel.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "competitor_price")
public class CompetitorPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String competitorName; // 竞品酒店名称，如 "精品酒店A"

    @Column(nullable = false)
    private String roomTypeName; // 竞品房型名称

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // 抓取到的价格

    @Column(nullable = false)
    private LocalDate stayDate; // 该价格对应的入住日期

    @Column(nullable = false)
    private LocalDateTime crawlTime; // 抓取/模拟生成的时间

    @PrePersist
    protected void onCreate() {
        this.crawlTime = LocalDateTime.now();
    }
}