package com.final_project.flight_booking.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table (name = "airports")
@Data
public class Airport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer airportId;

    @Column(nullable = false, unique = true, length = 5)
    private String airportCode;

    @Column(length = 100,nullable = false)
    private String airportName;

    @Column(length = 50,nullable = false)
    private String city;

    @Column(length = 50,nullable = false)
    private String country;
}


