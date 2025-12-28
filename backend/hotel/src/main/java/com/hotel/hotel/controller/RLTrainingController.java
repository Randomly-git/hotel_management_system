package com.hotel.hotel.controller;

import com.hotel.hotel.service.QLearningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RL 训练 API
 * 用于触发和管理强化学习模型的训练
 */
@Slf4j
@RestController
@RequestMapping("/api/rl-training")
@RequiredArgsConstructor
public class RLTrainingController {

    private final QLearningService qLearningService;
    private final Random random = new Random();

    // 存储训练状态
    private final Map<String, TrainingStatus> trainingStatuses = new ConcurrentHashMap<>();

    /**
     * 开始训练
     * POST /api/rl-training/train
     * Body: {"hotelId": 1, "roomTypeId": 1, "episodes": 100}
     */
    @PostMapping("/train")
    public ResponseEntity<Map<String, Object>> startTraining(@RequestBody TrainingRequest request) {
        String taskId = UUID.randomUUID().toString();

        // 初始化训练状态
        TrainingStatus status = new TrainingStatus();
        status.taskId = taskId;
        status.status = "running";
        status.progress = 0;
        status.totalEpisodes = request.getEpisodes();
        trainingStatuses.put(taskId, status);

        // 异步执行训练
        new Thread(() -> {
            try {
                trainModel(request.getHotelId(), request.getRoomTypeId(), request.getEpisodes(), taskId);
            } catch (Exception e) {
                log.error("训练失败: {}", e.getMessage(), e);
                TrainingStatus s = trainingStatuses.get(taskId);
                if (s != null) {
                    s.status = "failed";
                    s.errorMessage = e.getMessage();
                }
            }
        }).start();

        Map<String, Object> response = new HashMap<>();
        response.put("taskId", taskId);
        response.put("message", "训练任务已启动");
        response.put("episodes", request.getEpisodes());
        return ResponseEntity.ok(response);
    }

    /**
     * 查询训练状态
     * GET /api/rl-training/status/{taskId}
     */
    @GetMapping("/status/{taskId}")
    public ResponseEntity<Map<String, Object>> getTrainingStatus(@PathVariable String taskId) {
        TrainingStatus status = trainingStatuses.get(taskId);
        if (status == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("taskId", status.taskId);
        response.put("status", status.status);
        response.put("progress", status.progress);
        response.put("totalEpisodes", status.totalEpisodes);
        response.put("currentEpisode", status.currentEpisode);
        response.put("result", status.result);
        response.put("errorMessage", status.errorMessage);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取所有训练任务列表
     * GET /api/rl-training/tasks
     */
    @GetMapping("/tasks")
    public ResponseEntity<Map<String, TrainingStatus>> getAllTasks() {
        return ResponseEntity.ok(trainingStatuses);
    }

    /**
     * 模拟训练过程
     */
    private void trainModel(Long hotelId, Long roomTypeId, int episodes, String taskId) {
        log.info("开始训练: hotelId={}, roomTypeId={}, episodes={}", hotelId, roomTypeId, episodes);

        BigDecimal totalReward = BigDecimal.ZERO;
        BigDecimal roomPrice = new BigDecimal("500");

        for (int episode = 0; episode < episodes; episode++) {
            BigDecimal episodeReward = BigDecimal.ZERO;

            // 模拟30天的训练周期
            for (int day = 0; day < 30; day++) {
                LocalDate targetDate = LocalDate.now().plusDays(day);

                // 获取当前状态
                String stateKey = encodeState(hotelId, roomTypeId, targetDate);

                // 随机选择动作（探索）
                int action = random.nextInt(6); // 0-5

                // 模拟环境反馈
                int cancellations = random.nextInt(4);
                int noShows = random.nextInt(3);

                // 计算奖励
                BigDecimal reward = qLearningService.calculateReward(
                        action, cancellations, noShows, roomPrice, new BigDecimal("1.5")
                );
                episodeReward = episodeReward.add(reward);

                // 获取下一状态
                String nextStateKey = encodeState(hotelId, roomTypeId, targetDate.plusDays(1));

                // 更新Q值
                qLearningService.updateQValue(
                        hotelId, roomTypeId, stateKey, action, reward, nextStateKey
                );
            }

            totalReward = totalReward.add(episodeReward);
            BigDecimal avgReward = totalReward.divide(BigDecimal.valueOf(episode + 1), 2, RoundingMode.HALF_UP);

            // 更新进度
            TrainingStatus status = trainingStatuses.get(taskId);
            if (status != null) {
                status.currentEpisode = episode + 1;
            }

            if ((episode + 1) % 10 == 0) {
                int progress = (int) ((episode + 1) * 100.0 / episodes);
                if (status != null) {
                    status.progress = progress;
                }
                log.info("训练进度: {}/{}, 平均奖励: {}", episode + 1, episodes, avgReward);
            }
        }

        // 训练完成
        TrainingStatus finalStatus = trainingStatuses.get(taskId);
        if (finalStatus != null) {
            Map<String, Object> result = new HashMap<>();
            result.put("episodes", episodes);
            result.put("totalReward", totalReward);
            result.put("avgReward", totalReward.divide(BigDecimal.valueOf(episodes), 2, RoundingMode.HALF_UP));
            result.put("epsilon", 0.1);

            finalStatus.status = "completed";
            finalStatus.progress = 100;
            finalStatus.result = result;
        }

        log.info("训练完成: hotelId={}, roomTypeId={}, totalReward={}", hotelId, roomTypeId, totalReward);
    }

    /**
     * 编码状态（简化版本）
     */
    private String encodeState(Long hotelId, Long roomTypeId, LocalDate date) {
        String occupancyLevel = switch (random.nextInt(4)) {
            case 0 -> "LOW";
            case 1 -> "MEDIUM";
            case 2 -> "HIGH";
            default -> "VERY_HIGH";
        };

        String dayType = date.getDayOfWeek().toString().substring(0, 3);

        String leadCategory = switch (random.nextInt(3)) {
            case 0 -> "0";
            case 1 -> "1";
            default -> "2";
        };

        return String.format("OCC_%s_%s_%s", occupancyLevel, dayType, leadCategory);
    }

    /**
     * 训练状态内部类
     */
    private static class TrainingStatus {
        String taskId;
        String status;
        int progress;
        int totalEpisodes;
        int currentEpisode;
        Map<String, Object> result;
        String errorMessage;
    }

    /**
     * 训练请求 DTO
     */
    @lombok.Data
    public static class TrainingRequest {
        private Long hotelId;
        private Long roomTypeId;
        private Integer episodes = 100;
    }
}
