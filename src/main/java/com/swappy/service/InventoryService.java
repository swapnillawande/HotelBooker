package com.swappy.service;

import com.swappy.entities.Room;

public interface InventoryService {

	public void initializeRoomForAYear(Room room);
	
	public void deleteFutureInventories(Room room);
	
	public void deleteInventoriesByRoomId(Long roomId);
	
}
