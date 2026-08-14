package com.swappy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import com.swappy.dto.HotelDto;
import com.swappy.entities.Hotel;
import com.swappy.repository.HotelRepository;
import com.swappy.service.InventoryService;
import com.swappy.service.impl.HotelServiceImpl;

@ExtendWith(MockitoExtension.class)
class HotelBookerApplicationTests {

	@Mock
	private HotelRepository hotelRepository;

	@Mock
	private InventoryService inventoryService;

	private HotelServiceImpl hotelService;

	@BeforeEach
	void setUp() {
		hotelService = new HotelServiceImpl(hotelRepository, inventoryService, new ModelMapper());
	}

	@Test
	void createsInactiveHotel() {
		HotelDto request = new HotelDto();
		request.setName("Demo");
		when(hotelRepository.save(any(Hotel.class))).thenAnswer(invocation -> {
			Hotel hotel = invocation.getArgument(0);
			hotel.setId(1L);
			return hotel;
		});

		HotelDto created = hotelService.createNewHotel(request);

		assertEquals(1L, created.getId());
		assertEquals("Demo", created.getName());
		assertFalse(created.getIsActive());
	}

	@Test
	void updatePreservesPathId() {
		Hotel existing = new Hotel();
		existing.setId(7L);
		existing.setName("Old name");
		existing.setIsActive(false);
		HotelDto request = new HotelDto();
		request.setName("New name");
		when(hotelRepository.findById(7L)).thenReturn(Optional.of(existing));
		when(hotelRepository.save(any(Hotel.class))).thenAnswer(invocation -> invocation.getArgument(0));

		HotelDto updated = hotelService.updateHotelById(7L, request);

		assertEquals(7L, updated.getId());
		assertEquals("New name", updated.getName());
	}

	@Test
	void deleteReportsSuccess() {
		Hotel hotel = new Hotel();
		hotel.setId(9L);
		hotel.setRooms(Collections.emptyList());
		when(hotelRepository.findById(9L)).thenReturn(Optional.of(hotel));

		assertTrue(hotelService.deleteHotelById(9L));
		verify(hotelRepository).delete(hotel);
	}
}
