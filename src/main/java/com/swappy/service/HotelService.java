package com.swappy.service;

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.swappy.dto.HotelDto;
import com.swappy.dto.HotelInfoDto;
import com.swappy.entities.Hotel;

public interface HotelService {

	public HotelDto createNewHotel(HotelDto hotelDto, Long ownerId);

	public List<HotelDto> getHotelsForOwner(Long ownerId);
	
	public HotelDto getHotelById(Long id, Long ownerId);
	
	public HotelDto updateHotelById(Long id, HotelDto hotelDto, Long ownerId);
	
	public Boolean deleteHotelById(Long id, Long ownerId);
	
	public void activateHotel(Long id, Long ownerId);

	public HotelInfoDto getHotelInfoById(Long hotelId);
	
}
