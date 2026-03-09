package com.final_project.flight_booking.services;

import com.amadeus.Amadeus;
import com.amadeus.Params;
import com.amadeus.resources.FlightOfferSearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

@Service
public class AmadeusService {

    private static final Logger logger = LoggerFactory.getLogger(AmadeusService.class);

    @Value("${amadeus.api.key}")
    private String apiKey;

    @Value("${amadeus.api.secret}")
    private String apiSecret;

    private Amadeus amadeus;

    @PostConstruct
    public void init() {
        this.amadeus = Amadeus
                .builder(apiKey, apiSecret)
                .build();
    }

    /**
     * @param origin 
     * @param destination 
     * @param departureDate 
     * @param adults 
     * @return
     */
    public FlightOfferSearch[] searchFlights(String origin, String destination, String departureDate, int adults) {
        try {
            FlightOfferSearch[] offers = amadeus.shopping.flightOffersSearch.get(
                    Params.with("originLocationCode", origin)
                            .and("destinationLocationCode", destination)
                            .and("departureDate", departureDate)
                            .and("adults", adults)
                            .and("max", 5)
            );
            
            if (offers.length > 0) {
                logger.info("Amadeus API Success: Found {} flight offers.", offers.length);
                logger.debug("Raw Response Body: {}", offers[0].getResponse().getBody());
            } else {
                logger.warn("Amadeus API returned 0 offers for {} to {} on {}", origin, destination, departureDate);
            }
            
            return offers;
        } catch (Exception e) {
            logger.error("Error calling Amadeus API: {}", e.getMessage(), e);
            return new FlightOfferSearch[0];
        }
    }
}
