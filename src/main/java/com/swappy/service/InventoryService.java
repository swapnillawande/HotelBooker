package com.swappy.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;

import com.swappy.dto.HotelDto;
import com.swappy.dto.HotelSearchDto;
import com.swappy.dto.RoomOfferDto;
import com.swappy.entities.Room;

public interface InventoryService {

	public void initializeRoomForAYear(Room room);
	
	public void deleteFutureInventories(Room room);

	public void deleteInventoriesByRoomId(Long roomId);

	public Page<HotelDto> searchHotels(HotelSearchDto hotelSearchDto);

	public List<RoomOfferDto> getRoomOffers(Long hotelId, LocalDate startDate, LocalDate endDate, Integer roomsCount);
	
}
