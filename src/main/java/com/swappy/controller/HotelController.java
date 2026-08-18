package com.swappy.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swappy.dto.HotelDto;
import com.swappy.entities.User;
import com.swappy.service.HotelService;

import java.util.List;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/admin/hotels")
@RequiredArgsConstructor
public class HotelController {
	
	Logger logger = LoggerFactory.getLogger(HotelController.class);
	
	private final HotelService hotelService;
	
	
	@PostMapping
	public ResponseEntity<HotelDto> createNewHotel(
			@Valid @RequestBody HotelDto hotelDto,
			@AuthenticationPrincipal User user) {
		
	  HotelDto hotel = hotelService.createNewHotel(hotelDto, user.getId());
	  
	  return new ResponseEntity<>(hotel, HttpStatus.CREATED);
		
		
	}

	@GetMapping
	public ResponseEntity<List<HotelDto>> getMyHotels(@AuthenticationPrincipal User user) {
		return ResponseEntity.ok(hotelService.getHotelsForOwner(user.getId()));
	}
	
    @GetMapping("/{id}")
    public ResponseEntity<HotelDto> getHotelById(
			@PathVariable("id") Long id,
			@AuthenticationPrincipal User user) {

        HotelDto hotel = hotelService.getHotelById(id, user.getId());

        return new ResponseEntity<>(hotel, HttpStatus.OK);
    }

    
    @PutMapping("/{id}")
    public ResponseEntity<HotelDto> updateHotelById(
			@PathVariable("id") Long id,
			@Valid @RequestBody HotelDto hotelDto,
			@AuthenticationPrincipal User user){
    	
        HotelDto hotel = hotelService.updateHotelById(id, hotelDto, user.getId());
    	
    	
    	return ResponseEntity.ok(hotel);
    }
    
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHotelById(
			@PathVariable("id") Long id,
			@AuthenticationPrincipal User user){

        hotelService.deleteHotelById(id, user.getId());

        return ResponseEntity.noContent().build(); // 204 No Content
    }
    
    
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateHotelById(
			@PathVariable("id") Long id,
			@AuthenticationPrincipal User user){
		hotelService.activateHotel(id, user.getId());
		return ResponseEntity.noContent().build();
    }

}
