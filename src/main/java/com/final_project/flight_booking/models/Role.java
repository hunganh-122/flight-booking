package com.final_project.flight_booking.models;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "roles",
        uniqueConstraints = {
                @UniqueConstraint(name = "APP_ROLE_UK", columnNames = "name") })
@Data
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String description;
}

