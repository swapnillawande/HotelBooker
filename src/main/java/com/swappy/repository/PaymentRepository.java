package com.swappy.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.swappy.entities.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByBooking_Id(Long bookingId);
}
