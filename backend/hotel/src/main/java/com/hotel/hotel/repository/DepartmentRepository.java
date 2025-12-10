package com.hotel.hotel.repository;

import com.hotel.hotel.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /**
     * 根据部门名称查询部门信息 (用于归因查找)
     */
    Optional<Department> findByDeptName(String deptName);
}