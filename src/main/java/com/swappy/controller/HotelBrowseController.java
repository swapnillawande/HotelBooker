package com.swappy.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swappy.dto.HotelDto;
import com.swappy.dto.HotelInfoDto;
import com.swappy.dto.HotelSearchDto;
import com.swappy.dto.RoomOfferDto;
import com.swappy.service.HotelService;
import com.swappy.service.InventoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelBrowseController {

	private final InventoryService inventoryService;
	
	private final HotelService hotelService;
	
	
	@PostMapping("/search")
	public ResponseEntity<Page <HotelDto>> searchHotels(@Valid @RequestBody HotelSearchDto hotelSearchDto ){
		
		Page<HotelDto> page = inventoryService.searchHotels(hotelSearchDto);
		
		return ResponseEntity.ok(page);
	}
	
	
	@GetMapping("/{hotelId}/info")
	public ResponseEntity<HotelInfoDto> getHotelInfo(@PathVariable("hotelId") Long hotelId){
		
		
		return ResponseEntity.ok(hotelService.getHotelInfoById(hotelId));
	}

	@GetMapping("/{hotelId}/offers")
	public ResponseEntity<List<RoomOfferDto>> getRoomOffers(
			@PathVariable("hotelId") Long hotelId,
			@RequestParam LocalDate checkIn,
			@RequestParam LocalDate checkOut,
			@RequestParam(defaultValue = "1") Integer rooms) {
		return ResponseEntity.ok(inventoryService.getRoomOffers(hotelId, checkIn, checkOut, rooms));
	}
}
