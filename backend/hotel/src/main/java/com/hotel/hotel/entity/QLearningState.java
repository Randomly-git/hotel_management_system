package com.hotel.hotel.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Q-Learning 状态表 (Q-Table)
 */
@Data
@Entity
@Table(name = "q_learning_state")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QLearningState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hotel_id", nullable = false)
    private Long hotelId;

    @Column(name = "room_type_id", nullable = false)
    private Long roomTypeId;

    @Column(name = "state_key", nullable = false, length = 100)
    private String stateKey; // 状态编码，如 "OCC_HIGH_MON_0"

    @Column(name = "action_0", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal action0 = BigDecimal.ZERO;

    @Column(name = "action_1", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal action1 = BigDecimal.ZERO;

    @Column(name = "action_2", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal action2 = BigDecimal.ZERO;

    @Column(name = "action_3", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal action3 = BigDecimal.ZERO;

    @Column(name = "action_4", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal action4 = BigDecimal.ZERO;

    @Column(name = "action_5", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal action5 = BigDecimal.ZERO;

    @Column(name = "visit_count")
    @Builder.Default
    private Integer visitCount = 0;

    @Column(name = "last_updated")
    @CreationTimestamp
    private LocalDateTime lastUpdated;

    /**
     * 获取指定动作的Q值
     */
    public BigDecimal getQValue(int action) {
        return switch (action) {
            case 0 -> action0;
            case 1 -> action1;
            case 2 -> action2;
            case 3 -> action3;
            case 4 -> action4;
            case 5 -> action5;
            default -> BigDecimal.ZERO;
        };
    }

    /**
     * 设置指定动作的Q值
     */
    public void setQValue(int action, BigDecimal value) {
        switch (action) {
            case 0 -> this.action0 = value;
            case 1 -> this.action1 = value;
            case 2 -> this.action2 = value;
            case 3 -> this.action3 = value;
            case 4 -> this.action4 = value;
            case 5 -> this.action5 = value;
        }
    }

    /**
     * 获取所有Q值
     */
    public BigDecimal[] getAllQValues() {
        return new BigDecimal[]{action0, action1, action2, action3, action4, action5};
    }

    /**
     * 获取最大Q值及其对应动作
     */
    public int getBestAction() {
        BigDecimal[] qValues = getAllQValues();
        int bestAction = 0;
        BigDecimal maxValue = qValues[0];
        for (int i = 1; i < qValues.length; i++) {
            if (qValues[i].compareTo(maxValue) > 0) {
                maxValue = qValues[i];
                bestAction = i;
            }
        }
        return bestAction;
    }

    /**
     * 获取最大Q值
     */
    public BigDecimal getMaxQValue() {
        return getAllQValues()[getBestAction()];
    }
}
