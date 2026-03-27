package com.swappy.service;

import org.jspecify.annotations.Nullable;

import com.swappy.dto.HotelDto;
import com.swappy.dto.HotelInfoDto;
import com.swappy.entities.Hotel;

public interface HotelService {

	public HotelDto createNewHotel(HotelDto hotelDto);
	
	public HotelDto getHotelById(Long id);
	
	public HotelDto updateHotelById(Long id, HotelDto hotelDto);
	
	public Boolean deleteHotelById(Long id);
	
	public void activateHotel(Long id);

	public HotelInfoDto getHotelInfoById(Long hotelId);
	
}
