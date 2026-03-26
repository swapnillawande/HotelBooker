package com.swappy.service;

import org.springframework.data.domain.Page;

import com.swappy.dto.HotelDto;
import com.swappy.dto.HotelSearchDto;
import com.swappy.entities.Room;

public interface InventoryService {

	public void initializeRoomForAYear(Room room);
	
	public void deleteFutureInventories(Room room);

	public void deleteInventoriesByRoomId(Long roomId);

	public Page<HotelDto> searchHotels(HotelSearchDto hotelSearchDto);
	
}
