package com.hotel.hotel.service;

import com.hotel.hotel.entity.Booking;
import com.hotel.hotel.entity.Room;
import com.hotel.hotel.repository.BookingRepository;
import com.hotel.hotel.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private PricingService pricingService;

    public Booking bookRoom(Integer roomId, Integer memberId, Date checkInDate, Integer days) {
        // 1. 基础参数校验
        if (roomId == null || checkInDate == null || days == null || days <= 0) {
            throw new RuntimeException("预订失败：参数不完整或居住天数不正确。");
        }

        // 2. 检查物理房间是否存在
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("预订失败：未找到 ID 为 " + roomId + " 的房间。"));

        // 3. 计算预订离店日期
        Calendar cal = Calendar.getInstance();
        cal.setTime(checkInDate);
        cal.add(Calendar.DATE, days);
        Date checkOutDate = cal.getTime();

        // 4. 执行冲突检查
        List<Booking> conflicts = bookingRepository.findConflictingBookings(roomId, checkInDate, checkOutDate);
        if (!conflicts.isEmpty()) {
            throw new RuntimeException("预订失败：该房间在选定日期段内已被占用。");
        }

        // --- 核心修改点：第 5 步 每日阶梯定价计算 ---
        // 5. 循环计算每一天的价格并累加
        BigDecimal totalMoney = BigDecimal.ZERO;
        StringBuilder priceDetail = new StringBuilder(); // 用于记录每日价格明细

        Calendar walkCal = Calendar.getInstance();
        walkCal.setTime(checkInDate);

        for (int i = 0; i < days; i++) {
            Date currentDate = walkCal.getTime();
            // 调用 PricingService 获取那一天的具体价格
            BigDecimal dailyPrice = pricingService.getEffectivePrice(room.getRoomType(), currentDate);

            if (dailyPrice == null) {
                throw new RuntimeException("预订失败：无法获取日期 " + currentDate + " 的有效房价。");
            }

            totalMoney = totalMoney.add(dailyPrice);

            // 记录一下，方便在备注里查账
            if (i < 3) { // 仅记录前三天，防止备注过长
                priceDetail.append(String.format("[%tF:%.2f] ", currentDate, dailyPrice));
            }

            // 日期向后推一天
            walkCal.add(Calendar.DATE, 1);
        }

        if (days > 3) priceDetail.append("...");

        // 6. 构造并保存 Booking 对象
        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setRoomId(room.getId());
        booking.setMemberId(memberId != null ? memberId : 3);
        booking.setStartTime(checkInDate);
        booking.setDays(days);
        booking.setStatus(1); // 1-已预订
        booking.setMoney(totalMoney); // 这里存入的是累加后的总价

        // 增强备注信息：记录每日明细，让管理员在老后台一眼看出为何是这个价格
        booking.setRemark("智能每日调价下单。明细：" + priceDetail.toString());

        // 7. 持久化
        return bookingRepository.save(booking);
    }
}