package com.final_project.flight_booking.controllers;

import com.final_project.flight_booking.services.AmadeusService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class SeatController {

    private static final Logger logger = LoggerFactory.getLogger(SeatController.class);

    @Autowired
    private AmadeusService amadeusService;

    @GetMapping(value = "/flights/{flightId}/seats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getSeatMap(@PathVariable("flightId") Integer flightId) {
        logger.info("Seat map request for flightId={}", flightId);

        String jsonRaw = amadeusService.getSeatMapJson(flightId);
        logger.debug("Raw Amadeus seat map JSON for flightId={}: {}", flightId,
                jsonRaw != null ? jsonRaw.substring(0, Math.min(300, jsonRaw.length())) : "null");

        if (jsonRaw == null || jsonRaw.trim().isEmpty()) {
            logger.warn("No seat map data returned from Amadeus for flightId={}", flightId);
            return ResponseEntity.ok(buildFallbackSeats());
        }

        try {
            List<Map<String, Object>> sections = parseAmadeusSeats(jsonRaw);
            logger.info("Parsed {} sections/decks for flightId={}", sections.size(), flightId);
            return ResponseEntity.ok(sections);
        } catch (Exception e) {
            logger.error("Error parsing seat map for flightId={}: {}", flightId, e.getMessage(), e);
            return ResponseEntity.ok(buildFallbackSeats());
        }
    }

    private List<Map<String, Object>> parseAmadeusSeats(String jsonRaw) {
        Gson gson = new Gson();
        JsonObject root = gson.fromJson(jsonRaw, JsonObject.class);
        List<Map<String, Object>> sections = new ArrayList<>();

        if (root == null || !root.has("data")) {
            logger.warn("Amadeus response missing 'data' field");
            return sections;
        }

        JsonArray dataArr = root.getAsJsonArray("data");
        if (dataArr.size() == 0) {
            logger.warn("Amadeus 'data' array is empty");
            return sections;
        }

        JsonObject firstOffer = dataArr.get(0).getAsJsonObject();
        logger.debug("First offer keys: {}", firstOffer.keySet());

        JsonArray decks = firstOffer.getAsJsonArray("decks");
        if (decks == null || decks.size() == 0) {
            logger.warn("No 'decks' found in Amadeus seat map response");
            return sections;
        }

        logger.info("Found {} deck(s)", decks.size());

        int seatCounter = 100001;
        for (int d = 0; d < decks.size(); d++) {
            JsonObject deck = decks.get(d).getAsJsonObject();
            JsonArray seatsJson = deck.getAsJsonArray("seats");

            if (seatsJson == null) {
                logger.warn("Deck {} has no 'seats' array", d);
                continue;
            }

            logger.info("Deck {}: {} seats", d, seatsJson.size());

            List<Map<String, Object>> seatList = new ArrayList<>();
            for (JsonElement se : seatsJson) {
                JsonObject sObj = se.getAsJsonObject();
                Map<String, Object> seat = new HashMap<>();
                seat.put("id", seatCounter++);
                seat.put("number", sObj.has("number") ? sObj.get("number").getAsString() : "?");
                seat.put("classType", sObj.has("cabin") ? sObj.get("cabin").getAsString() : "Economy");
                seat.put("status", "UNAVAILABLE");
                seat.put("price", 1000000.0);

                if (sObj.has("travelerPricing")) {
                    JsonArray tp = sObj.getAsJsonArray("travelerPricing");
                    if (tp != null && tp.size() > 0) {
                        JsonObject t0 = tp.get(0).getAsJsonObject();
                        if (t0.has("seatAvailabilityStatus")) {
                            seat.put("status", t0.get("seatAvailabilityStatus").getAsString());
                        }
                        if (t0.has("price") && t0.getAsJsonObject("price").has("total")) {
                            double eur = t0.getAsJsonObject("price").get("total").getAsDouble();
                            seat.put("price", Math.round(eur * 30000.0));
                        }
                    }
                }
                seatList.add(seat);
            }

            Map<String, Object> section = new LinkedHashMap<>();
            section.put("name", "Khoang " + (d + 1));
            section.put("seats", seatList);
            sections.add(section);
        }
        return sections;
    }

    private List<Map<String, Object>> buildFallbackSeats() {
        List<Map<String, Object>> sections = new ArrayList<>();
        String[] rows = {"A","B","C","D","E","F"};
        for (int sec = 1; sec <= 2; sec++) {
            List<Map<String, Object>> seatList = new ArrayList<>();
            for (int r = 1; r <= 5; r++) {
                for (String col : rows) {
                    Map<String, Object> seat = new HashMap<>();
                    seat.put("id", sec * 1000 + r * 10 + col.charAt(0));
                    seat.put("number", r + col);
                    seat.put("classType", sec == 1 ? "Business" : "Economy");
                    seat.put("status", "AVAILABLE");
                    seat.put("price", sec == 1 ? 3000000L : 1000000L);
                    seatList.add(seat);
                }
            }
            Map<String, Object> section = new LinkedHashMap<>();
            section.put("name", "Khoang " + sec);
            section.put("seats", seatList);
            sections.add(section);
        }
        return sections;
    }
}
