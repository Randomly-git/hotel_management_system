package com.hotel.hotel.controller;

import com.hotel.hotel.common.Response;
import com.hotel.hotel.entity.TaskOrder;
import com.hotel.hotel.service.TaskService;
import com.hotel.hotel.service.GuestProfileService;
import com.hotel.hotel.service.ZhipuNlpService;
import com.hotel.hotel.service.TaskDistributionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 个性化服务系统控制器
 * 负责处理客户的个性化需求解析和任务生成
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/personalization")
@CrossOrigin(origins = "*")
@Tag(name = "个性化服务系统", description = "处理客户自然语言需求，自动生成任务单")
public class PersonalizationController {

    private final TaskService taskService;
    private final GuestProfileService profileService;
    private final ZhipuNlpService zhipuNlpService;
    private final TaskDistributionService taskDistributionService;

    @Autowired
    public PersonalizationController(TaskService taskService,
                                   GuestProfileService profileService,
                                   ZhipuNlpService zhipuNlpService,
                                   TaskDistributionService taskDistributionService) {
        this.taskService = taskService;
        this.profileService = profileService;
        this.zhipuNlpService = zhipuNlpService;
        this.taskDistributionService = taskDistributionService;
    }

    /**
     * DTO 用于接收预测模拟请求
     */
    @Data
    @Schema(description = "AI预测请求")
    public static class PredictionRequest {
        @Parameter(description = "客户会员ID", required = true)
        private String memberId;

        @Parameter(description = "模拟AI预测结果")
        private String simulatedPrediction;

        @Parameter(description = "模拟AI归因部门")
        private String attributedDept;
    }

    /**
     * DTO 用于接收客户请求
     */
    @Data
    @Schema(description = "客户请求")
    public static class CustomerRequest {
        @Parameter(description = "客户ID或会员号", required = true)
        private String customerId;

        @Parameter(description = "客户姓名")
        private String customerName;

        @Parameter(description = "自然语言请求内容", required = true)
        private String content;

        @Parameter(description = "房间号")
        private String roomNumber;

        @Parameter(description = "期望解决时间（ISO格式，如：2025-12-17T15:30:00）")
        private String dueTime;

        @Parameter(description = "酒店ID（租户隔离）")
        private Long hotelId = 1L;  // 默认值为1
    }

