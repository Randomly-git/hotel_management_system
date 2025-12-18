package com.hotel.hotel.controller;

import com.hotel.hotel.entity.Department;
import com.hotel.hotel.entity.TaskOrder;
import com.hotel.hotel.repository.DepartmentRepository;
import com.hotel.hotel.repository.TaskOrderRepository;
import com.hotel.hotel.common.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 诊断控制器 - 用于排查系统问题
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/diagnostic")
public class DiagnosticController {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TaskOrderRepository taskOrderRepository;

    /**
     * 检查数据库状态
     */
    @GetMapping("/database-status")
    public Response<Map<String, Object>> checkDatabaseStatus() {
        Map<String, Object> status = new HashMap<>();

        try {
            // 检查部门表
            List<Department> departments = departmentRepository.findAll();
            status.put("departments", departments);
            status.put("departmentCount", departments.size());

            log.info("数据库中的部门数量: {}", departments.size());
            for (Department dept : departments) {
                log.info("部门: ID={}, 名称={}", dept.getDeptId(), dept.getDeptName());
            }

            // 检查任务表
            List<TaskOrder> tasks = taskOrderRepository.findAll();
            status.put("tasks", tasks);
            status.put("taskCount", tasks.size());

            log.info("数据库中的任务数量: {}", tasks.size());
            for (TaskOrder task : tasks) {
                log.info("任务: ID={}, 客户={}, 部门={}, 状态={}",
                    task.getTaskId(), task.getGuestMemberId(),
                    task.getAssignedDepartment() != null ? task.getAssignedDepartment().getDeptName() : "null",
                    task.getStatus());
            }

            status.put("status", "success");
            return Response.success("数据库状态检查完成", status);

        } catch (Exception e) {
            log.error("检查数据库状态失败", e);
            status.put("status", "error");
            status.put("error", e.getMessage());
            return Response.error("数据库状态检查失败: " + e.getMessage());
        }
    }

    /**
     * 创建测试部门
     */
    @GetMapping("/create-test-departments")
    public Response<String> createTestDepartments() {
        try {
            // 删除现有部门
            departmentRepository.deleteAll();

            // 创建基础部门
            Department housekeeping = new Department();
            housekeeping.setDeptName("房务部");
            housekeeping.setWeight(new java.math.BigDecimal("0.40"));

            Department service = new Department();
            service.setDeptName("服务部");
            service.setWeight(new java.math.BigDecimal("0.30"));

            Department dining = new Department();
            dining.setDeptName("餐饮部");
            dining.setWeight(new java.math.BigDecimal("0.20"));

            Department engineering = new Department();
            engineering.setDeptName("工程部");
            engineering.setWeight(new java.math.BigDecimal("0.10"));

            Department business = new Department();
            business.setDeptName("业务部");
            business.setWeight(new java.math.BigDecimal("0.00"));

            departmentRepository.saveAll(List.of(housekeeping, service, dining, engineering, business));

            return Response.success("测试部门创建成功");

        } catch (Exception e) {
            log.error("创建测试部门失败", e);
            return Response.error("创建测试部门失败: " + e.getMessage());
        }
    }

    /**
     * 创建测试任务
     */
    @GetMapping("/create-test-task")
    public Response<String> createTestTask() {
        try {
            // 确保有工程部门
            Department engineering = departmentRepository.findByDeptName("工程部")
                .orElseThrow(() -> new RuntimeException("工程部门不存在"));

            TaskOrder testTask = new TaskOrder();
            testTask.setHotelId(1L);
            testTask.setGuestMemberId("TEST001");
            testTask.setAssignedDepartment(engineering);
            testTask.setTaskType("REQUEST");
            testTask.setTaskContent("测试任务：空调维修");
            testTask.setStatus("PENDING");
            testTask.setRoomNumber("801");
            testTask.setCreateTime(java.time.LocalDateTime.now());
            testTask.setDueTime(java.time.LocalDateTime.now().plusHours(2));

            TaskOrder saved = taskOrderRepository.save(testTask);

            return Response.success("测试任务创建成功，任务ID: " + saved.getTaskId());

        } catch (Exception e) {
            log.error("创建测试任务失败", e);
            return Response.error("创建测试任务失败: " + e.getMessage());
        }
    }
}