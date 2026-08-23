package com.swappy.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.swappy.entities.Booking;
import com.swappy.entities.enums.BookingStatus;

public interface BookingRepository extends JpaRepository<Booking, Long>{

	List<Booking> findByBookingStatusInAndCreatedAtBefore(List<BookingStatus> bookingStatuses, LocalDateTime createdBefore);

	@Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT b FROM Booking b WHERE b.id = :id")
	Optional<Booking> findByIdForUpdate(@Param("id") Long id);

	List<Booking> findByUser_IdOrderByCreatedAtDesc(Long userId);

	List<Booking> findByHotel_Owner_IdOrderByCheckInDateAsc(Long ownerId);

}
