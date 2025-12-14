package com.hotel.hotel.service;

import com.hotel.hotel.entity.Department;
import com.hotel.hotel.entity.TaskOrder;
import com.hotel.hotel.repository.DepartmentRepository;
import com.hotel.hotel.repository.TaskOrderRepository;
import com.hotel.hotel.service.TaskService;
import com.hotel.hotel.service.GuestProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskOrderRepository taskOrderRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private GuestProfileService profileService; // 虽然没用，但 TaskService 依赖它

    @InjectMocks
    private TaskService taskService;

    // 模拟数据
    private Department roomServiceDept;

    @BeforeEach
    void setUp() {
        roomServiceDept = new Department(1L, "房务部", new BigDecimal("0.40"), null);
    }

    @Test
    void testGenerateTaskFromPrediction_Success() {
        // 模拟依赖
        when(departmentRepository.findByDeptName("房务部")).thenReturn(Optional.of(roomServiceDept));
        when(taskOrderRepository.save(any(TaskOrder.class))).thenAnswer(invocation -> {
            TaskOrder task = invocation.getArgument(0);
            task.setTaskId(100L); // 模拟 ID 生成
            return task;
        });

        // 执行方法
        TaskOrder result = taskService.generateTaskFromPrediction(
                "VIP001", "预测需要安静房间", "房务部", 60
        );

        // 验证结果
        assertNotNull(result);
        assertEquals("VIP001", result.getGuestMemberId());
        assertEquals("PREDICTION", result.getTaskType());
        assertEquals("PENDING", result.getStatus());
        assertEquals("房务部", result.getAssignedDepartment().getDeptName());

        // 验证保存操作被执行
        verify(taskOrderRepository, times(1)).save(any(TaskOrder.class));
    }

    @Test
    void testUpdateTaskStatus_ToCompleted_Success() {
        // 模拟任务单
        TaskOrder existingTask = new TaskOrder();
        existingTask.setTaskId(1L);
        existingTask.setStatus("PENDING");

        // 模拟依赖
        when(taskOrderRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskOrderRepository.save(any(TaskOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 执行方法
        TaskOrder updatedTask = taskService.updateTaskStatus(1L, "COMPLETED");

        // 验证结果
        assertEquals("COMPLETED", updatedTask.getStatus());
        verify(taskOrderRepository, times(1)).save(existingTask);
    }

    @Test
    void testUpdateTaskStatus_InvalidStatus_ThrowsException() {
        // 模拟任务单
        TaskOrder existingTask = new TaskOrder();
        existingTask.setTaskId(1L);
        existingTask.setStatus("PENDING");

        // 模拟依赖
        when(taskOrderRepository.findById(1L)).thenReturn(Optional.of(existingTask));

        // 执行方法并验证异常
        assertThrows(IllegalArgumentException.class, () -> {
            taskService.updateTaskStatus(1L, "INVALID_STATUS");
        });

        // 验证保存操作未被执行
        verify(taskOrderRepository, never()).save(any(TaskOrder.class));
    }
}