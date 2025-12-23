package com.hotel.hotel.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(name = "orders") // 严格对齐老数据库表名
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "member_id")
    private Integer memberId; // 暂时在 Service 层 Mock 处理

    // 映射物理房间
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", insertable = false, updatable = false)
    private Room room;

    // 为了兼容老系统，保留 roomId 的直接操作能力
    @Column(name = "room_id")
    private Integer roomId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_time")
    private Date startTime;

    @Column(name = "days")
    private Integer days;

    @Column(name = "status")
    private Integer status; // 1-已预订 2-已入住 3-已退房

    @Column(name = "remark")
    private String remark; // 备注：未来可存储“系统动态调价”说明

    @Column(name = "money", precision = 10, scale = 2)
    private BigDecimal money; // 实付金额

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "check_out_time")
    private Date checkOutTime; // 实际退房时间

    // --- 业务辅助逻辑（不存储到数据库） ---

    /**
     * 计算预定离店时间（逻辑结束时间）
     * 用于房态冲突检测
     */
    public Date getLogicalEndTime() {
        if (startTime != null && days != null) {
            return new Date(startTime.getTime() + (long) days * 24 * 60 * 60 * 1000);
        }
        return null;
    }
}