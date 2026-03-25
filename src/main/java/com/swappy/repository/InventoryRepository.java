package com.swappy.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swappy.entities.Inventory;
import com.swappy.entities.Room;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long>{

	public void deleteByDateAfterAndRoom(LocalDate date, Room room);
}
