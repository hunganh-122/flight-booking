package com.final_project.flight_booking.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table (name = "pay_method")
@Data
public class PayMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer paymethodId;

    @Column(length = 100)
    private String paymethodName;
}

