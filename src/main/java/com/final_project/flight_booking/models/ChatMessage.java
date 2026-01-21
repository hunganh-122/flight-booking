package com.final_project.flight_booking.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;
    
    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isRead = false;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isAdminMessage = false;
    
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}

