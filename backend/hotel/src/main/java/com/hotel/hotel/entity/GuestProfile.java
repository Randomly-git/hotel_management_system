package com.hotel.hotel.entity;

import com.hotel.hotel.util.AESUtil;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 客户画像实体类
 * 存储客户的基本信息、偏好标签和历史数据
 */
@Data
@NoArgsConstructor
@Slf4j
@Entity
@Table(name = "guest_profile")
public class GuestProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long guestId;

    @Column(nullable = false, unique = true, length = 50)
    private String memberId;

    @Column(name = "hotel_id")
    private Long hotelId;  // 租户隔离字段

    // 基本信息
    @Column(length = 100)
    private String customerName;

    // 加密存储的敏感信息
    @Column(name = "id_card_encrypted", length = 500)
    private String idCardEncrypted;  // 加密后的身份证号

    @Column(name = "phone_encrypted", length = 500)
    private String phoneEncrypted;  // 加密后的手机号

    // 偏好和标签
    @Column(name = "preference_tags", columnDefinition = "TEXT")
    private String preferenceTags;  // 逗号分隔的偏好标签

    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;  // 逗号分隔的客户标签

    // 住宿信息
    @Column(name = "last_check_in")
    private LocalDate lastCheckIn;

    @Column(name = "last_stay_room", length = 20)
    private String lastStayRoom;  // 最近入住房间号

    @Column(name = "total_stay")
    private Integer totalStay;  // 累计入住次数

    // 消费信息
    @Column(precision = 10, scale = 2)
    private BigDecimal avgSpend;  // 平均消费

    @Column(name = "total_spend", precision = 12, scale = 2)
    private BigDecimal totalSpend;  // 累计消费

    @Lob
    @Column(name = "prediction_model_data")
    private String predictionModelData;  // AI预测模型数据

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;  // 备注

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 便捷的加密/解密方法
    public void setIdCard(String idCard) {
        try {
            this.idCardEncrypted = AESUtil.encrypt(idCard);
        } catch (Exception e) {
            log.error("加密身份证号失败", e);
            this.idCardEncrypted = null;
        }
    }

    public String getIdCard() {
        try {
            return idCardEncrypted != null ? AESUtil.decrypt(idCardEncrypted) : null;
        } catch (Exception e) {
            log.error("解密身份证号失败", e);
            return null;
        }
    }

    public void setPhone(String phone) {
        try {
            this.phoneEncrypted = AESUtil.encrypt(phone);
        } catch (Exception e) {
            log.error("加密手机号失败", e);
            this.phoneEncrypted = null;
        }
    }

    public String getPhone() {
        try {
            return phoneEncrypted != null ? AESUtil.decrypt(phoneEncrypted) : null;
        } catch (Exception e) {
            log.error("解密手机号失败", e);
            return null;
        }
    }
}