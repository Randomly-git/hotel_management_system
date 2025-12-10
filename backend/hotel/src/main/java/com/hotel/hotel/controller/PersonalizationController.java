package com.hotel.hotel.controller;

import com.hotel.hotel.dto.FeedbackRequest; // 复用 FeedbackRequest 结构接收请求
import com.hotel.hotel.entity.TaskOrder;
import com.hotel.hotel.service.TaskService;
import com.hotel.hotel.service.GuestProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/personalization")
public class PersonalizationController {

    private final TaskService taskService;
    private final GuestProfileService profileService;

    @Autowired
    public PersonalizationController(TaskService taskService, GuestProfileService profileService) {
        this.taskService = taskService;
        this.profileService = profileService;
    }

    // DTO 用于接收预测模拟请求
    public static class PredictionRequest {
        @Valid
        private String memberId;
        private String simulatedPrediction; // 模拟AI预测结果
        private String attributedDept; // 模拟AI归因部门

        // 省略 getter/setter/构造器，假设已使用 Lombok @Data
    }


    /**
     * [POST] 模拟AI预测并自动生成任务单接口
     * 接口路径: /api/v1/personalization/predict
     * * 实际应用中，该接口可能由【入住登记系统】在客户Check-in时自动调用。
     */
    @PostMapping("/predict")
    public ResponseEntity<Map<String, Object>> triggerAIPrediction(@RequestBody PredictionRequest request) {

        // 1. 获取客户画像特征 (模拟：实际中可能更复杂)
        String features = profileService.extractFeaturesForPrediction(request.memberId);

        // 2. 模拟 AI 模型输出结果 (这里使用请求体传入的模拟值)
        String predictedNeed = request.simulatedPrediction != null ? request.simulatedPrediction : "高楼层安静房间";
        String attributedDept = request.attributedDept != null ? request.attributedDept : "房务部";
        int dueMinutes = 60; // 预测任务给 60 分钟准备时间

        // 3. 调用 TaskService 生成任务单
        TaskOrder task = taskService.generateTaskFromPrediction(
                request.memberId,
                predictedNeed,
                attributedDept,
                dueMinutes
        );

        // 4. 返回响应
        Map<String, Object> response = new HashMap<>();
        response.put("message", "AI预测任务单生成成功");
        response.put("taskId", task.getTaskId());
        response.put("assignedTo", task.getAssignedDepartment().getDeptName());
        response.put("predictionBasedOn", features);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * [POST] 接收客户主动请求并生成任务单接口
     * 接口路径: /api/v1/personalization/request
     * * 用于接收来自客户APP/小程序的请求，快速转化为内部任务。
     */
    @PostMapping("/request")
    public ResponseEntity<Map<String, Object>> submitCustomerRequest(@Valid @RequestBody FeedbackRequest request) {
        // 注：这里复用了 FeedbackRequest 作为请求 DTO，实际应创建 RequestTaskRequest DTO

        // 1. 模拟一个简单的请求归因逻辑 (实际可通过 NLP 或业务规则)
        String deptName = "服务部"; // 默认归因给服务部处理
        if (request.getFeedbackContent().contains("房间")) {
            deptName = "房务部";
        }

        // 2. 调用 TaskService 生成请求任务单
        TaskOrder task = taskService.generateTaskFromRequest(
                request.getCustomerName(), // 客户名作为 MemberId
                request.getFeedbackContent(),
                deptName
        );

        // 3. 返回响应
        Map<String, Object> response = new HashMap<>();
        response.put("message", "客户请求已接收并生成任务单。");
        response.put("taskId", task.getTaskId());
        response.put("assignedTo", task.getAssignedDepartment().getDeptName());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * [GET] 查询所有待处理的任务单列表（供各部门工作台展示）
     * 接口路径: /api/v1/personalization/tasks/pending
     */
    @GetMapping("/tasks/pending")
    public ResponseEntity<List<TaskOrder>> getPendingTasks() {
        List<TaskOrder> tasks = taskService.getPendingTasks();

        if (tasks.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(tasks);
    }

    /**
     * [PUT] 更新任务单状态接口（供各部门工作台操作）
     * 接口路径: /api/v1/personalization/tasks/{taskId}/status
     */
    @PutMapping("/tasks/{taskId}/status")
    public ResponseEntity<TaskOrder> updateTaskStatus(@PathVariable Long taskId, @RequestParam String status) {
        try {
            TaskOrder updatedTask = taskService.updateTaskStatus(taskId, status.toUpperCase());
            return ResponseEntity.ok(updatedTask);
        } catch (RuntimeException e) {
            // 捕获任务不存在或状态无效的异常
            return ResponseEntity.notFound().build();
        }
    }
}