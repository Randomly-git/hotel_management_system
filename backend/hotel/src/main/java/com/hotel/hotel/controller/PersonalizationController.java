package com.hotel.hotel.controller;

import com.hotel.hotel.common.Response;
import com.hotel.hotel.dto.FeedbackRequest;
import com.hotel.hotel.entity.TaskOrder;
import com.hotel.hotel.service.TaskService;
import com.hotel.hotel.service.GuestProfileService;
import com.hotel.hotel.service.ZhipuNlpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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
@Tag(name = "个性化服务系统", description = "处理客户自然语言需求，自动生成任务单")
public class PersonalizationController {

    private final TaskService taskService;
    private final GuestProfileService profileService;
    private final ZhipuNlpService zhipuNlpService;

    @Autowired
    public PersonalizationController(TaskService taskService,
                                   GuestProfileService profileService,
                                   ZhipuNlpService zhipuNlpService) {
        this.taskService = taskService;
        this.profileService = profileService;
        this.zhipuNlpService = zhipuNlpService;
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
        private String requestContent;

        @Parameter(description = "房间号")
        private String roomNumber;
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
                dueMinutes
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
    @PostMapping("/request")
    public Mono<ResponseEntity<Response<Map<String, Object>>>> submitCustomerRequest(
            @Valid @RequestBody CustomerRequest request) {

        log.info("收到客户请求: customerId={}, content={}",
                request.getCustomerId(), request.getRequestContent());

        // 1. 使用智谱AI解析请求
        return zhipuNlpService.parseCustomerRequest(request.getRequestContent())
            .flatMap(nlpResult -> {
                // 2. 根据解析结果生成任务单
                try {
                    // 检查是否需要人工确认
                    if ("UNKNOWN".equals(nlpResult.getIntent())) {
                        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                            .body(Response.<Map<String, Object>>error("AI无法识别请求内容，需要人工处理")));
                    }

                    TaskOrder task = taskService.generateTaskFromRequest(
                        request.getCustomerId(),
                        nlpResult.getDescription(),
                        nlpResult.getRecommendedDepartment()
                    );

                    // 更新任务额外信息
                    task.setTaskContent(nlpResult.toString());
                    if (request.getRoomNumber() != null) {
                        task.setRoomNumber(request.getRoomNumber());
                    }

                    Map<String, Object> responseData = new HashMap<>();
                    responseData.put("taskId", task.getTaskId());
                    responseData.put("assignedTo", task.getAssignedDepartment().getDeptName());
                    responseData.put("nlpAnalysis", nlpResult);

                    return Mono.just(ResponseEntity.status(HttpStatus.CREATED)
                        .body(Response.success("客户请求已处理", responseData)));

                } catch (Exception e) {
                    log.error("生成任务单失败", e);
                    return Mono.just(ResponseEntity.internalServerError()
                        .body(Response.<Map<String, Object>>error("生成任务单失败: " + e.getMessage())));
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
    }
}