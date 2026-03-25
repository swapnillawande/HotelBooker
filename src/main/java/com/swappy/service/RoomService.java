package com.swappy.service;

import java.util.List;

import com.swappy.dto.RoomDto;

public interface RoomService {

	public RoomDto createNewRoom(Long hotelId, RoomDto roomDto);
	
	public List<RoomDto> getAllRoomsInHotel(Long hotelId);
	
	public RoomDto getRoomById(Long roomId);
	
	public void deleteRoomById(Long roomId);
	
	
}
