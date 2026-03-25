package com.swappy.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swappy.entities.Inventory;
import com.swappy.entities.Room;
import com.swappy.repository.InventoryRepository;
import com.swappy.service.InventoryService;

@Service
public class InventoryServiceImpl implements InventoryService{

	@Autowired
	private InventoryRepository inventoryRepository;
	
	@Override
	public void initializeRoomForAYear(Room room) {
		
		LocalDate today = LocalDate.now();
		LocalDate enddate = today.plusYears(1);
		
		
		for(;!today.isAfter(enddate); today =today.plusDays(1)) {
			boolean inventoryAlreadyExists = inventoryRepository.existsByHotelIdAndRoomIdAndDate(room.getHotel().getId(), room.getId(), today);
			
			if (inventoryAlreadyExists) {
				continue;
			}
			
			Inventory inventory = new Inventory();
			inventory.setHotel(room.getHotel());
			inventory.setRoom(room);
			inventory.setBookedCount(0);
			inventory.setCity(room.getHotel().getCity());
			inventory.setDate(today);
			inventory.setPrice(room.getBasePrice());
			inventory.setSurgeFactor(BigDecimal.ONE);
			inventory.setTotalCount(room.getTotalCount());
			inventory.setClosed(false);
			
			inventoryRepository.save(inventory);
			
			
		}
		
	}

	@Override
	public void deleteFutureInventories(Room room) {
		LocalDate today = LocalDate.now();
		
		inventoryRepository.deleteByDateAfterAndRoom(today, room);
		
		
	}
	
	
	@Override
	public void deleteInventoriesByRoomId(Long roomId) {
		inventoryRepository.deleteByRoomId(roomId);
		inventoryRepository.flush();
	}

	
}







