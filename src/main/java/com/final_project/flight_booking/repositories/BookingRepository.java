package com.final_project.flight_booking.repositories;

import com.final_project.flight_booking.models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findByEmail(String email);
    List<Booking> findByUser_Id(Integer userId);
}
