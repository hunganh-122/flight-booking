package com.final_project.flight_booking.models;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer paymentId;

    @ManyToOne
    @JoinColumn(name = "bookingId")
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "paymethod_id")
    private PayMethod payMethod;

    private LocalDateTime paymentDate;
    private Double amount;

    @Column(length = 50)
    private String paymentStatus;

}

