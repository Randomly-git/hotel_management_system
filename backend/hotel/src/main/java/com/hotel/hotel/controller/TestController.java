package com.hotel.hotel.controller;

import com.hotel.hotel.entity.Department;
import com.hotel.hotel.entity.TaskOrder;
import com.hotel.hotel.repository.DepartmentRepository;
import com.hotel.hotel.repository.TaskOrderRepository;
import com.hotel.hotel.common.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.hotel.hotel.service.CsvImportService;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

/**
 * 测试控制器 - 用于调试问题
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TaskOrderRepository taskOrderRepository;

    @Autowired
    private CsvImportService csvImportService;

    @GetMapping("/import-data")
    public String triggerImport(@RequestParam String fileName) {
        // 异步或同步执行导入。建议在开发环境下直接同步执行以便观察控制台
        try {
            csvImportService.importHotelData(fileName);
            return "导入任务已执行完毕，请检查控制台日志！";
        } catch (Exception e) {
            return "触发失败: " + e.getMessage();
        }
    }


    /**
     * 测试部门查询
     */
    @GetMapping("/departments")
    public Response<List<Department>> testDepartments() {
        try {
            log.info("开始查询所有部门...");
            List<Department> departments = departmentRepository.findAll();
            log.info("查询到 {} 个部门", departments.size());

            for (Department dept : departments) {
                log.info("部门详情: ID={}, 名称={}", dept.getDeptId(), dept.getDeptName());
            }

            return Response.success(departments);
        } catch (Exception e) {
            log.error("查询部门失败", e);
            return Response.error("查询部门失败: " + e.getMessage());
        }
    }

    /**
     * 测试创建工程部
     */
    @PostMapping("/create-engineering")
    public Response<Department> createEngineeringDepartment() {
        try {
            log.info("开始创建工程部门...");

            // 先检查是否已存在
            if (departmentRepository.findByDeptName("工程部").isPresent()) {
                log.info("工程部门已存在");
                return Response.success(departmentRepository.findByDeptName("工程部").get());
            }

            Department engineering = new Department();
            engineering.setDeptName("工程部");
            engineering.setWeight(java.math.BigDecimal.valueOf(0.10));

            Department saved = departmentRepository.save(engineering);
            log.info("工程部门创建成功: ID={}", saved.getDeptId());

            return Response.success(saved);
        } catch (Exception e) {
            log.error("创建工程部门失败", e);
            return Response.error("创建工程部门失败: " + e.getMessage());
        }
    }

    /**
     * 测试创建简单任务
     */
    @PostMapping("/create-simple-task")
    public Response<TaskOrder> createSimpleTask() {
        try {
            log.info("开始创建简单测试任务...");

            // 查找工程部门
            Department engineering = departmentRepository.findByDeptName("工程部")
                .orElseThrow(() -> new RuntimeException("工程部门不存在"));

            log.info("找到工程部门: ID={}", engineering.getDeptId());

            TaskOrder task = new TaskOrder();
            task.setHotelId(1L);
            task.setGuestMemberId("TEST001");
            task.setAssignedDepartment(engineering);
            task.setTaskType("REQUEST");
            task.setTaskContent("测试任务：空调维修");
            task.setStatus("PENDING");
            task.setRoomNumber("801");

            log.info("任务对象创建完成，准备保存...");

            TaskOrder saved = taskOrderRepository.save(task);
            log.info("任务保存成功: ID={}", saved.getTaskId());

            return Response.success(saved);
        } catch (Exception e) {
            log.error("创建测试任务失败", e);
            return Response.error("创建测试任务失败: " + e.getMessage());
        }
    }

    /**
     * 测试查询任务
     */
    @GetMapping("/tasks")
    public Response<List<TaskOrder>> testTasks() {
        try {
            log.info("开始查询所有任务...");
            List<TaskOrder> tasks = taskOrderRepository.findAll();
            log.info("查询到 {} 个任务", tasks.size());

            for (TaskOrder task : tasks) {
                log.info("任务详情: ID={}, 客户={}, 部门={}, 状态={}, 部门ID={}",
                    task.getTaskId(),
                    task.getGuestMemberId(),
                    task.getAssignedDepartment() != null ? task.getAssignedDepartment().getDeptName() : "null",
                    task.getStatus(),
                    task.getAssignedDepartment() != null ? task.getAssignedDepartment().getDeptId() : "null");
            }

            return Response.success(tasks);
        } catch (Exception e) {
            log.error("查询任务失败", e);
            return Response.error("查询任务失败: " + e.getMessage());
        }
    }
}