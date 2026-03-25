package com.swappy.service;

import com.swappy.dto.HotelDto;
import com.swappy.entities.Hotel;

public interface HotelService {

	public HotelDto createNewHotel(HotelDto hotelDto);
	
	public HotelDto getHotelById(Long id);
	
	public HotelDto updateHotelById(Long id, HotelDto hotelDto);
	
	public Boolean deleteHotelById(Long id);
	
	public void activateHotel(Long id);
	
}
