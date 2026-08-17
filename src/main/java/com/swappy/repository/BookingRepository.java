package com.swappy.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.swappy.entities.Booking;
import com.swappy.entities.enums.BookingStatus;

public interface BookingRepository extends JpaRepository<Booking, Long>{

	List<Booking> findByBookingStatusAndCreatedAtBefore(BookingStatus bookingStatus, LocalDateTime createdBefore);

}
