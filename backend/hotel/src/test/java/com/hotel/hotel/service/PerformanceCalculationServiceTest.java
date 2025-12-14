package com.hotel.hotel.service;

import com.hotel.hotel.entity.Department;
import com.hotel.hotel.entity.CustomerFeedback;
import com.hotel.hotel.entity.DepartmentPerformance;
import com.hotel.hotel.repository.CustomerFeedbackRepository;
import com.hotel.hotel.repository.DepartmentRepository;
import com.hotel.hotel.repository.DepartmentPerformanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 部门绩效计算服务 PerformanceCalculationService 的单元测试
 */
@ExtendWith(MockitoExtension.class)
public class PerformanceCalculationServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private CustomerFeedbackRepository feedbackRepository;

    @Mock
    private DepartmentPerformanceRepository performanceRepository;

    @InjectMocks
    private PerformanceCalculationService performanceService;

    // 模拟数据
    private Department roomService;
    private CustomerFeedback goodFeedback;
    private CustomerFeedback badFeedback;

    @BeforeEach
    void setUp() {
        // 房务部 (权重在这里的计算中暂未用到，但保留字段)
        roomService = new Department();
        roomService.setDeptId(1L);
        roomService.setDeptName("房务部");
        roomService.setWeight(new BigDecimal("0.40")); // 0.40

        // 积极反馈 (情感得分 0.8)
        goodFeedback = new CustomerFeedback();
        goodFeedback.setSentimentScore(new BigDecimal("0.8"));
        goodFeedback.setDepartment(roomService);
        goodFeedback.setIsProcessed(false); // 初始状态为未处理

        // 消极反馈 (情感得分 -0.2)
        badFeedback = new CustomerFeedback();
        badFeedback.setSentimentScore(new BigDecimal("-0.2"));
        badFeedback.setDepartment(roomService);
        badFeedback.setIsProcessed(false); // 初始状态为未处理
    }

    @Test
    void testCalculateDailyPerformance_Success_InsertNewRecord() {
        // --- 模拟行为 (Mocking Setup) ---

        // 1. 模拟部门仓库：返回房务部列表
        when(departmentRepository.findAll()).thenReturn(List.of(roomService));

        // 2. 【核心修复】模拟反馈仓库：返回非空列表，以避免 Service 层的 'continue' 跳过计算
        // 确保 Mocking 的方法签名与 Service 实际调用的一致：findByDepartmentDeptIdAndIsProcessedFalse
        when(feedbackRepository.findByDepartmentDeptIdAndIsProcessedFalse(anyLong()))
                .thenReturn(Arrays.asList(goodFeedback, badFeedback));

        // 3. 模拟绩效仓库查找：返回 Optional.empty()，确保执行 INSERT (创建新记录) 路径
        when(performanceRepository.findByDepartmentDeptIdAndStatisticsDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        // --- 执行 Service 方法 ---
        performanceService.calculateDailyPerformance();

        // --- 验证结果 (Verification) ---

        // 1. 验证 performanceRepository.save() 被调用一次，并捕获保存的记录
        ArgumentCaptor<DepartmentPerformance> performanceCaptor = ArgumentCaptor.forClass(DepartmentPerformance.class);
        verify(performanceRepository, times(1)).save(performanceCaptor.capture());

        DepartmentPerformance savedPerformance = performanceCaptor.getValue();

        // 2. 验证 Score Index 计算是否正确
        // -----------------------------------------------------
        // Service 逻辑计算验证：
        // a. Good Feedback 归一化: (0.8 + 1) / 2 = 0.9
        // b. Bad Feedback 归一化: (-0.2 + 1) / 2 = 0.4
        // c. 总归一化分: 0.9 + 0.4 = 1.3
        // d. 平均归一化分: 1.3 / 2 = 0.65
        // e. ScoreIndex: 0.65 * 100 = 65.00
        // -----------------------------------------------------
        BigDecimal expectedScore = new BigDecimal("65.00");

        assertEquals(expectedScore, savedPerformance.getScoreIndex(),
                "绩效指数计算结果不正确");

        // 3. 验证其他关键字段
        assertEquals(2, savedPerformance.getTotalReviews(), "总评价数不正确");
        // ⚠️ 预警等级需要根据您的 determineAlertLevel() 逻辑来确定，我们假设 65.00 是 YELLOW
        // 由于 determineAlertLevel() 逻辑未在 Service 中提供，如果这里失败，请根据实际逻辑修改期望值。
        // assertEquals("YELLOW", savedPerformance.getAlertLevel(), "预警等级不正确");
        assertEquals(roomService.getDeptId(), savedPerformance.getDepartment().getDeptId(), "关联部门ID不正确");
        assertNotNull(savedPerformance.getStatisticsDate(), "统计日期不应为空");

        // 4. 验证 feedbackRepository.saveAll() 被调用一次 (标记已处理)
        verify(feedbackRepository, times(1)).saveAll(argThat(obj -> {
            // 强制转换为正确的 List 类型
            @SuppressWarnings("unchecked")
            List<CustomerFeedback> feedbackList = (List<CustomerFeedback>) obj;

            // 现在 feedbackList.size() 和 feedbackList.stream() 应该不会标红了
            return feedbackList.size() == 2 &&
                    feedbackList.stream().allMatch(CustomerFeedback::getIsProcessed);
        }));
    }

    // 您也可以添加另一个测试方法来验证 UPDATES (更新现有记录) 的逻辑，但这里主要解决您的问题
    @Test
    void testCalculateDailyPerformance_SkipCalculation() {
        // 模拟部门：返回房务部列表
        when(departmentRepository.findAll()).thenReturn(List.of(roomService));

        // 模拟反馈仓库：返回空列表，触发 Service 层的 'continue' 逻辑
        when(feedbackRepository.findByDepartmentDeptIdAndIsProcessedFalse(anyLong()))
                .thenReturn(List.of()); // <-- 返回空列表

        // 执行 Service 方法
        performanceService.calculateDailyPerformance();

        // 验证结果
        // 验证 performanceRepository.save() 绝对不能被调用
        verify(performanceRepository, never()).save(any(DepartmentPerformance.class));

        // 验证 feedbackRepository.saveAll() 也不能被调用
        verify(feedbackRepository, never()).saveAll(anyList());
    }
}