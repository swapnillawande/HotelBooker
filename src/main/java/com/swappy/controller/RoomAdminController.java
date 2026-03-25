package com.swappy.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swappy.dto.RoomDto;
import com.swappy.service.RoomService;

@RestController
@RequestMapping("/admin/hotels/{hotelId}/rooms")
public class RoomAdminController {
	
	@Autowired
	private RoomService  roomService;
	
	@PostMapping
	public ResponseEntity<RoomDto> createRoomWithHotelId(@PathVariable("hotelId") Long hotelId, @RequestBody RoomDto roomDto){
		
		RoomDto room = roomService.createNewRoom(hotelId, roomDto);
		
		return new ResponseEntity<>(room, HttpStatus.CREATED);
	}
	
	@GetMapping
	public ResponseEntity<List<RoomDto>> getAllRoomsInHotel(@PathVariable("hotelId") Long hotelId){
		return ResponseEntity.ok(roomService.getAllRoomsInHotel(hotelId));
	}
	
	
	@GetMapping("/{roomId}")
	public ResponseEntity<RoomDto> getRoomById(@PathVariable("roomId") Long hotelId){
		return ResponseEntity.ok(roomService.getRoomById(hotelId));
	}
	
	
	@DeleteMapping("/{roomId}")
	public ResponseEntity<Void> deleteRoomById(@PathVariable("hotelId") Long hotelId, @PathVariable("roomId") Long roomId ){
		roomService.deleteRoomById(roomId);
		return ResponseEntity.noContent().build();
	}

}









