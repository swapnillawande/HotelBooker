package com.swappy.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.swappy.entities.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long>{

}