    /**
     * [POST] 模拟AI预测并自动生成任务单接口
     * 实际应用中，该接口可能由【入住登记系统】在客户Check-in时自动调用
     */
    @Operation(summary = "AI预测生成任务", description = "基于客户画像预测需求并生成任务单")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "任务单生成成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "404", description = "客户不存在")
    })
    @PostMapping("/predict")
    public Mono<ResponseEntity<Response<Map<String, Object>>>> triggerAIPrediction(
            @Valid @RequestBody PredictionRequest request) {

        log.info("收到AI预测请求: memberId={}", request.getMemberId());

        // 1. 获取客户画像特征
        String features;
        try {
            features = profileService.extractFeaturesForPrediction(request.getMemberId());
        } catch (Exception e) {
            log.error("获取客户画像失败: memberId={}", request.getMemberId(), e);
            return Mono.just(ResponseEntity.badRequest()
                .body(Response.error("获取客户画像失败: " + e.getMessage())));
        }

        // 2. 使用模拟值或请求传入的值
        String predictedNeed = request.getSimulatedPrediction() != null ?
            request.getSimulatedPrediction() : "高楼层安静房间";
        String attributedDept = request.getAttributedDept() != null ?
            request.getAttributedDept() : "房务部";
        int dueMinutes = 60;

        // 3. 生成任务单
        try {
            TaskOrder task = taskService.generateTaskFromPrediction(
                request.getMemberId(),
                predictedNeed,
                attributedDept,
                dueMinutes,
                1L,  // 默认酒店ID
                null  // 房间号，预测时可能未知
            );

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("taskId", task.getTaskId());
            responseData.put("assignedTo", task.getAssignedDepartment().getDeptName());
            responseData.put("predictionBasedOn", features);
            responseData.put("predictedNeed", predictedNeed);

            return Mono.just(ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.success("AI预测任务单生成成功", responseData)));

        } catch (Exception e) {
            log.error("生成预测任务失败", e);
            return Mono.just(ResponseEntity.internalServerError()
                .body(Response.error("生成任务失败: " + e.getMessage())));
        }
    }

    /**
     * [POST] 接收客户主动请求并解析生成任务单
     * 使用智谱AI进行自然语言解析
     */
    @Operation(summary = "客户请求处理", description = "接收客户自然语言请求，使用AI解析后生成任务单")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "任务单生成成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "503", description = "AI服务暂时不可用")
    })
    @PostMapping(value = "/request", consumes = "application/json; charset=UTF-8")
    public Mono<ResponseEntity<Response<Map<String, Object>>>> submitCustomerRequest(
            @Valid @RequestBody CustomerRequest request) {

        log.info("收到客户请求: customerId={}, content={}, hotelId={}",
                request.getCustomerId(), request.getContent(), request.getHotelId());

        // 验证请求内容不为空
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            return Mono.just(ResponseEntity.badRequest()
                .body(Response.error("请求内容不能为空")));
        }

        // 验证hotelId有效性
        if (request.getHotelId() == null || request.getHotelId() <= 0) {
            return Mono.just(ResponseEntity.badRequest()
                .body(Response.error("无效的酒店ID")));
        }

        // 1. 使用智谱AI解析请求
        return zhipuNlpService.parseCustomerRequest(request.getContent())
            .flatMap(nlpResult -> {
                // 2. 根据解析结果生成任务单
                try {
                    log.info("NLP解析结果: intent={}, description={}, dept={}",
                        nlpResult.getIntent(), nlpResult.getDescription(), nlpResult.getRecommendedDepartment());

                    // 检查是否需要人工确认
                    if ("UNKNOWN".equals(nlpResult.getIntent())) {
                        log.warn("AI无法识别请求，分配给业务部处理");
                        // 对于UNKNOWN意图，仍然创建任务但分配给业务部
                        nlpResult.setRecommendedDepartment("业务部");
                        nlpResult.setIntent("GENERAL"); // 设置为通用意图
                    }

                    // 详细的任务生成参数日志
                    log.info("开始生成任务单 - 参数: customerId={}, description={}, dept={}, hotelId={}, roomNumber={}, dueTime={}",
                        request.getCustomerId(),
                        nlpResult.getDescription(),
                        nlpResult.getRecommendedDepartment(),
                        request.getHotelId(),
                        request.getRoomNumber(),
                        request.getDueTime());

                    // 检查TaskService是否为null
                    if (taskService == null) {
                        log.error("TaskService注入失败！");
                        throw new RuntimeException("TaskService未正确注入");
                    }

                    log.info("TaskService准备调用generateTaskFromRequest方法...");

                    TaskOrder task = taskService.generateTaskFromNlpResult(
                        request.getCustomerId(),
                        nlpResult.getDescription(),
                        nlpResult.getRecommendedDepartment(),
                        request.getHotelId(), // 从请求中获取酒店ID
                        request.getRoomNumber(),
                        request.getDueTime(), // 传递客户指定的期望解决时间
                        nlpResult.getUrgency() // 使用NLP解析的优先级
                    );

                    log.info("TaskService调用完成，生成的任务ID: {}, 任务状态: {}",
                        task.getTaskId(), task.getStatus());

                    // 验证任务是否正确保存
                    if (task.getTaskId() == null) {
                        log.error("任务生成失败：任务ID为null");
                        throw new RuntimeException("任务生成失败：任务ID为null");
                    }

                    // 调用任务分发服务
                    try {
                        taskDistributionService.distributeTaskToDepartment(task);
                        log.info("任务分发完成: taskId={}", task.getTaskId());
                    } catch (Exception e) {
                        log.warn("任务分发失败，但不影响主流程: taskId={}, error={}", task.getTaskId(), e.getMessage());
                    }

                    // 不覆盖任务内容，保持原有的"客户请求: ..."格式
                    // task.setTaskContent(nlpResult.toString());

                    Map<String, Object> responseData = new HashMap<>();
                    responseData.put("taskId", task.getTaskId());
                    responseData.put("assignedTo", task.getAssignedDepartment().getDeptName());
                    responseData.put("nlpAnalysis", nlpResult);

                    log.info("客户请求处理完成，返回成功响应");
                    return Mono.just(ResponseEntity.status(HttpStatus.CREATED)
                        .body(Response.success("客户请求已处理", responseData)));

                } catch (IllegalArgumentException e) {
                    log.error("参数错误：{}", e.getMessage(), e);
                    return Mono.just(ResponseEntity.badRequest()
                        .body(Response.<Map<String, Object>>error("参数错误: " + e.getMessage())));
                } catch (RuntimeException e) {
                    log.error("运行时错误：{}", e.getMessage(), e);
                    return Mono.just(ResponseEntity.internalServerError()
                        .body(Response.<Map<String, Object>>error("处理请求时发生错误: " + e.getMessage())));
                } catch (Exception e) {
                    log.error("未知错误：{}", e.getMessage(), e);
                    return Mono.just(ResponseEntity.internalServerError()
                        .body(Response.<Map<String, Object>>error("系统错误: " + e.getMessage())));
                }
            })
            .onErrorReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Response.<Map<String, Object>>error("AI服务暂时不可用，请稍后重试")));
    }

    /**
     * [GET] 查询所有待处理的任务单列表
     */
    @Operation(summary = "查询待处理任务", description = "获取所有状态为PENDING的任务单")
    @GetMapping("/tasks/pending")
    public ResponseEntity<Response<List<TaskOrder>>> getPendingTasks() {
        try {
            List<TaskOrder> tasks = taskService.getPendingTasks();

            if (tasks.isEmpty()) {
                return ResponseEntity.ok(Response.success("暂无待处理任务", tasks));
            }

            return ResponseEntity.ok(Response.success(tasks));
        } catch (Exception e) {
            log.error("查询待处理任务失败", e);
            return ResponseEntity.internalServerError()
                .body(Response.error("查询失败: " + e.getMessage()));
        }
    }

    /**
     * [GET] 获取任务单详情
     */
    @Operation(summary = "获取任务单详情", description = "根据ID获取指定任务单的详细信息")
    @Parameter(name = "taskId", description = "任务单ID", required = true)
    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<Response<TaskOrder>> getTaskById(@PathVariable Long taskId) {
        try {
            TaskOrder task = taskService.getTaskById(taskId);
            return ResponseEntity.ok(Response.success(task));
        } catch (RuntimeException e) {
            log.error("获取任务单失败: taskId={}", taskId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("获取任务单异常: taskId={}", taskId, e);
            return ResponseEntity.internalServerError()
                .body(Response.error("获取任务单失败: " + e.getMessage()));
        }
    }

    /**
     * [PUT] 更新任务单状态
     */
    @Operation(summary = "更新任务状态", description = "更新指定任务单的状态")
    @Parameter(name = "taskId", description = "任务单ID", required = true)
    @PutMapping("/tasks/{taskId}/status")
    public ResponseEntity<Response<TaskOrder>> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestParam String status) {
        try {
            TaskOrder updatedTask = taskService.updateTaskStatus(taskId, status.toUpperCase());
            return ResponseEntity.ok(Response.success("状态更新成功", updatedTask));
        } catch (RuntimeException e) {
            log.error("更新任务状态失败: taskId={}, status={}", taskId, status, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("更新任务状态异常", e);
            return ResponseEntity.internalServerError()
                .body(Response.error("更新失败: " + e.getMessage()));
        }
    }

    /**
     * [PUT] 取消任务单
     */
    @Operation(summary = "取消任务单", description = "取消指定的任务单")
    @Parameter(name = "taskId", description = "任务单ID", required = true)
    @PutMapping("/tasks/{taskId}/cancel")
    public ResponseEntity<Response<TaskOrder>> cancelTask(
            @PathVariable Long taskId,
            @RequestParam String cancelReason) {
        try {
            TaskOrder canceledTask = taskService.cancelTask(taskId, cancelReason);
            return ResponseEntity.ok(Response.success("任务取消成功", canceledTask));
        } catch (RuntimeException e) {
            log.error("取消任务失败: taskId={}, cancelReason={}", taskId, cancelReason, e);
            return ResponseEntity.badRequest()
                .body(Response.error("取消失败: " + e.getMessage()));
        } catch (Exception e) {
            log.error("取消任务异常", e);
            return ResponseEntity.internalServerError()
                .body(Response.error("取消异常: " + e.getMessage()));
        }
    }

    /**
     * [GET] 获取任务统计信息
     */
    @Operation(summary = "任务统计", description = "获取任务处理统计信息")
    @GetMapping("/tasks/statistics")
    public ResponseEntity<Response<Map<String, Object>>> getTaskStatistics() {
        try {
            Map<String, Object> statistics = taskService.getTaskStatistics();
            return ResponseEntity.ok(Response.success(statistics));
        } catch (Exception e) {
            log.error("获取任务统计失败", e);
            return ResponseEntity.internalServerError()
                .body(Response.error("获取统计失败: " + e.getMessage()));
        }
    }}
