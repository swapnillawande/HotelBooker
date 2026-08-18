package com.swappy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swappy.entities.Hotel;

@Repository
public interface HotelRepository extends  JpaRepository<Hotel, Long> {

	Optional<Hotel> findByIdAndOwner_Id(Long hotelId, Long ownerId);

	List<Hotel> findByOwner_IdOrderByCreatedAtDesc(Long ownerId);

}
