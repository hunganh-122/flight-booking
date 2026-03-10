package com.final_project.flight_booking.controllers;

import com.final_project.flight_booking.models.User;
import com.final_project.flight_booking.services.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class AppUserController {

    @Autowired
    private AppUserService userService;

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(@ModelAttribute("user") User user) {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, 
                              @RequestParam("otpCode") String otpCode,
                              RedirectAttributes redirectAttributes) {
        try {
            userService.registerNewUser(user, otpCode);
            redirectAttributes.addFlashAttribute("success", "Đăng ký tài khoản thành công!");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đăng ký thất bại: " + e.getMessage());
            return "redirect:/register";
        }
    }

    @PostMapping("/api/check-availability")
    @ResponseBody
    public Map<String, Object> checkAvailability(@RequestParam("username") String username, 
                                                 @RequestParam("email") String email) {
        return userService.checkAvailability(username, email);
    }

    @PostMapping("/api/otp/send")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendOtp(@RequestParam("email") String email) {
        return ResponseEntity.ok(userService.processOtpSending(email));
    }
}
