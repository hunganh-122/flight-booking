package com.final_project.flight_booking.repositories;

import com.final_project.flight_booking.models.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
    @Query("SELECT o FROM Otp o WHERE o.email = ?1 AND o.code = ?2 AND o.type = ?3 AND o.used = false AND o.expiresAt > ?4")
    Optional<Otp> findValidOtpByEmailAndCodeAndType(String email, String code, String type, LocalDateTime now);

    @Query("SELECT o FROM Otp o WHERE o.email = ?1 AND o.type = ?2 AND o.used = false AND o.expiresAt > ?3 ORDER BY o.createdAt DESC")
    Optional<Otp> findValidOtpByEmailAndType(String email, String type, LocalDateTime now);

    @Modifying
    @Query("UPDATE Otp o SET o.used = true WHERE o.id = ?1")
    void markAsUsed(Long id);

    @Modifying
    @Query("DELETE FROM Otp o WHERE o.expiresAt < ?1")
    void deleteExpiredOtps(LocalDateTime now);
}
