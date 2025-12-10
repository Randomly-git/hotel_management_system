package com.hotel.hotel.repository;

import com.hotel.hotel.entity.TaskOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskOrderRepository extends JpaRepository<TaskOrder, Long> {
    List<TaskOrder> findByStatusOrderByCreateTimeDesc(String status);
}
