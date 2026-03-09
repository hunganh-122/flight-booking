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

    @Autowired
    private AmadeusService amadeusService;

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

    public Map<Integer, Double> getMinPricesForFlights(List<Flight> flights) {
        Map<Integer, Double> minPrices = new HashMap<>();
        if (flights != null) {
            for (Flight f : flights) {
                if (f.getFlightId() < 0) {
                    minPrices.put(f.getFlightId(), f.getPrice());
                } else if (!minPrices.containsKey(f.getFlightId())) {
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
                if (f.getFlightId() < 0) {
                    availableSeats.put(f.getFlightId(), 99L);
                } else if (!availableSeats.containsKey(f.getFlightId())) {
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

    private List<Flight> mapAmadeusToFlights(com.amadeus.resources.FlightOfferSearch[] offers, Airport origin, Airport destination) {
        java.util.ArrayList<Flight> flights = new java.util.ArrayList<>();
        if (offers == null) return flights;

        for (int i = 0; i < offers.length; i++) {
            com.amadeus.resources.FlightOfferSearch offer = offers[i];
            Flight flight = new Flight();
            flight.setFlightId(-(i + 1000)); 
            
            com.amadeus.resources.FlightOfferSearch.Itinerary itinerary = offer.getItineraries()[0];
            com.amadeus.resources.FlightOfferSearch.SearchSegment firstSegment = itinerary.getSegments()[0];
            com.amadeus.resources.FlightOfferSearch.SearchSegment lastSegment = itinerary.getSegments()[itinerary.getSegments().length - 1];

            flight.setFlightNumber(firstSegment.getCarrierCode() + firstSegment.getNumber());
            flight.setAirline(firstSegment.getCarrierCode());
            flight.setDepartureAirport(origin);
            flight.setArrivalAirport(destination);
            
            flight.setDepartureTime(LocalDateTime.parse(firstSegment.getDeparture().getAt()));
            flight.setArrivalTime(LocalDateTime.parse(lastSegment.getArrival().getAt()));
            
            double priceInEur = Double.parseDouble(offer.getPrice().getTotal());
            double exchangeRate = 30000.0; 
            flight.setPrice(priceInEur * exchangeRate);
            
            flights.add(flight);
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
            flights = new java.util.ArrayList<>();
            departureDate = LocalDate.now().format(inputFormatter);
        } else {
            flights = new java.util.ArrayList<>();
        }

        try {
            LocalDate localDate = LocalDate.parse(departureDate, inputFormatter);
            
            String amadeusDate = localDate.toString();
            com.amadeus.resources.FlightOfferSearch[] amadeusOffers = amadeusService.searchFlights(
                    departureAirport.getAirportCode(), 
                    arrivalAirport.getAirportCode(), 
                    amadeusDate, 1);
            
            flights = mapAmadeusToFlights(amadeusOffers, departureAirport, arrivalAirport);
            
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Please use dd-MM-yyyy.");
        }

        if ("roundtrip".equalsIgnoreCase(tripType)) {
            results.put("tripType", "roundtrip");
            if (!returnDate.isEmpty()) {
                try {
                    LocalDate returnLocalDate = LocalDate.parse(returnDate, inputFormatter);
                    
                    com.amadeus.resources.FlightOfferSearch[] returnOffers = amadeusService.searchFlights(
                            arrivalAirport.getAirportCode(), 
                            departureAirport.getAirportCode(), 
                            returnLocalDate.toString(), 1);
                    returnFlights = mapAmadeusToFlights(returnOffers, arrivalAirport, departureAirport);
                    
                    flights = filterReturnFlights(flights, returnFlights);
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("Invalid date format. Please use dd-MM-yyyy.");
                }
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
