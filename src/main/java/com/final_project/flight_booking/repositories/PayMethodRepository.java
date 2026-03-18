package com.final_project.flight_booking.repositories;

import com.final_project.flight_booking.models.PayMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PayMethodRepository extends JpaRepository<PayMethod, Integer> {
    Optional<PayMethod> findByPaymethodName(String paymethodName);
}
