package com.hotel.hotel.service;

import com.hotel.hotel.entity.HotelRoomType;
import com.hotel.hotel.entity.PricingRecord;
import com.hotel.hotel.entity.Booking;
import com.hotel.hotel.repository.HotelRoomTypeRepository;
import com.hotel.hotel.repository.PricingRecordRepository;
import com.hotel.hotel.repository.BookingRepository;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression; // 引入数学库
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PricingService {

    private final HotelRoomTypeRepository roomTypeRepository;
    private final PricingRecordRepository pricingRecordRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public PricingService(HotelRoomTypeRepository roomTypeRepository,
                          PricingRecordRepository pricingRecordRepository,
                          BookingRepository bookingRepository) {
        this.roomTypeRepository = roomTypeRepository;
        this.pricingRecordRepository = pricingRecordRepository;
        this.bookingRepository = bookingRepository;
    }

    // ... getRecordsByStatus, applyPriceRecord, getAppliedPricesByDate 保持不变 ...

    public List<PricingRecord> getRecordsByStatus(String status) {
        return pricingRecordRepository.findByStatus(status);
    }

    @Transactional
    public boolean applyPriceRecord(Long recordId) {
        Optional<PricingRecord> recordOpt = pricingRecordRepository.findById(recordId);
        if (recordOpt.isPresent()) {
            PricingRecord record = recordOpt.get();
            record.setStatus("APPLIED");
            pricingRecordRepository.save(record);
            System.out.println("✅ 价格记录已批准：ID=" + recordId + ", 房型=" + record.getRoomType().getTypeCode());
            return true;
        }
        return false;
    }

    public List<PricingRecord> getAppliedPricesByDate(LocalDate date) {
        List<PricingRecord> appliedRecords = pricingRecordRepository.findByEffectiveDateAndStatus(date, "APPLIED");
        if (!appliedRecords.isEmpty()) {
            return appliedRecords;
        }
        List<HotelRoomType> allTypes = roomTypeRepository.findByHotelId(1L);
        List<PricingRecord> fallbackRecords = new ArrayList<>();
        for (HotelRoomType type : allTypes) {
            PricingRecord fallback = PricingRecord.builder()
                    .roomType(type)
                    .effectiveDate(date)
                    .basePrice(type.getBasePrice())
                    .originalPrice(type.getBasePrice())
                    .adjustedPrice(type.getBasePrice())
                    .status("BASE_PRICE_FALLBACK")
                    .adjustFactor("系统自动降级：使用房型基准价")
                    .build();
            fallbackRecords.add(fallback);
        }
        return fallbackRecords;
    }

    @Transactional
    public void calculateAndAdjustPricesForFutureWeek() {
        LocalDate anchorDate = LocalDate.now();
        List<HotelRoomType> allRoomTypes = roomTypeRepository.findByHotelId(1L);

        for (HotelRoomType roomType : allRoomTypes) {
            // 1. 【核心修改】直接调用 Repository 新增的方法，清空该房型所有的 PENDING 记录
            // 这将删除该房型下所有日期（包括 1月2号生成的）且状态为 PENDING 的建议
            pricingRecordRepository.deleteByRoomTypeAndStatus(roomType, "PENDING");

            // 2. 必须执行 flush，确保删除指令在进入下方循环生成新价格前，已在数据库层面执行完毕
            pricingRecordRepository.flush();

            // 2. 【核心步骤】：缓冲区生成 (多算前后各2天，用于平摊边缘)
            List<PricingRecord> buffer = new ArrayList<>();
            // 我们需要平摊未来 7 天 (i=0 到 6)，所以范围定为 -2 到 8
            for (int i = -2; i <= 8; i++) {
                LocalDate targetDate = anchorDate.plusDays(i);
                // 调用下方的私有计算方法（不存库）
                buffer.add(calculateSinglePrice(roomType, targetDate));
            }

            // 3. 【核心步骤】：执行 5 日平摊
            // 索引 0,1 是辅助位；索引 2 到 8 是我们要存的 7 天
            // 1. 先用临时数组存结果，不污染 buffer
            BigDecimal[] results = new BigDecimal[11];
            for (int i = 2; i <= 8; i++) {
                BigDecimal sum = BigDecimal.ZERO;
                for (int j = -2; j <= 2; j++) {
                    sum = sum.add(buffer.get(i + j).getAdjustedPrice());
                }
                results[i] = sum.divide(BigDecimal.valueOf(5), 2, RoundingMode.HALF_UP);
            }

// 2. 全部算完后，再统一写回对象
            List<PricingRecord> finalRecords = new ArrayList<>();
            for (int i = 2; i <= 8; i++) {
                PricingRecord current = buffer.get(i);
                current.setAdjustedPrice(results[i]);
                current.setAdjustFactor(current.getAdjustFactor() );
                finalRecords.add(current);
            }

            // 4. 批量存入数据库
            pricingRecordRepository.saveAll(finalRecords);
        }
    }

    /**
     * 修改后的算法逻辑：使用 Apache Commons Math 线性回归
     */
    @Transactional
    public PricingRecord calculateSinglePrice(HotelRoomType roomType, LocalDate targetDate) {
        Long typeId = roomType.getId();
        BigDecimal basePrice = roomType.getBasePrice();

        // 1. 获取样本数据（逻辑保持不变）
        List<Booking> allTrainingBookings = new ArrayList<>();
        LocalDate now = LocalDate.now();
        allTrainingBookings.addAll(bookingRepository.findTrainingDataForRegression(1L, typeId, now.minusDays(90), now));
        allTrainingBookings.addAll(bookingRepository.findTrainingDataForRegression(1L, typeId, targetDate.minusYears(1).minusDays(45), targetDate.minusYears(1).plusDays(45)));
        allTrainingBookings.addAll(bookingRepository.findTrainingDataForRegression(1L, typeId, targetDate.minusYears(2).minusDays(45), targetDate.minusYears(2).plusDays(45)));

        List<Booking> validBookings = allTrainingBookings.stream()
                .filter(b -> b.getAdr() != null && b.getAdr().compareTo(BigDecimal.valueOf(10)) > 0)
                .collect(Collectors.toList());

        String factorMsg;
        BigDecimal finalPrice;

        if (validBookings.size() < 8) {
            finalPrice = basePrice;
            factorMsg = String.format("总样本不足(%d), 无法进行同期对比回归", validBookings.size());
        } else {
            try {
                // --- 【核心修改 1】：在循环外计算一次均价，避免循环内数千次查库 ---
                double histAvgVal = getHistoricalAveragePrice(typeId).doubleValue();

                OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
                double[] y = new double[validBookings.size()];
                // 1. 降为 1 维矩阵（只看预订率），解决数学冲突
                double[][] x = new double[validBookings.size()][1];
                for (int i = 0; i < validBookings.size(); i++) {
                    Booking b = validBookings.get(i);
                    y[i] = b.getAdr().doubleValue();
                    double simulatedOcc = b.getAdr().doubleValue() / (basePrice.doubleValue() * 2.0);
                    x[i][0] = Math.min(Math.max(simulatedOcc, 0.1), 1.0);
                }

                regression.newSampleData(y, x);
                double[] beta = regression.estimateRegressionParameters();

// 2. 预测公式同步修改：截距 + 系数 * 当前预订率
                double currentOcc = calculateRealOccupancy(typeId, targetDate);
                double predictedPrice = beta[0] + beta[1] * currentOcc;

                double smoothedPrice = (basePrice.doubleValue() * 0.7) + (predictedPrice * 0.3);

                // 安全边界判断
                BigDecimal rawPredicted = BigDecimal.valueOf( smoothedPrice);
                finalPrice = rawPredicted.max(basePrice.multiply(BigDecimal.valueOf(0.7)))
                        .min(basePrice.multiply(BigDecimal.valueOf(1.5)))
                        .setScale(2, RoundingMode.HALF_UP);

                factorMsg = String.format("回归(样本:%d): β3(预订率系数)=%.2f, 当前预订率=%.2f",
                        validBookings.size(), beta[1], currentOcc);

            } catch (Exception e) {
                finalPrice = basePrice;
                factorMsg = "回归降级(数据波动不足): 使用房型基准价";
            }
        }

        // 保存逻辑保持不变
        PricingRecord record = new PricingRecord();
        record.setRoomType(roomType);
        record.setBasePrice(basePrice);
        record.setOriginalPrice(basePrice);
        record.setAdjustedPrice(finalPrice);
        record.setAdjustFactor(factorMsg);
        record.setEffectiveDate(targetDate);
        record.setStatus("PENDING");
        return record;
    }

    // ... calculateRealOccupancy 和 getHistoricalAveragePrice 保持原有实现 ...

    private double calculateRealOccupancy(Long typeId, LocalDate date) {
        // 根据你提供的数据映射房型总量
        int total;
        switch (typeId.intValue()) {
            case 1: total = 55; break; // 标准间
            case 2: total = 58; break; // 标准间（海景）
            case 3: total = 28; break; // 豪华间
            case 4: total = 38; break; // 豪华间（海景）
            case 5: total = 16; break; // 套房
            case 6: total = 8;  break; // 海景套房
            case 7: total = 10; break; // 家庭套房
            case 8: total = 4;  break; // 总统套房
            default:
                // 安全垫：如果出现新 ID 但没更新代码，默认设为 10 避免崩溃
                total = 10;
                System.err.println("⚠️ 警告：发现未定义库存的房型 ID: " + typeId);
        }

        List<Booking> activeBookings = bookingRepository.findBookingsByRoomTypeAndDateRange(
                1L, typeId, date, date.plusDays(1));

        // 执行浮点除法
        return (double) activeBookings.size() / total;
    }

    private BigDecimal getHistoricalAveragePrice(Long typeId) {
        // 同样使用专用方法获取过去一整年的真实成交数据
        LocalDate end = LocalDate.now().minusDays(1);
        LocalDate start = end.minusYears(1);

        // 调用新方法，确保包含 completed 状态
        List<Booking> history = bookingRepository.findTrainingDataForRegression(1L, typeId, start, end);

        if (history.isEmpty()) {
            return roomTypeRepository.findById(typeId).map(HotelRoomType::getBasePrice).orElse(BigDecimal.ZERO);
        }

        BigDecimal totalAdr = history.stream()
                .map(Booking::getAdr)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalAdr.divide(new BigDecimal(history.size()), 2, RoundingMode.HALF_UP);
    }
}