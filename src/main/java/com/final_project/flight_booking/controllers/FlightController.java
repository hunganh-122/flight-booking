package com.final_project.flight_booking.controllers;

import com.final_project.flight_booking.models.Airport;
import com.final_project.flight_booking.models.Flight;
import com.final_project.flight_booking.services.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
public class FlightController {

    @Autowired
    private FlightService flightService;

    @GetMapping
    public String showSearchForm(Model model) {
        List<Airport> airports = flightService.getDistinctDepartureAirportsWithFutureFlights();
        List<Flight> flights = flightService.findAllFlightsByCurrentDateTime();
        model.addAttribute("airports", airports);

        Map<Integer, Double> minPrices = flightService.getMinPricesForFlights(flights);
        Map<Integer, Long> availableSeats = flightService.getAvailableSeatsForFlights(flights);

        model.addAttribute("flights", flights);
        model.addAttribute("minPrices", minPrices);
        model.addAttribute("availableSeats", availableSeats);
        
        return "flight/home";
    }

    @PostMapping("/flights/search")
    public String searchFlights(@RequestParam(value = "departureAirportId", required = false) Integer departureAirportId,
                                @RequestParam(value = "arrivalAirportId", required = false) Integer arrivalAirportId,
                                @RequestParam(value = "departure-date", defaultValue = "") String departureDate,
                                @RequestParam(value = "tripType", defaultValue = "oneway") String tripType,
                                @RequestParam(value = "return-date", defaultValue = "") String returnDate,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        if (departureAirportId == null) {
            redirectAttributes.addFlashAttribute("messageWarning", "Vui lòng chọn điểm khởi hành!");
            return "redirect:/";
        }
        if (arrivalAirportId == null) {
            redirectAttributes.addFlashAttribute("messageWarning", "Vui lòng chọn điểm đến!");
            return "redirect:/";
        }

        Map<String, Object> searchResults = flightService.processFlightSearch(departureAirportId, arrivalAirportId, departureDate, tripType, returnDate);
        model.addAllAttributes(searchResults);

        return "flight/list";
    }

    @GetMapping("/introduction")
    public String introduction() {
        return "flight/introduction";
    }
}
