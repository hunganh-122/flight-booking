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
    }

    public void sendTicketEmail(String to, String passengerName, String flightInfo, String seats, double amount, String txnRef) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Xác nhận đặt vé thành công - Flight Booking");
        
        String content = String.format(
            "Chào %s,\n\n" +
            "Chúc mừng! Bạn đã đặt vé thành công tại Flight Booking.\n\n" +
            "CHI TIẾT VÉ:\n" +
            "- Mã đặt vé: %s\n" +
            "- Chuyến bay: %s\n" +
            "- Chỗ ngồi: %s\n" +
            "- Tổng tiền: %,.0f VND\n\n" +
            "Cảm ơn bạn đã lựa chọn dịch vụ của chúng tôi.\n" +
            "Chúc bạn có một chuyến bay tốt đẹp!",
            passengerName, txnRef, flightInfo, seats, amount
        );
        
        message.setText(content);
        mailSender.send(message);
        System.out.println("DEBUG: Ticket email sent to " + to);
    }
}
