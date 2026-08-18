package com.swappy.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swappy.dto.RoomDto;
import com.swappy.entities.User;
import com.swappy.service.RoomService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/hotels/{hotelId}/rooms")
@RequiredArgsConstructor
public class RoomAdminController {
	
	private final RoomService roomService;
	
	@PostMapping
	public ResponseEntity<RoomDto> createRoomWithHotelId(
			@PathVariable("hotelId") Long hotelId,
			@Valid @RequestBody RoomDto roomDto,
			@AuthenticationPrincipal User user){
		
		RoomDto room = roomService.createNewRoom(hotelId, roomDto, user.getId());
		
		return new ResponseEntity<>(room, HttpStatus.CREATED);
	}
	
	@GetMapping
	public ResponseEntity<List<RoomDto>> getAllRoomsInHotel(
			@PathVariable("hotelId") Long hotelId,
			@AuthenticationPrincipal User user){
		return ResponseEntity.ok(roomService.getAllRoomsInHotel(hotelId, user.getId()));
	}
	
	
	@GetMapping("/{roomId}")
	public ResponseEntity<RoomDto> getRoomById(@PathVariable("hotelId") Long hotelId,
			@PathVariable("roomId") Long roomId,
			@AuthenticationPrincipal User user){
		return ResponseEntity.ok(roomService.getRoomById(hotelId, roomId, user.getId()));
	}
	
	
	@DeleteMapping("/{roomId}")
	public ResponseEntity<Void> deleteRoomById(
			@PathVariable("hotelId") Long hotelId,
			@PathVariable("roomId") Long roomId,
			@AuthenticationPrincipal User user){
		roomService.deleteRoomById(hotelId, roomId, user.getId());
		return ResponseEntity.noContent().build();
	}

}







