package com.swappy.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.swappy.entities.Guest;

public interface GuestRepository extends JpaRepository<Guest, Long>{

}
