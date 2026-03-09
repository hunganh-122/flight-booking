package com.final_project.flight_booking.services;

import com.final_project.flight_booking.models.Otp;
import com.final_project.flight_booking.repositories.OtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private OtpRepository otpRepository;

    private static final String OTP_TYPE_REGISTRATION = "REGISTRATION";
    private static final int OTP_LENGTH = 6;

    @Transactional
    public String generateOtpForRegistration(String email) {
        otpRepository.deleteExpiredOtps(LocalDateTime.now());
        
        String otpCode = generateRandomOtp();
        
        Otp otp = new Otp(email, otpCode, OTP_TYPE_REGISTRATION);
        otpRepository.save(otp);
        
        return otpCode;
    }

    public boolean checkOtpValid(String email, String code) {
        Optional<Otp> otpOptional = otpRepository.findValidOtpByEmailAndCodeAndType(
            email, code, OTP_TYPE_REGISTRATION, LocalDateTime.now()
        );
        return otpOptional.isPresent();
    }

    @Transactional
    public boolean verifyOtp(String email, String code) {
        Optional<Otp> otpOptional = otpRepository.findValidOtpByEmailAndCodeAndType(
            email, code, OTP_TYPE_REGISTRATION, LocalDateTime.now()
        );
        
        if (otpOptional.isPresent()) {
            Otp otp = otpOptional.get();
            otpRepository.markAsUsed(otp.getId());
            return true;
        }
        
        return false;
    }

    private String generateRandomOtp() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        
        return otp.toString();
    }
}
