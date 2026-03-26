package com.swappy.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.swappy.dto.HotelDto;
import com.swappy.dto.HotelSearchDto;
import com.swappy.entities.Hotel;
import com.swappy.entities.Inventory;
import com.swappy.entities.Room;
import com.swappy.repository.InventoryRepository;
import com.swappy.service.InventoryService;

@Service
public class InventoryServiceImpl implements InventoryService{

	@Autowired
	private InventoryRepository inventoryRepository;
	
	@Autowired
	private ModelMapper modelMapper; 
	
	@Override
	public void initializeRoomForAYear(Room room) {
		
		LocalDate today = LocalDate.now();
		LocalDate enddate = today.plusYears(1);
		
		
		for(;!today.isAfter(enddate); today =today.plusDays(1)) {
			boolean inventoryAlreadyExists = inventoryRepository
					.existsByHotelIdAndRoomIdAndDate(room.getHotel().getId(), room.getId(), today);
			
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
		inventoryRepository.deleteByRoom_Id(roomId);
		inventoryRepository.flush();
	}

	@Override
	public Page<HotelDto> searchHotels(HotelSearchDto hotelSearchDto) {
		
		Pageable pageable = PageRequest.of(hotelSearchDto.getPage(), hotelSearchDto.getSize());
		
		long dateCount = ChronoUnit.DAYS.between(
		        hotelSearchDto.getStartDate(),
		        hotelSearchDto.getEndDate()
		) + 1;
		
		Page<Hotel> hotelPage = inventoryRepository.findHotelsWithAvailableInventory(
				hotelSearchDto.getCity(), 
				hotelSearchDto.getStartDate(), 
				hotelSearchDto.getEndDate(), 
				hotelSearchDto.getRoomCount(), 
				dateCount, pageable);
		
		return hotelPage.map((ele) -> modelMapper.map(ele, HotelDto.class));
	}

	
}







