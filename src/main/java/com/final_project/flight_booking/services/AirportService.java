package com.final_project.flight_booking.services;

import com.final_project.flight_booking.models.Airport;
import com.final_project.flight_booking.repositories.AirportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AirportService {

    @Autowired
    private AirportRepository airportRepository;

    public List<Airport> findAll() {
        return airportRepository.findAll();
    }

    public Airport findById(Integer AirportId) {
        return airportRepository.findById(AirportId).orElse(null);
    }

    public Airport findByAirportCode(String airportCode) {
        return airportRepository.findByAirportCode(airportCode);
    }
}
