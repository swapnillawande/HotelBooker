package com.swappy.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.swappy.service.HotelService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/admin/hotels")
@RequiredArgsConstructor
public class HotelController {
	
	Logger logger = LoggerFactory.getLogger(HotelController.class);
	
	@Autowired
	private HotelService hotelService;
	
	
	@PostMapping
	public ResponseEntity<HotelDto> createNewHotel(@RequestBody HotelDto hotelDto) {
		
	  HotelDto hotel = hotelService.createNewHotel(hotelDto);
	  
	  return new ResponseEntity<>(hotel, HttpStatus.CREATED);
		
		
	}
	
    @GetMapping("/{id}")
    public ResponseEntity<HotelDto> getHotelById(@PathVariable("id") Long id) {

        HotelDto hotel = hotelService.getHotelById(id);

        return new ResponseEntity<>(hotel, HttpStatus.OK);
    }

    
    @PutMapping("/{id}")
    public ResponseEntity<HotelDto> updateHotelById(@PathVariable("id") Long id, @RequestBody HotelDto hotelDto){
    	
        HotelDto hotel = hotelService.updateHotelById(id, hotelDto);
    	
    	
    	return ResponseEntity.ok(hotel);
    }
    
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHotelById(@PathVariable("id") Long id){

        hotelService.deleteHotelById(id);

        return ResponseEntity.noContent().build(); // 204 No Content
    }
    
    
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateHotelById(@PathVariable("id") Long id){
    	hotelService.activateHotel(id);
		return ResponseEntity.noContent().build();
    }

}
