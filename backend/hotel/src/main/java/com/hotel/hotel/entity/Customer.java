package com.hotel.hotel.entity;

import com.hotel.hotel.util.AESUtil;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 统一的客户实体
 * 合并了原有的Customer和GuestProfile的所有功能
 */
@Data
@Entity
@Table(name = "customers")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 会员ID - 用于AI个性化服务
    @Column(name = "member_id", unique = true, length = 50)
    private String memberId;

    @Column(name = "hotel_id", nullable = false)
    private Long hotelId;

    // 基本信息
    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String email;

    // 敏感信息加密存储
    @Column(length = 20)
    private String phone;  // 明文存储，与原有逻辑兼容

    @Column(name = "phone_encrypted", length = 500)
    private String phoneEncrypted;  // 加密存储，用于个性化服务

    @Column(length = 2)
    private String country;

    @Column(name = "id_card_number", length = 50)
    private String idCardNumber;  // 明文存储

    @Column(name = "id_card_encrypted", length = 500)
    private String idCardEncrypted;  // 加密存储

    // 客户状态
    @Column(name = "is_repeated_guest")
    @Builder.Default
    private Boolean isRepeatedGuest = false;

    // 统计数据
    @Column(name = "total_stays")
    @Builder.Default
    private Integer totalStays = 0;

    @Column(name = "total_cancellations")
    @Builder.Default
    private Integer totalCancellations = 0;

    // 住宿信息
    @Column(name = "last_check_in")
    private LocalDate lastCheckIn;

    @Column(name = "last_stay_room", length = 20)
    private String lastStayRoom;

    // 消费信息
    @Column(precision = 10, scale = 2)
    private BigDecimal avgSpend;  // 平均消费

    @Column(name = "total_spend", precision = 12, scale = 2)
    private BigDecimal totalSpend;  // 累计消费

    // VIP等级
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private VipLevel vipLevel = VipLevel.normal;

    // 偏好和标签
    @Column(name = "preference_tags", columnDefinition = "TEXT")
    private String preferenceTags;  // 逗号分隔的偏好标签

    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;  // 逗号分隔的客户标签

    // AI预测模型数据
    @Lob
    @Column(name = "prediction_model_data")
    private String predictionModelData;  // AI预测模型数据

    // 备注
    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 关联关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", insertable = false, updatable = false)
    private Hotel hotel;

    /**
     * VIP等级枚举
     */
    public enum VipLevel {
        normal,    // 普通
        silver,    // 银卡
        gold,      // 金卡
        platinum   // 白金卡
    }

    // 便捷的加密/解密方法
    public void setEncryptedIdCard(String idCard) {
        try {
            this.idCardEncrypted = AESUtil.encrypt(idCard);
        } catch (Exception e) {
            log.error("加密身份证号失败", e);
            this.idCardEncrypted = null;
        }
    }

    public String getDecryptedIdCard() {
        try {
            return idCardEncrypted != null ? AESUtil.decrypt(idCardEncrypted) : null;
        } catch (Exception e) {
            log.error("解密身份证号失败", e);
            return null;
        }
    }

    public void setEncryptedPhone(String phone) {
        try {
            this.phoneEncrypted = AESUtil.encrypt(phone);
        } catch (Exception e) {
            log.error("加密手机号失败", e);
            this.phoneEncrypted = null;
        }
    }

    public String getDecryptedPhone() {
        try {
            return phoneEncrypted != null ? AESUtil.decrypt(phoneEncrypted) : null;
        } catch (Exception e) {
            log.error("解密手机号失败", e);
            return null;
        }
    }
}
