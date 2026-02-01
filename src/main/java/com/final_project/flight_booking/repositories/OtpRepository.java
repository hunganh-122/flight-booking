package com.final_project.flight_booking.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import com.final_project.flight_booking.models.Otp;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
    @Query("SELECT o FROM Otp o WHERE o.email = :email AND o.code = :code AND o.type = :type AND o.used = false AND o.expiresAt > :now")
    Optional<Otp> findTopByEmailAndTypeOrderByCreatedAtDesc(@Param("email") String email, @Param("code") String code,
            @Param("type") String type, @Param("now") LocalDateTime now);
}
