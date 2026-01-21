package com.final_project.flight_booking.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table (name = "users_roles")
@Data
public class UserRole {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

}

