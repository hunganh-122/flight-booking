package com.final_project.flight_booking.services;

import com.final_project.flight_booking.models.Role;
import com.final_project.flight_booking.models.User;
import com.final_project.flight_booking.models.UserRole;
import com.final_project.flight_booking.repositories.AppUserRepository;
import com.final_project.flight_booking.repositories.RoleRepository;
import com.final_project.flight_booking.repositories.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.HashMap;

@Service
public class AppUserService {

    private static final Logger logger = LoggerFactory.getLogger(AppUserService.class);

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private OtpService otpService;

    @Autowired
    private MailService mailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public void registerNewUser(User user, String otpCode) {
        if (otpCode == null || otpCode.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập mã OTP!");
        }

        boolean isValidOtp = otpService.verifyOtp(user.getEmail(), otpCode);
        if (!isValidOtp) {
            throw new IllegalArgumentException("Mã OTP không hợp lệ hoặc đã hết hạn!");
        }

        if (user.getFullname() == null || user.getFullname().isEmpty()) {
            throw new IllegalArgumentException("Họ tên không được để trống!");
        }

        logger.info("Starting user registration for email: {}", user.getEmail());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActivated(true);

        User savedUser = userRepository.save(user);
        
        Role role = roleRepository.findByName("ROLE_CUSTOMER");
        if (role == null) {
            role = new Role();
            role.setName("ROLE_CUSTOMER");
            role.setDescription("Default customer role");
            role = roleRepository.save(role);
        }

        UserRole userRole = new UserRole();
        userRole.setUser(savedUser);
        userRole.setRole(role);
        userRoleRepository.save(userRole);
        
        logger.info("User registered successfully: {}", savedUser.getUsername());
    }

    public Map<String, Object> checkAvailability(String username, String email) {
        Map<String, Object> response = new HashMap<>();
        boolean usernameExists = userRepository.findByUsername(username) != null;
        boolean emailExists = userRepository.findByEmail(email) != null;

        boolean isAvailable = !usernameExists && !emailExists;
        response.put("success", isAvailable);

        if (!isAvailable) {
            if (usernameExists && emailExists) {
                response.put("message", "Tên đăng nhập và email đã tồn tại!");
                response.put("field", "both");
            } else if (usernameExists) {
                response.put("message", "Tên đăng nhập đã tồn tại!");
                response.put("field", "username");
            } else {
                response.put("message", "Email đã tồn tại!");
                response.put("field", "email");
            }
        }
        return response;
    }

    public Map<String, Object> processOtpSending(String email) {
        Map<String, Object> response = new HashMap<>();
        try {
            String otpCode = otpService.generateOtpForRegistration(email);
            mailService.sendOtpEmail(email, otpCode);
            response.put("success", true);
            response.put("message", "Mã OTP đã được gửi đến email của bạn.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Không thể gửi mã OTP. Vui lòng thử lại sau.");
        }
        return response;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
