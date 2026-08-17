package com.swappy.service.impl;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.swappy.dto.HotelDto;
import com.swappy.dto.HotelInfoDto;
import com.swappy.dto.RoomDto;
import com.swappy.entities.Hotel;
import com.swappy.entities.Room;
import com.swappy.exception.ResourceNotFoundException;
import com.swappy.repository.HotelRepository;
import com.swappy.service.HotelService;
import com.swappy.service.InventoryService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
	@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService{

	private final HotelRepository hotelRepository;
	
	private final InventoryService inventoryService;
	
	private final ModelMapper modelMapper;
	
	Logger logger = LoggerFactory.getLogger(HotelServiceImpl.class);

	
	public HotelDto createNewHotel(HotelDto hotelDto) {


		logger.info("Creating hotel with name: "+ hotelDto.getName());

		Hotel hotel = modelMapper.map(hotelDto, Hotel.class);
		
		hotel.setIsActive(false);
		
		hotel=  hotelRepository.save(hotel);
		
		logger.info("Created hotel with id: "+ hotel.getId());

		
		return modelMapper.map(hotel, HotelDto.class);
		
		
	}

	@Override
	@Transactional
	public HotelDto getHotelById(Long id) {

		logger.info("Getting hotel with id: "+ id);

		Hotel hotel=  hotelRepository
				.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + id));
		
		return modelMapper.map(hotel, HotelDto.class);
	}

	@Override
	public HotelDto updateHotelById(Long id, HotelDto hotelDto) {
		
		Hotel hotel=  hotelRepository
				.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + id));
		
		logger.info("Updating the hotel with ID: "+id);
		
		modelMapper.map(hotelDto, hotel);
		hotel.setId(id);
		
		hotel = hotelRepository.save(hotel);
		
		logger.info("Updated the hotel with ID: "+id);

		
		return modelMapper.map(hotel, HotelDto.class);
	}

	@Override
	@Transactional
	public Boolean deleteHotelById(Long id) {
		Hotel hotel=  hotelRepository
				.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + id));
		
		logger.info("Deleting the hotel with ID: "+ id);
		if (hotel.getRooms() != null) {
			for (Room room : hotel.getRooms()) {
				inventoryService.deleteFutureInventories(room);
			}
		}
		hotelRepository.delete(hotel);
		logger.info("Deleted the hotel with ID: "+ id);

		return true;
	}
	
	
	public Boolean findHotelById(Long id) {

	    if (!hotelRepository.existsById(id)) {
	        throw new ResourceNotFoundException("Hotel not found with id: " + id);
	    }

	    return true;
	}

	@Override
	@Transactional
	public void activateHotel(Long id) {
		
		logger.info("Activating the hotel with ID: "+id);

		
		Hotel hotel=  hotelRepository
				.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + id));
		
		
		hotel.setIsActive(true);
		
		//assume do only onces
		
		for(Room room: hotel.getRooms()) {
			inventoryService.initializeRoomForAYear(room);
		}
		
		
	}

	@Override
	@Transactional
	public HotelInfoDto getHotelInfoById(Long hotelId) {
		
		logger.info("Getting the hotel with ID: "+hotelId);

		
		Hotel hotel=  hotelRepository
				.findById(hotelId)
				.orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + hotelId));
		
		
		List<RoomDto> rooms = hotel.getRooms()
							  .stream()
							  .map((ele) -> modelMapper.map(ele, RoomDto.class)).toList();
		
		
		
		return new HotelInfoDto(modelMapper.map(hotel, HotelDto.class), rooms);
	}
}





