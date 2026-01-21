package com.final_project.flight_booking.controllers;

import com.final_project.flight_booking.models.Airport;
import com.final_project.flight_booking.services.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/airports")
public class AirportApiController {

    @Autowired
    private FlightService flightService;

    @GetMapping("/arrival")
    public List<Airport> getArrivalAirports(@RequestParam Integer departureAirportId) {
        return flightService.getArrivalAirportsByDeparture(departureAirportId, LocalDateTime.now());
    }
}
