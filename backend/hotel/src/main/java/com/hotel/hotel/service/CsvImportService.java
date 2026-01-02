package com.hotel.hotel.service;

import com.hotel.hotel.entity.Booking;
import com.hotel.hotel.repository.BookingRepository;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CsvImportService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    public void importHotelData(String fileName) {
        // 获取 backend/hotel 目录下的文件路径
        String path = System.getProperty("user.dir") + File.separator + fileName;
        System.out.println("准备从路径读取文件: " + path);

        try {
            // 临时关闭外键检查，确保即使没有对应的 Hotel 或 Customer 也能导入数据
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
            System.out.println("⚠️ 已临时关闭外键约束...");
        } catch (Exception e) {
            System.err.println("警告：无法执行关闭外键指令: " + e.getMessage());
        }

        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))
                .withSkipLines(1)
                .build()) {

            String[] line;
            int totalProcessed = 0;
            int limit = 20000; // 严格限制前 20000 条
            List<Booking> batch = new ArrayList<>();

            while ((line = reader.readNext()) != null && totalProcessed < limit) {
                // 安全校验：CSV 至少要有 30 列左右
                if (line.length < 28) continue;

                try {
                    String hotelType = line[0];
                    boolean isCanceled = "1".equals(line[1]);
                    int leadTime = Integer.parseInt(line[2]);
                    int year = Integer.parseInt(line[3]);
                    String monthName = line[4].trim().toUpperCase();
                    int day = Integer.parseInt(line[6]);

                    // 获取入住天数
                    int weekendNights = Integer.parseInt(line[7]);
                    int weekNights = Integer.parseInt(line[8]);
                    int totalNights = weekendNights + weekNights;
                    if (totalNights == 0) totalNights = 1; // 保底 1 晚

                    // 获取单价 ADR
                    String adrRaw = line[27].trim();
                    BigDecimal adr = adrRaw.isEmpty() ? BigDecimal.ZERO : new BigDecimal(adrRaw);

                    // 1. 年份加 8 处理：2015 -> 2023
                    int newYear = year + 8;
                    LocalDate checkInDate = LocalDate.of(newYear, Month.valueOf(monthName), day);

                    Booking booking = Booking.builder()
                            .hotelId(hotelType.contains("Resort") ? 1L : 2L)
                            .customerId(1L) // 预设 ID
                            .roomTypeId(1L) // 预设 ID，后续通过 SQL 统一关联
                            .bookingNumber("BK-" + UUID.randomUUID().toString().substring(0, 8))
                            .leadTime(leadTime)
                            .bookingDate(checkInDate.minusDays(leadTime).atStartOfDay())
                            .checkInDate(checkInDate)
                            .checkOutDate(checkInDate.plusDays(totalNights))
                            .staysInWeekendNights(weekendNights)
                            .staysInWeekNights(weekNights)
                            .totalNights(totalNights)
                            .adr(adr)
                            .totalPrice(adr.multiply(new BigDecimal(totalNights)))
                            .isCanceled(isCanceled)
                            .status(isCanceled ? Booking.BookingStatus.canceled : Booking.BookingStatus.confirmed)
                            // 2. 正确的索引：reserved_room_type 在第 20 列，索引为 19
                            .actualRoomType(line[19])
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();

                    batch.add(booking);
                    totalProcessed++;

                    // 每 500 条分批提交，防止 Communications link failure
                    if (batch.size() >= 500) {
                        saveBatchInNewTransaction(batch);
                        System.out.println("🚀 进度更新：已成功存入云端 " + totalProcessed + " 条记录");
                        batch.clear();
                    }

                } catch (Exception e) {
                    // 记录错误但不中断循环
                    System.err.println("跳过单条错误记录 (行 " + (totalProcessed + 1) + "): " + e.getMessage());
                }
            }

            // 处理最后一批
            if (!batch.isEmpty()) {
                saveBatchInNewTransaction(batch);
            }

            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
            System.out.println("✅ 任务完成！共计成功导入: " + totalProcessed + " 条。");

        } catch (Exception e) {
            System.err.println("❌ 导入核心逻辑异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveBatchInNewTransaction(List<Booking> batch) {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            bookingRepository.saveAll(batch);
            transactionManager.commit(status);
        } catch (Exception e) {
            transactionManager.rollback(status);
            System.err.println("批次保存失败，已回滚该批次: " + e.getMessage());
        }
    }
}