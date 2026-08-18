package com.swappy.service;

import java.util.List;

import com.swappy.dto.RoomDto;

public interface RoomService {

	public RoomDto createNewRoom(Long hotelId, RoomDto roomDto, Long ownerId);
	
	public List<RoomDto> getAllRoomsInHotel(Long hotelId, Long ownerId);
	
	public RoomDto getRoomById(Long hotelId, Long roomId, Long ownerId);
	
	public void deleteRoomById(Long hotelId, Long roomId, Long ownerId);
	
	
}
