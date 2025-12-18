package com.hotel.hotel.repository;

import com.hotel.hotel.entity.CompetitorPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CompetitorPriceRepository extends JpaRepository<CompetitorPrice, Long> {

    // 查询某个日期范围内，所有竞品的价格记录
    List<CompetitorPrice> findByStayDateBetween(LocalDate startDate, LocalDate endDate);

    // 查询指定日期所有竞品的价格（用于计算当日市场平均价）
    List<CompetitorPrice> findByStayDate(LocalDate stayDate);
}