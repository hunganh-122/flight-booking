package com.final_project.flight_booking.services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.final_project.flight_booking.models.Otp;
import com.final_project.flight_booking.repositories.OtpRepository;

import jakarta.transaction.Transactional;

@Service
public class OtpService {
    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final String OTP_TYPE = "REGISTRATION";

    public Map<String, Object> processOtpSending(String email) {
        Map<String, Object> response = new HashMap<>();
        try {
            String otpCode = genOtpForRegistration(email);
            sendEmail(email, "Mã xác thực đăng ký", "Mã OTP của bạn là: " + otpCode + ". Mã có hiệu lực trong 5 phút.");

            response.put("status", "success");
            response.put("message", "Mã OTP đã được gửi thành công đến email: " + email);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Gửi mã OTP thất bại: " + e.getMessage());
            return response;
        }
    }

    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    @Transactional
    private String genOtpForRegistration(String email) {
        String otpCode = genRandomOtp();
        Otp otp = new Otp(email, otpCode, OTP_TYPE);
        otpRepository.save(otp);
        return otpCode;
    }

    private String genRandomOtp() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int digit = random.nextInt(10);
            otp.append(digit);
        }
        return otp.toString();
    }

    public Map<String, Object> verifyOtp(String email, String code) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<Otp> otpOptional = otpRepository.findTopByEmailAndTypeOrderByCreatedAtDesc(email, code, OTP_TYPE,
                    LocalDateTime.now());
            if (otpOptional.isPresent()) {
                Otp otp = otpOptional.get();
                if (otp.isValid() && otp.getCode().equals(code)) {
                    otp.setUsed(true);
                    otpRepository.save(otp);
                    response.put("status", "success");
                    response.put("message", "Xác thực OTP thành công.");
                    return response;
                }
            }
            response.put("status", "error");
            response.put("message", "Mã OTP không chính xác hoặc đã hết hạn.");
            return response;
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Xác thực OTP thất bại: " + e.getMessage());
            return response;
        }
    }

}
