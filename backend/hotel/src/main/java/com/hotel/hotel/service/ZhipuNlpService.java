package com.hotel.hotel.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.hotel.entity.Department;
import com.hotel.hotel.repository.DepartmentRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 智谱AI NLP服务
 * 负责解析客户的自然语言需求
 */
@Slf4j
@Service
public class ZhipuNlpService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    // 注入部门
    @Autowired
    private DepartmentRepository departmentRepository;

    private String getAllDepartmentNames() {
        List<Department> depts = departmentRepository.findAll();
        return depts.stream()
                .map(Department::getDeptName)
                .collect(Collectors.joining("/"));
    }

    @Value("${zhipu.ai.api-key}")
    private String apiKey;

    @Value("${zhipu.ai.api-url}")
    private String apiUrl;

    @Value("${zhipu.ai.model}")
    private String model;

    public ZhipuNlpService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    /**
     * 分析单条反馈
     * 复用了底层的 callZhipuAi 通用逻辑
     */
    public NlpResult analyzeFeedback(String text) {
        // 动态获取当前数据库中所有的部门名称
        String deptNames = getAllDepartmentNames();
        String prompt = String.format("""
            请分析以下酒店客户评价，并以JSON格式返回：
            1. sentiment_score: 情感得分 (-1.0 到 1.0)
            2. recommendedDepartment: 归属部门 (%s)
            内容: "%s"
            注意：仅返回JSON。
            """,deptNames, text);

        try {
            // 注意：这里用 .block() 是因为 Controller 那边目前是同步阻塞调用的
            String jsonResponse = callZhipuAi(prompt).block();
            JsonNode root = objectMapper.readTree(jsonResponse);
            String content = root.path("choices").get(0).path("message").path("content").asText();

            // 简单处理 AI 可能带的反引号
            if (content.contains("```json")) {
                content = content.substring(content.indexOf("{"), content.lastIndexOf("}") + 1);
            }

            return objectMapper.readValue(content, NlpResult.class);
        } catch (Exception e) {
            log.error("绩效分析失败: {}", e.getMessage());
            NlpResult fallback = new NlpResult();
            fallback.setSentimentScore(BigDecimal.ZERO);
            fallback.setRecommendedDepartment("业务部");
            return fallback;
        }
    }

    /**
     * 根据负面反馈生成改进建议
     * @param feedbackSummary 汇总后的负面反馈文本
     * @return AI 生成的建议文本
     */
    public String generateManagementSuggestions(String feedbackSummary) {
        String prompt = "基于以下负面评价汇总，给出酒店管理建议：" + feedbackSummary;
        try {
            String jsonResponse = callZhipuAi(prompt).block();
            JsonNode root = objectMapper.readTree(jsonResponse);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            return "暂无建议";
        }
    }

    /**
     * 【通用：调用AI】
     */
    private Mono<String> callZhipuAi(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        return webClient.post()
                .uri(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class);
    }

    /**
     * 解析客户自然语言需求
     * @param customerRequest 客户的自然语言请求
     * @return 解析结果（包含意图、实体等信息）
     */
    public Mono<NlpResult> parseCustomerRequest(String customerRequest) {
        log.info("解析客户请求: {}", customerRequest);

        // 构建请求提示词
        String prompt = buildPrompt(customerRequest);

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);

        List<Map<String, String>> messages = List.of(
            Map.of("role", "system", "content", getSystemPrompt()),
            Map.of("role", "user", "content", prompt)
        );
        requestBody.put("messages", messages);

        // 设置请求参数
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("temperature", 0.3);  // 降低随机性，提高稳定性
        parameters.put("max_tokens", 500);
        requestBody.put("parameters", parameters);

        return webClient.post()
                .uri(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseNlpResponse)
                .doOnSuccess(result -> log.info("NLP解析成功: {}", result))
                .doOnError(error -> log.error("NLP解析失败", error))
                .onErrorReturn(createFallbackResult(customerRequest));  // 降级处理
    }

    /**
     * 构建系统提示词
     */
    private String getSystemPrompt() {
        return """
            你是一个酒店服务需求解析专家。你的任务是分析客户的自然语言请求，提取以下信息：

            1. 主要意图（从以下类别选择）：
               - ROOM_SERVICE: 房间相关服务
               - MAINTENANCE: 维修服务
               - CONCIERGE: 礼宾服务
               - DINING: 餐饮服务
               - OTHER: 其他

            2. 具体需求描述
            3. 数量信息（如果有）
            4. 时间要求
            5. 房间号（如果有）
            6. 推荐负责部门（从以下选择）：
               - 房务部: 清洁、布草、房间设施
               - 工程部: 维修、设备故障
               - 前厅部: 入住、退房、咨询
               - 餐饮部: 送餐、餐饮服务
               - 礼宾部: 行李、接送、预订

            请以JSON格式返回解析结果。
            """;
    }

    /**
     * 构建请求提示词
     */
    private String buildPrompt(String customerRequest) {
        return String.format("""
            请解析以下客户请求：

            客户请求："%s"

            请以JSON格式返回，格式如下：
            {
                "intent": "意图类别",
                "description": "具体需求描述",
                "quantity": 数量（数字，如无则为null），
                "timeRequirement": "时间要求",
                "roomNumber": "房间号（如无则为null）",
                "recommendedDepartment": "推荐负责部门",
                "urgency": "HIGH/MEDIUM/LOW"
            }

            如果无法准确识别，请将intent设为"UNKNOWN"，description设为"需要人工确认"。
            """, customerRequest);
    }

    /**
     * 解析NLP响应
     */
    private NlpResult parseNlpResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode choices = root.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                String content = choices.get(0).get("message").get("content").asText();

                // 尝试解析JSON内容
                try {
                    // 提取JSON内容（去掉代码块格式）
                    String jsonContent = content;
                    log.debug("AI返回原始内容: {}", content);

                    if (content.contains("```json")) {
                        log.info("检测到代码块格式，开始提取JSON...");
                        int start = content.indexOf("```json") + 7;
                        int end = content.lastIndexOf("```");

                        log.debug("代码块提取位置: start={}, end={}, length={}", start, end, content.length());

                        if (start >= 7 && end > start) {
                            jsonContent = content.substring(start, end).trim();
                            log.info("成功提取JSON内容（方法1）: {}", jsonContent);
                        } else {
                            log.warn("方法1失败，尝试替代方案");
                            // 替代方案：使用正则表达式提取
                            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("```json\\s*([\\s\\S]*?)\\s*```");
                            java.util.regex.Matcher matcher = pattern.matcher(content);
                            if (matcher.find()) {
                                jsonContent = matcher.group(1).trim();
                                log.info("成功提取JSON内容（方法2）: {}", jsonContent);
                            } else {
                                log.error("无法提取JSON内容，使用原始内容");
                            }
                        }
                    }

                    log.info("提取的JSON内容: {}", jsonContent);
                    JsonNode contentJson = objectMapper.readTree(jsonContent);
                    NlpResult result = new NlpResult();

                    // 手动解析JSON字段
                    if (contentJson.has("intent")) {
                        result.setIntent(contentJson.get("intent").asText());
                    }
                    if (contentJson.has("description")) {
                        result.setDescription(contentJson.get("description").asText());
                    }
                    if (contentJson.has("recommendedDepartment")) {
                        String dept = contentJson.get("recommendedDepartment").asText();
                        result.setRecommendedDepartment(dept);
                    }
                    if (contentJson.has("urgency")) {
                        result.setUrgency(contentJson.get("urgency").asText());
                    }

                    log.info("成功解析NLP结果: intent={}, description={}, dept={}",
                        result.getIntent(), result.getDescription(), result.getRecommendedDepartment());
                    return result;
                } catch (Exception e) {
                    log.warn("无法解析JSON内容，使用文本解析: {}", content);
                    return parseFromText(content);
                }
            }
        } catch (Exception e) {
            log.error("解析NLP响应失败", e);
        }
        return new NlpResult();
    }

    /**
     * 从文本解析（降级方案）
     */
    private NlpResult parseFromText(String content) {
        NlpResult result = new NlpResult();
        result.setIntent("UNKNOWN");
        result.setDescription("需要人工确认: " + content);
        result.setRecommendedDepartment("业务部");
        result.setUrgency("MEDIUM");
        return result;
    }

    /**
     * 创建降级结果
     */
    private NlpResult createFallbackResult(String customerRequest) {
        NlpResult result = new NlpResult();
        result.setIntent("UNKNOWN");
        result.setDescription("AI服务暂时不可用，需要人工处理");
        result.setOriginalRequest(customerRequest);
        result.setRecommendedDepartment("业务部");  // 修改为业务部
        result.setUrgency("MEDIUM");
        return result;
    }

    /**
     * NLP解析结果内部类
     */
    @Data
    public static class NlpResult {
        private String intent;
        private String description;
        private Integer quantity;
        private String timeRequirement;
        private String roomNumber;
        private String urgency;
        private String originalRequest;
        private Double confidence = 0.0;

        @JsonProperty("sentiment_score")
        private BigDecimal sentimentScore;

        @JsonProperty("recommendedDepartment")
        private String recommendedDepartment;

        @Override
        public String toString() {
            return "NlpResult{" +
                    "intent='" + intent + '\'' +
                    ", description='" + description + '\'' +
                    ", recommendedDepartment='" + recommendedDepartment + '\'' +
                    ", urgency='" + urgency + '\'' +
                    '}';
        }
    }
}