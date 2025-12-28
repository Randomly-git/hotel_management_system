package com.hotel.hotel.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_task")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false, length = 50)
    private String customerId;

    @Column(name = "request_content", columnDefinition = "TEXT")
    private String requestContent;

    @Column(name = "nlp_intent", length = 50)
    private String nlpIntent;

    @Column(name = "nlp_description", columnDefinition = "TEXT")
    private String nlpDescription;

    @Column(name = "nlp_department", length = 50)
    private String nlpDepartment;

    @Column(name = "room_number", length = 20)
    private String roomNumber;

    @Column(name = "hotel_id")
    @Builder.Default
    private Long hotelId = 1L;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "due_time")
    private LocalDateTime dueTime;

    @PrePersist
    protected void onCreate() {
        this.createTime = LocalDateTime.now();
        if (this.status == null) {
            this.status = "PENDING";
        }
        if (this.hotelId == null) {
            this.hotelId = 1L;
        }
    }
}