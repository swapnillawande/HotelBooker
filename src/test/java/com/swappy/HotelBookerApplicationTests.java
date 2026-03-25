package com.swappy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.swappy.dto.HotelDto;
import com.swappy.service.impl.HotelServiceImpl;

@SpringBootTest
class HotelBookerApplicationTests {

	@Test
	void contextLoads() {
		
	
	}
	
	@Test
	void con() {
		HotelDto hotel = new HotelDto();
		hotel.setName("Demo");
		HotelServiceImpl hotelServiceImpl = new HotelServiceImpl();
		
		hotelServiceImpl.createNewHotel(hotel);
	}

}
