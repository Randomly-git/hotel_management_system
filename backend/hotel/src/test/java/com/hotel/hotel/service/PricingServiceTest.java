package com.hotel.hotel.service;

import com.hotel.hotel.entity.RoomType;
import com.hotel.hotel.entity.PricingRecord;
import com.hotel.hotel.repository.RoomTypeRepository;
import com.hotel.hotel.repository.PricingRecordRepository;
import com.hotel.hotel.service.PricingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PricingServiceTest {

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private PricingRecordRepository pricingRecordRepository;

    @InjectMocks
    private PricingService pricingService;

    // 模拟数据
    private RoomType standardRoom;
    private final BigDecimal BASE_PRICE = new BigDecimal("1000.00");

    @BeforeEach
    void setUp() {
        standardRoom = new RoomType();
        standardRoom.setTypeId(1);
        standardRoom.setTypeName("豪华大床房");
        standardRoom.setBasePrice(BASE_PRICE);
    }

    @Test
    void testAdjustPrice_Weekday_BasePriceFactor() {
        // 假设目标日期是平日 (周一)
        LocalDate monday = LocalDate.of(2025, 12, 15); // 假设 12月15日是周一

        // 模拟 PricingService 内部的 collectDynamicFactors 方法返回平日因子 1.0
        // 由于我们无法直接mock private方法，我们必须依赖 adjustPrice 方法的实际行为。
        // 在 Service 逻辑中，平日因子是 1.0

        when(pricingRecordRepository.save(any(PricingRecord.class))).thenAnswer(invocation -> {
            PricingRecord record = invocation.getArgument(0);
            // 验证调整后的价格是否接近基础价格 (平日因子 1.0)
            // 考虑到内部模拟的随机浮动 (竞争对手/预订率)，价格应在 0.7*1000 到 1.3*1000 之间。
            assertTrue(record.getAdjustedPrice().compareTo(new BigDecimal("700.00")) >= 0);
            assertTrue(record.getAdjustedPrice().compareTo(new BigDecimal("1300.00")) <= 0);
            return record;
        });

        // 执行方法
        PricingRecord result = pricingService.adjustPrice(standardRoom, monday);

        // 验证价格是否在合理浮动范围内
        assertNotNull(result);
        assertTrue(result.getAdjustedPrice().compareTo(BASE_PRICE.multiply(new BigDecimal("0.9"))) > 0);

        verify(pricingRecordRepository, times(1)).save(any(PricingRecord.class));
    }

    @Test
    void testAdjustPrice_Weekend_IncreasedFactor() {
        // 假设目标日期是周末 (周六)
        LocalDate saturday = LocalDate.of(2025, 12, 20); // 假设 12月20日是周六

        // 周末因子为 1.2
        // 价格起点: 1000 * 1.2 = 1200

        when(pricingRecordRepository.save(any(PricingRecord.class))).thenAnswer(invocation -> {
            PricingRecord record = invocation.getArgument(0);
            // 验证调整后的价格是否高于平日起点 1000
            assertTrue(record.getAdjustedPrice().compareTo(new BigDecimal("1000.00")) >= 0);
            // 验证价格是否在合理浮动范围内 (基于 1.2 的乘数，调整后价格应高于 1200 的某个百分比)
            assertTrue(record.getAdjustedPrice().compareTo(new BigDecimal("1100.00")) > 0);
            return record;
        });

        // 执行方法
        PricingRecord result = pricingService.adjustPrice(standardRoom, saturday);

        // 验证价格是否显著高于基础价格
        assertNotNull(result);
        assertTrue(result.getAdjustedPrice().compareTo(BASE_PRICE.multiply(new BigDecimal("1.1"))) > 0);
    }

    @Test
    void testAdjustPrice_PriceCeiling_Capped() {
        // 目标日期是周末，但我们模拟 Service 内部逻辑，使其触发价格上限。
        // 价格上限设置为基础价格 * 1.8 (Service 中定义) -> 1800.00
        LocalDate targetDate = LocalDate.of(2025, 12, 20);

        when(pricingRecordRepository.save(any(PricingRecord.class))).thenAnswer(invocation -> {
            PricingRecord record = invocation.getArgument(0);
            // 在实际 Service 内部逻辑中，如果随机数触发了最大的竞争对手和最低的入住率（导致价格向上截断），则价格会接近 1800
            // 这里我们只需要验证最终价格没有超过 1800.00
            assertTrue(record.getAdjustedPrice().compareTo(new BigDecimal("1800.00")) <= 0);
            return record;
        });

        // 执行方法
        PricingRecord result = pricingService.adjustPrice(standardRoom, targetDate);

        // 验证价格没有超过上限 (这里依赖 Service 内部的 maxPrice 逻辑)
        assertNotNull(result);
        assertTrue(result.getAdjustedPrice().compareTo(new BigDecimal("1800.00")) <= 0);
    }
}
