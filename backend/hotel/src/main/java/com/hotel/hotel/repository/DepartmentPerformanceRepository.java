package com.hotel.hotel.repository;

import com.hotel.hotel.entity.DepartmentPerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentPerformanceRepository extends JpaRepository<DepartmentPerformance, Long> {

    /**
     * 查询某个日期范围内的所有部门绩效记录 (用于趋势分析)
     */
    List<DepartmentPerformance> findByStatisticsDateBetweenOrderByStatisticsDateAsc(
            LocalDate startDate,
            LocalDate endDate
    );

    /**
     * 查询某个部门的最新绩效记录 (用于预警判断)
     */
    Optional<DepartmentPerformance> findTopByDepartmentDeptIdOrderByStatisticsDateDesc(Long deptId);

    Optional<DepartmentPerformance> findByDepartmentDeptIdAndStatisticsDate(Long deptId, LocalDate date);

    /**
     * 租户级UPSERT：查询指定酒店、部门在特定日期的记录
     */
    Optional<DepartmentPerformance> findByHotelIdAndDepartmentDeptIdAndStatisticsDate(
            String hotelId, Long deptId, LocalDate date);

    /**
     * 趋势刻画：查询指定日期之前的最后一次绩效记录，用于计算 ScoreIndex 的升降
     */
    Optional<DepartmentPerformance> findTopByHotelIdAndDepartmentDeptIdAndStatisticsDateBeforeOrderByStatisticsDateDesc(
            String hotelId, Long deptId, LocalDate date);
}