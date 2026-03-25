package com.swappy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swappy.entities.Hotel;

@Repository
public interface HotelRepository extends  JpaRepository<Hotel, Long> {

}
