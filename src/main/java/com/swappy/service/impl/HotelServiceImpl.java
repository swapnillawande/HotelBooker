package com.swappy.service.impl;

import org.apache.commons.logging.Log;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swappy.config.MapperConfig;
import com.swappy.dto.HotelDto;
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

	@Autowired
	private HotelRepository hotelRepository; 
	
	@Autowired
	private InventoryService inventoryService;
	
	@Autowired
	private ModelMapper modelMapper;
	
	Logger logger = LoggerFactory.getLogger(HotelServiceImpl.class);

	
	public HotelDto createNewHotel(HotelDto hotelDto) {


		logger.info("Creating hotel with name: "+ hotelDto.getName());

		Hotel hotel = modelMapper.map(hotelDto, Hotel.class);
		
		hotel.setIsActive(false);
		
		hotel=  hotelRepository.save(hotel);
		
		logger.info("Created hotel with id: "+ hotelDto.getId());

		
		return modelMapper.map(hotel, HotelDto.class);
		
		
	}

	@Override
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
		
		hotel = modelMapper.map(hotelDto, Hotel.class);
		
		hotelRepository.save(hotel);
		
		logger.info("Updated the hotel with ID: "+id);

		
		return modelMapper.map(hotel, HotelDto.class);
	}

	@Override
	@Transactional
	public Boolean deleteHotelById(Long id) {
		// TODO Auto-generated method stub
		
		Hotel hotel=  hotelRepository
				.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + id));
		
		if (findHotelById(id)) {
			logger.info("Deleting the hotel with ID: "+ id);
			hotelRepository.deleteById(id);
			logger.info("Deleted the hotel with ID: "+ id);
			
			
			
			for(Room room: hotel.getRooms()) {
				inventoryService.deleteFutureInventories(room);
			}

		}
		logger.info("Hotel not found with ID: "+ id);

		return false;
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
}








