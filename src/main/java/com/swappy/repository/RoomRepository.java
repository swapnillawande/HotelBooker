package com.swappy.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swappy.entities.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

	Optional<Room> findByIdAndHotel_Id(Long roomId, Long hotelId);

}
