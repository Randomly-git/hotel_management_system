package com.hotel.hotel.service;

// --- 补齐缺少的导入 ---
import com.hotel.hotel.entity.Booking;
import com.hotel.hotel.entity.Room;
import com.hotel.hotel.entity.RoomType; // 报错找不到 RoomType 的原因
import com.hotel.hotel.repository.BookingRepository;
import com.hotel.hotel.repository.RoomRepository;
import com.hotel.hotel.repository.RoomTypeRepository; // 报错找不到 RoomTypeRepository 的原因
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal; // 报错找不到 BigDecimal 的原因
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    // 新增：注入房型仓库以修改底价
    @Autowired
    private RoomTypeRepository roomTypeRepository;

    /**
     * 新增：修改房型基础底价
     */
    public void updateBasePrice(Integer typeId, BigDecimal newBasePrice) {
        // 这里的 RoomType 必须 import
        RoomType roomType = roomTypeRepository.findById(typeId)
                .orElseThrow(() -> new RuntimeException("修改失败：找不到 ID 为 " + typeId + " 的房型。"));

        roomType.setBasePrice(newBasePrice);
        roomTypeRepository.save(roomType);
        System.out.println("📢 房型 [" + roomType.getTypeName() + "] 底价已手动调整为: " + newBasePrice);
    }

    /**
     * 查询房间详情及房态日历
     */
    public Room findById(Integer id) {
        Room room = roomRepository.findById(id).orElse(null);
        if (room == null) return null;

        List<Booking> activeBookings = bookingRepository.findByRoomIdAndStatusIn(id, Arrays.asList(1, 2));

        List<String> notUseDateList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String todayStr = sdf.format(new Date());
        boolean canUseToday = true;

        for (Booking order : activeBookings) {
            if (order.getStartTime() == null || order.getDays() == null) continue;

            for (int i = 0; i < order.getDays(); i++) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(order.getStartTime());
                cal.add(Calendar.DATE, i);

                String dateStr = sdf.format(cal.getTime());
                notUseDateList.add(dateStr);

                if (dateStr.equals(todayStr)) {
                    canUseToday = false;
                }
            }
        }

        room.setNotUseDateList(notUseDateList);
        room.setCanUse(canUseToday);

        return room;
    }
}