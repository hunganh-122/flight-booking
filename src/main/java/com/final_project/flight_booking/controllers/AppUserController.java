package com.final_project.flight_booking.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.final_project.flight_booking.services.OtpService;

@RestController
public class AppUserController {
    @Autowired
    OtpService otpService;

    @PostMapping("api/otp/send")
    public ResponseEntity<Map<String, Object>> sendOtp(@RequestParam String email) {

        return ResponseEntity.ok(otpService.processOtpSending(email));
    }

    @PostMapping("api/otp/verify")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestParam String email, @RequestParam String code) {
        return ResponseEntity.ok(otpService.verifyOtp(email, code));
    }

}
