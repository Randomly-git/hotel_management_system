package com.hotel.hotel.controller;

import com.hotel.hotel.common.Response;
import com.hotel.hotel.entity.Department;
import com.hotel.hotel.entity.TaskOrder;
import com.hotel.hotel.repository.DepartmentRepository;
import com.hotel.hotel.repository.TaskOrderRepository;
import com.hotel.hotel.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 简单测试控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/simple")
public class SimpleTestController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TaskOrderRepository taskOrderRepository;

    @PostMapping("/test-request")
    public Response<String> testRequest(@RequestBody Map<String, Object> requestData) {
        try {
            log.info("收到测试请求: {}", requestData);

            String customerId = (String) requestData.get("customerId");
            String content = (String) requestData.get("requestContent");
            String roomNumber = (String) requestData.get("roomNumber");
            Long hotelId = Long.valueOf(requestData.get("hotelId").toString());

            log.info("解析参数: customerId={}, content={}, roomNumber={}, hotelId={}",
                    customerId, content, roomNumber, hotelId);

            // 直接调用TaskService
            TaskOrder task = taskService.generateTaskFromRequest(
                customerId, content, "工程部", hotelId, roomNumber, null
            );

            log.info("任务创建成功: taskId={}", task.getTaskId());

            return Response.success("任务创建成功，ID: " + task.getTaskId());

        } catch (Exception e) {
            log.error("测试请求失败", e);
            return Response.error("测试失败: " + e.getMessage());
        }
    }

    @PostMapping("/check-departments")
    public Response<String> checkDepartments() {
        try {
            long count = departmentRepository.count();
            log.info("部门总数: {}", count);

            departmentRepository.findAll().forEach(dept -> {
                log.info("部门: ID={}, 名称={}", dept.getDeptId(), dept.getDeptName());
            });

            return Response.success("存在 " + count + " 个部门");

        } catch (Exception e) {
            log.error("检查部门失败", e);
            return Response.error("检查失败: " + e.getMessage());
        }
    }
}