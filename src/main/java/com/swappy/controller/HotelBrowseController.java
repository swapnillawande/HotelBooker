package com.swappy.controller;

import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swappy.dto.HotelDto;
import com.swappy.dto.HotelInfoDto;
import com.swappy.dto.HotelSearchDto;
import com.swappy.service.HotelService;
import com.swappy.service.InventoryService;

@RestController
@RequestMapping("/hotels")
public class HotelBrowseController {

	@Autowired
	private InventoryService inventoryService;
	
	@Autowired
	private HotelService hotelService;
	
	
	@PostMapping("/search")
	public ResponseEntity<Page <HotelDto>> searchHotels(@RequestBody HotelSearchDto hotelSearchDto ){
		
		Page<HotelDto> page = inventoryService.searchHotels(hotelSearchDto);
		
		return ResponseEntity.ok(page);
	}
	
	
	@GetMapping("/{hotelId}/info")
	private ResponseEntity<HotelInfoDto> getHotelInfo(@PathVariable("hotelId") Long hotelId){
		
		
		return ResponseEntity.ok(hotelService.getHotelInfoById(hotelId));
	}
}
