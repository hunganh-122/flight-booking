package com.final_project.flight_booking.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table (name = "order_details")
@Data
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderId;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

    @ManyToOne
    @JoinColumn(name = "bookingId")
    private Booking booking;
}

