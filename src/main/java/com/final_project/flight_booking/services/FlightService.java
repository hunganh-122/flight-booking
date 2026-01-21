package com.final_project.flight_booking.services;

import com.final_project.flight_booking.models.Airport;
import com.final_project.flight_booking.models.Flight;
import com.final_project.flight_booking.repositories.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FlightService {

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private SeatService seatService;

    @Autowired
    private AirportService airportService;

    public Flight getFlightById(Integer flightId) {
        return flightRepository.findById(flightId).orElse(null);
    }

    public Flight findById(int flightId) {
        return flightRepository.findById(flightId).orElse(null);
    }

    public List<Flight> findAllFlights(LocalDate localDate, Integer arrivalAirportId, Integer departureAirportId) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        return flightRepository.findFlights(localDate, arrivalAirportId, departureAirportId, currentDateTime);
    }

    public List<Flight> findAllFlightsByDateOnly(LocalDate localDate, Integer arrivalAirportId, Integer departureAirportId) {
        return flightRepository.findFlightsByDate(localDate, arrivalAirportId, departureAirportId);
    }

    public List<Flight> findAllFlightsByCurrentDateTime() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        return flightRepository.findFlightsAfterCurrentDateTime(currentDateTime);
    }

    public List<Airport> getDistinctDepartureAirportsWithFutureFlights() {
        LocalDateTime currentTime = LocalDateTime.now();
        return flightRepository.findDistinctDepartureAirportsWithFutureFlights(currentTime);
    }

    public List<Airport> getArrivalAirportsByDeparture(Integer departureAirportId, LocalDateTime currentTime) {
        return flightRepository.findArrivalAirportsByDeparture(departureAirportId, currentTime);
    }

    public List<Flight> findAllFlightsByCurrentDate(Integer departureAirportId, Integer arrivalAirportId) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        return flightRepository.findAllFlightByCurrentDateAndAirportId(departureAirportId, arrivalAirportId, currentDateTime);
    }

    public void saveUserId(Integer flightId, int id) {
        flightRepository.updateUserForFlight(flightId, id);
    }

    public void removeUserId(int flightId) {
        flightRepository.removeUserFromFlight(flightId);
    }

    // New methods moved from Controller

    public Map<Integer, Double> getMinPricesForFlights(List<Flight> flights) {
        Map<Integer, Double> minPrices = new HashMap<>();
        if (flights != null) {
            for (Flight f : flights) {
                if (!minPrices.containsKey(f.getFlightId())) {
                    Double min = seatService.findMinPriceByFlightId(f.getFlightId());
                    minPrices.put(f.getFlightId(), min);
                }
            }
        }
        return minPrices;
    }

    public Map<Integer, Long> getAvailableSeatsForFlights(List<Flight> flights) {
        Map<Integer, Long> availableSeats = new HashMap<>();
        if (flights != null) {
            for (Flight f : flights) {
                if (!availableSeats.containsKey(f.getFlightId())) {
                    long avail = seatService.countAvailableSeatsByFlightId(f.getFlightId());
                    availableSeats.put(f.getFlightId(), avail);
                }
            }
        }
        return availableSeats;
    }

    public List<Flight> filterReturnFlights(List<Flight> flights, List<Flight> returnFlights) {
         if (returnFlights != null && !returnFlights.isEmpty() && flights != null && !flights.isEmpty()) {
            Optional<LocalDateTime> earliestReturnDepOpt = returnFlights.stream()
                    .map(Flight::getDepartureTime)
                    .min(LocalDateTime::compareTo);
            if (earliestReturnDepOpt.isPresent()) {
                LocalDateTime earliestReturnDep = earliestReturnDepOpt.get();
                return flights.stream()
                        .filter(f -> f.getArrivalTime() != null && !f.getArrivalTime().isAfter(earliestReturnDep))
                        .collect(Collectors.toList());
            }
        }
        return flights;
    }

    public Map<String, Object> processFlightSearch(Integer departureAirportId, Integer arrivalAirportId, String departureDate, String tripType, String returnDate) {
        Map<String, Object> results = new HashMap<>();
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        
        Airport departureAirport = airportService.findById(departureAirportId);
        Airport arrivalAirport = airportService.findById(arrivalAirportId);
        
        List<Flight> flights;
        List<Flight> returnFlights = null;

        if (departureDate.isEmpty()) {
            flights = findAllFlightsByCurrentDate(departureAirportId, arrivalAirportId);
        } else {
            try {
                LocalDate localDate = LocalDate.parse(departureDate, inputFormatter);
                flights = findAllFlightsByDateOnly(localDate, arrivalAirportId, departureAirportId);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date format. Please use dd-MM-yyyy.");
            }
        }

        if ("roundtrip".equalsIgnoreCase(tripType)) {
            results.put("tripType", "roundtrip");
            if (!returnDate.isEmpty()) {
                try {
                    LocalDate returnLocalDate = LocalDate.parse(returnDate, inputFormatter);
                    returnFlights = findAllFlightsByDateOnly(returnLocalDate, departureAirportId, arrivalAirportId);
                    flights = filterReturnFlights(flights, returnFlights);
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("Invalid date format. Please use dd-MM-yyyy.");
                }
            } else {
                returnFlights = findAllFlightsByCurrentDate(arrivalAirportId, departureAirportId);
            }
        } else {
            results.put("tripType", "oneway");
        }

        Map<Integer, Double> minPrices = getMinPricesForFlights(flights);
        Map<Integer, Long> availableSeats = getAvailableSeatsForFlights(flights);

        if (returnFlights != null) {
            minPrices.putAll(getMinPricesForFlights(returnFlights));
            availableSeats.putAll(getAvailableSeatsForFlights(returnFlights));
        }

        results.put("flights", flights);
        results.put("minPrices", minPrices);
        results.put("availableSeats", availableSeats);
        results.put("departureAirport", departureAirport);
        results.put("arrivalAirport", arrivalAirport);
        results.put("departureDate", departureDate);
        results.put("returnFlights", returnFlights);
        results.put("returnDate", returnDate);

        return results;
    }
}
