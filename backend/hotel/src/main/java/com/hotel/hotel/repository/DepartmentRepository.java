package com.hotel.hotel.repository;

import com.hotel.hotel.entity.CustomerFeedback;
import com.hotel.hotel.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /**
     * 根据部门名称查询部门信息 (用于归因查找)
     */
    Optional<Department> findByDeptName(String deptName);

    /**
     * 获取指定酒店的所有部门列表（用于每日绩效轮询计算）
     */
    List<Department> findByHotelId(String hotelId);

    /**
     * 精确查找指定酒店下的部门
     */
    Optional<Department> findByHotelIdAndDeptName(String hotelId, String deptName);
}