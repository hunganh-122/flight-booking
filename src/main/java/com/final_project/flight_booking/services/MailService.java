package com.final_project.flight_booking.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtpEmail(String to, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Mã OTP xác thực đăng ký - Flight Booking");
        message.setText("Mã OTP của bạn là: " + otpCode + ". Mã có hiệu lực trong 5 phút.");
        
        mailSender.send(message);
        System.out.println("DEBUG: OTP sent to " + to + " via real email.");
    }
}
