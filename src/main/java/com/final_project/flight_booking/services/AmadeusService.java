package com.final_project.flight_booking.services;

import com.amadeus.Amadeus;
import com.amadeus.Params;
import com.amadeus.resources.FlightOfferSearch;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AmadeusService {

    private static final Logger logger = LoggerFactory.getLogger(AmadeusService.class);

    private final Map<Integer, FlightOfferSearch> offerCache = new ConcurrentHashMap<>();

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

    public void cacheOffer(int flightId, FlightOfferSearch offer) {
        offerCache.put(flightId, offer);
        logger.info("Cached FlightOfferSearch for flightId={}. Cache now has {} entries: {}",
                flightId, offerCache.size(), offerCache.keySet());
    }

    public String getSeatMapJson(int flightId) {
        logger.info("getSeatMapJson called for flightId={}. Cache size={}, keys={}",
                flightId, offerCache.size(), offerCache.keySet());

        FlightOfferSearch offer = offerCache.get(flightId);
        if (offer == null) {
            logger.warn("Cache MISS for flightId={}. Available keys: {}", flightId, offerCache.keySet());
            return null;
        }

        logger.info("Cache HIT for flightId={}. Calling Amadeus Seat Map API...", flightId);
        try {
            Gson gson = new Gson();
            String rawOfferBody = offer.getResponse().getBody();
            logger.debug("Raw offer body snippet: {}",
                    rawOfferBody != null ? rawOfferBody.substring(0, Math.min(200, rawOfferBody.length())) : "null");

            com.google.gson.JsonElement jsonElement = gson.toJsonTree(offer);
            JsonArray array = new JsonArray();
            array.add(jsonElement);
            String requestBody = "{\"data\":[" + gson.toJson(jsonElement) + "]}";
            logger.debug("Seat map request body snippet: {}", requestBody.substring(0, Math.min(300, requestBody.length())));

            com.amadeus.resources.SeatMap[] response = amadeus.shopping.seatMaps.post(requestBody);
            logger.info("Amadeus SeatMaps response: {} entries", response != null ? response.length : 0);
            if (response != null && response.length > 0) {
                String body = response[0].getResponse().getBody();
                logger.debug("Seat map response snippet: {}",
                        body != null ? body.substring(0, Math.min(300, body.length())) : "null");
                return body;
            }
            return null;
        } catch (Exception e) {
            logger.error("Error fetching seat map for flight {}: {} — {}", flightId, e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }
}
