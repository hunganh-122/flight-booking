package com.final_project.flight_booking.models;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "refund_requests")
@Data
@EntityListeners(AuditingEntityListener.class)
public class RefundRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer refundId;

    @ManyToOne
    @JoinColumn(name = "bookingId")
    private Booking booking;

    @Column
    @CreatedDate
    private LocalDateTime requestDate;

    @Column(length = 50)
    private String status; // PENDING, APPROVED, REJECTED, PROCESSED

    @Column
    private Double refundAmount;

    @Column(length = 255)
    private String reason;
}

