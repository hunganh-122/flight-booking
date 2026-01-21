package com.final_project.flight_booking.repositories;

import com.final_project.flight_booking.models.Flight;
import com.final_project.flight_booking.models.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer> {

    @Query("SELECT s FROM Seat s WHERE s.flight.flightId = :flightId ORDER BY s.coachNumber, s.seatNumber")
    List<Seat> findSeatsByFlightId(@Param("flightId") Integer flightId);

    @Modifying
    @Transactional
    @Query("UPDATE Seat s SET s.availabilityStatus = :status, s.holdExpiration = :holdExpiration WHERE s.seatId = :seatId")
    void updateSeatStatus(Integer seatId, String status, LocalDateTime holdExpiration);

    @Modifying
    @Transactional
    @Query("UPDATE Seat s SET s.availabilityStatus = :status WHERE s.seatId = :seatId")
    void updateSeatStatusConfig(Integer seatId, String status);

    @Modifying
    @Transactional
    @Query("UPDATE Seat s SET s.availabilityStatus = 'AVAILABLE', s.holdExpiration = NULL WHERE s.availabilityStatus like 'BOOKED%' AND s.holdExpiration < :now")
    void updateSeatsToAvailableIfExpired(LocalDateTime now);

    @Query("SELECT COUNT(s) FROM Seat s WHERE s.flight.flightId = :flightId AND s.availabilityStatus = 'AVAILABLE'")
    long countAvailableSeatsByFlightId(Integer flightId);

    @Query("SELECT MIN(s.price) FROM Seat s WHERE s.flight.flightId = :flightId")
    Double findMinPriceByFlightId(@Param("flightId") Integer flightId);

    @Modifying
    @Transactional
    void deleteAllByFlight_FlightId(Integer flightId);

    @Query("SELECT s FROM Seat s WHERE s.flight.flightId = :flightId AND s.availabilityStatus = CONCAT('BOOKED', :id)")
    List<Seat> findAllSeat(@Param("flightId") int flightId, @Param("id") int id);

}



