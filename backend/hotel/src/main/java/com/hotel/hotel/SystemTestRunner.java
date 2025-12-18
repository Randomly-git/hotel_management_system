package com.hotel.hotel;

import com.hotel.hotel.entity.Department;
import com.hotel.hotel.entity.TaskOrder;
import com.hotel.hotel.repository.DepartmentRepository;
import com.hotel.hotel.repository.RoomTypeRepository;
import com.hotel.hotel.service.NlpIntegrationService;
import com.hotel.hotel.service.PerformanceCalculationService;
import com.hotel.hotel.service.TaskService;
import com.hotel.hotel.service.PricingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.hotel.hotel.entity.PricingRecord;
import com.hotel.hotel.entity.RoomType;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;



// @Component
public class SystemTestRunner implements CommandLineRunner {

    // 依赖注入所有核心 Service
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private NlpIntegrationService nlpService;
    @Autowired private PerformanceCalculationService performanceService;
    @Autowired private TaskService taskService;
    @Autowired private PricingService pricingService;
    @Autowired private RoomTypeRepository roomTypeRepository;

    // 假设酒店基础部门数据已存在，如果不存在，需要先初始化
    private void initializeData() {
        if (departmentRepository.count() == 0) {
            System.out.println("⚠️ 初始化部门数据...");
            departmentRepository.saveAll(List.of(
                    new Department(null, "房务部", new java.math.BigDecimal("0.40"), null),
                    new Department(null, "服务部", new java.math.BigDecimal("0.30"), null),
                    new Department(null, "餐饮部", new java.math.BigDecimal("0.20"), null),
                    new Department(null, "工程部", new java.math.BigDecimal("0.10"), null)
            ));
            System.out.println("✅ 部门数据初始化完成。");
        }
    }

    @Override
    public void run(String... args) throws Exception {
        initializeData();

        Scanner scanner = new Scanner(System.in);
        System.out.println("\n=======================================================");
        System.out.println("  ✨ 核心模块交互式测试启动 (后端核心开发者沙箱) ✨");
        System.out.println("=======================================================");

        while (true) {
            System.out.println("\n请选择要测试的模块：");
            System.out.println("1. 模块一：口碑量化系统 (模拟反馈与绩效计算)");
            System.out.println("2. 模块二：个性化服务系统 (模拟AI预测与任务生成)");
            System.out.println("3. 模块三：动态定价系统 (模拟调价)");
            System.out.println("4. 退出测试");
            System.out.print("输入数字选择: ");

            String choice = scanner.nextLine();
            System.out.println("-------------------------------------------------");

            try {
                switch (choice) {
                    case "1":
                        testReputationQuantification(scanner);
                        break;
                    case "2":
                        testPersonalization(scanner);
                        break;
                    case "3":
                        testDynamicPricing();
                        break;
                    case "4":
                        System.out.println("👋 退出测试，程序将继续运行...");
                        return; // 退出 run 方法
                    default:
                        System.out.println("❌ 无效选择，请重新输入。");
                }
            } catch (Exception e) {
                System.err.println("❌ 测试执行错误: " + e.getMessage());
            }
        }
    }

    /**
     * 模块一：口碑量化系统测试
     */
    private void testReputationQuantification(Scanner scanner) {
        System.out.println("\n--- 口碑量化系统测试 ---");
        System.out.print("请输入客户反馈内容 (如：房间很干净): ");
        String feedback = scanner.nextLine();

        // 1. 模拟数据流入和NLP分析
        var nlpResult = nlpService.analyzeFeedback(feedback);
        Department attributedDept = nlpService.findDepartmentByName(nlpResult.getAttributedDeptName());

        System.out.println("\n[NLP 分析结果]");
        System.out.println("  情感得分: " + nlpResult.getSentimentScore());
        System.out.println("  归因部门: " + attributedDept.getDeptName());

        // 实际应用中需要调用 Controller 的 POST /feedback 逻辑来写入数据库
        // 这里为了测试 Service，我们跳过 Controller 直接模拟写入
        System.out.println(">> 假定数据已通过 Controller 成功写入 CustomerFeedback 表...");

        // 2. 模拟触发绩效计算
        System.out.println("\n>> 正在模拟触发每日绩效计算...");
        performanceService.calculateDailyPerformance();

        System.out.println("\n✅ 绩效计算完成，请检查数据库 `department_performance` 表。");
    }

