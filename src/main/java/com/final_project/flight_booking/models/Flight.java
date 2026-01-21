package com.final_project.flight_booking.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;


@Entity
@Table (name = "flights")
@Data
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer flightId;

    @Column(nullable = false, unique = true, length = 10)
    private String flightNumber;

    @Column(length = 50)
    private String airline;

    @ManyToOne
    @JoinColumn(name = "departureAirportId")
    private Airport departureAirport;

    @ManyToOne
    @JoinColumn(name = "arrivalAirportId")
    private Airport arrivalAirport;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Integer seatCapacity;
    private Double price;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;
}

