package com.final_project.flight_booking.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Otp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Boolean used = false;

    @Column(nullable = false)
    private String type;

    public Otp(String email, String code, String type) {
        this.email = email;
        this.code = code;
        this.type = type;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(5);
        this.used = false;
    }

    public boolean isValid() {
        return !used && LocalDateTime.now().isBefore(expiresAt);
    }
}

