package com.swappy.service.impl;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swappy.dto.RoomDto;
import com.swappy.entities.Hotel;
import com.swappy.entities.Room;
import com.swappy.exception.ResourceNotFoundException;
import com.swappy.repository.HotelRepository;
import com.swappy.repository.RoomRepository;
import com.swappy.service.InventoryService;
import com.swappy.service.RoomService;

import jakarta.transaction.Transactional;

@Service
public class RoomServiceImpl implements RoomService{

	@Autowired
	private RoomRepository roomRepository;
	
	@Autowired
	private HotelRepository hotelRepository; 
	
	@Autowired
	private InventoryService inventoryService;
	
	@Autowired
	private ModelMapper modelMapper;
	
	Logger logger = LoggerFactory.getLogger(HotelServiceImpl.class);
	
	@Override
	public RoomDto createNewRoom(Long hotelId, RoomDto roomDto) {
		
		logger.info("Creating room..");
		
		Hotel hotel=  hotelRepository
				.findById(hotelId)
				.orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + hotelId));
		
		Room room = modelMapper.map(roomDto, Room.class);
		
		room.setHotel(hotel);
		
		room = roomRepository.save(room);
		
		logger.info("Room created for hotel: "+ hotel.getName() + " with room id: "+ room.getId());

		if(hotel.getIsActive()) {
			inventoryService.initializeRoomForAYear(room);
		}
		
		
		return modelMapper.map(room, RoomDto.class);
	}

	@Override
	public List<RoomDto> getAllRoomsInHotel(Long hotelId) {
		
		logger.info("Getting all rooms in hotel with ID: "+hotelId);
		
		Hotel hotel=  hotelRepository
				.findById(hotelId)
				.orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + hotelId));
		
	    return hotel.getRooms()
	            .stream()
	            .map(room -> modelMapper.map(room, RoomDto.class))
	            .toList();
	}

	@Override
	public RoomDto getRoomById(Long roomId) {
		
		logger.info("Getting rooms with ID: "+ roomId);

		
		Room room = roomRepository.findById(roomId)
					  .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));

		
		return modelMapper.map(room, RoomDto.class);
	}

	@Override
	@Transactional
	public void deleteRoomById(Long roomId) {
		
		Room room = roomRepository.findById(roomId)
				  .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));
		

		inventoryService.deleteInventoriesByRoomId(roomId);
		roomRepository.flush();
		

		roomRepository.delete(room);

		logger.info("Room deleted with ID: "+ roomId);

	}

}
