package com.final_project.flight_booking.controllers;

import com.final_project.flight_booking.services.GroqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private GroqService groqService;

    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        String aiResponse = groqService.chat(userMessage);
        return Map.of("response", aiResponse);
    }
}