    /**
     * 模块二：个性化服务系统测试
     */
    private void testPersonalization(Scanner scanner) {
        System.out.println("\n--- 个性化服务系统测试 ---");
        System.out.print("请输入客户会员ID (如：VIP001): ");
        String memberId = scanner.nextLine();

        // 1. 模拟 AI 预测
        System.out.println(">> 正在模拟 AI 预测客户需求...");
        String predictedNeed = "预测客户需要安静房间";
        String attributedDept = "房务部";

        // 2. 生成预测任务单
        TaskOrder predictedTask = taskService.generateTaskFromPrediction(
                memberId, predictedNeed, attributedDept, 120, 1L, null
        );
        System.out.println("✅ 预测任务单生成成功。任务ID: " + predictedTask.getTaskId());

        // 3. 模拟客户主动请求
        System.out.print("请输入客户主动请求内容 (如：需要多一张毛巾): ");
        String requestContent = scanner.nextLine();

        TaskOrder requestTask = taskService.generateTaskFromRequest(
                memberId, requestContent, "服务部", 1L, null, null
        );
        System.out.println("✅ 请求任务单生成成功。任务ID: " + requestTask.getTaskId());

        // 4. 查询待处理任务
        System.out.println("\n>> 查询所有待处理任务...");
        List<TaskOrder> pendingTasks = taskService.getPendingTasks();
        pendingTasks.forEach(t ->
                System.out.println(String.format("  [待处理] ID:%d, 部门:%s, 类型:%s, 内容:%s",
                        t.getTaskId(), t.getAssignedDepartment().getDeptName(), t.getTaskType(), t.getTaskContent())));

        // 5. 模拟任务完成
        if (!pendingTasks.isEmpty()) {
            TaskOrder taskToComplete = pendingTasks.get(0);
            taskService.updateTaskStatus(taskToComplete.getTaskId(), "COMPLETED");
            System.out.println(String.format(">> 任务ID %d 状态已更新为 COMPLETED。", taskToComplete.getTaskId()));
        }
    }

    /**
     * 模块三：动态定价系统测试
     */
    private void testDynamicPricing() {
        System.out.println("\n--- 动态定价系统测试 ---");

        // 1. 模拟房型数据（如果不存在）
        if (roomTypeRepository.count() == 0) {
            System.out.println("⚠️ 初始化房型数据...");
            // 使用导入的 RoomType，并依赖 @AllArgsConstructor
            roomTypeRepository.save(new RoomType(null, "豪华大床房", new java.math.BigDecimal("800.00"), 50));
            roomTypeRepository.save(new RoomType(null, "行政套房", new java.math.BigDecimal("1500.00"), 10));
        }

        // 2. 触发调价
        System.out.println(">> 正在执行多因子调价模型，计算未来 7 天价格...");
        pricingService.calculateAndAdjustPricesForFutureWeek();

        // 3. 查询今日价格
        LocalDate today = LocalDate.now();
        List<PricingRecord> todayPrices = pricingService.getPricesByEffectiveDate(today.plusDays(1)); // 查明天价格

        System.out.println("\n[查询未来一天价格结果 (" + today.plusDays(1) + ")]");
        todayPrices.forEach(p ->
                // 依赖 @Data 或 @Getter 生成的 Getter 方法 (修正了3个方法无法解析的错误)
                System.out.println(String.format("  - %s: 调整价格 %.2f (原价 %.2f)",
                        p.getRoomType().getTypeName(), p.getAdjustedPrice(), p.getOriginalPrice())));

        System.out.println("\n✅ 动态调价测试完成，请检查 `pricing_record` 表。");
    }
}