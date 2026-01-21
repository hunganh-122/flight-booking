package com.final_project.flight_booking.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "APP_USER_UK", columnNames = "username") })
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String fullname;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, length = 200)
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 200)
    private String address;

    @Column(length = 12)
    private String phone;

    @Column(length = 200)
    private String avatar;

    @Column(nullable = false)
    private Boolean activated = true;

    @Column(length = 200)
    private String rememberToken;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private Set<UserRole> roles;
}

