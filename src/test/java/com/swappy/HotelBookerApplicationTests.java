package com.swappy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import com.swappy.entities.User;
import com.swappy.exception.ResourceNotFoundException;
import com.swappy.repository.HotelRepository;
import com.swappy.repository.UserRepository;
import com.swappy.service.InventoryService;
import com.swappy.service.impl.HotelServiceImpl;

@ExtendWith(MockitoExtension.class)
class HotelBookerApplicationTests {

	@Mock
	private HotelRepository hotelRepository;

	@Mock
	private InventoryService inventoryService;

	@Mock
	private UserRepository userRepository;

	private HotelServiceImpl hotelService;

	@BeforeEach
	void setUp() {
		hotelService = new HotelServiceImpl(hotelRepository, userRepository, inventoryService, new ModelMapper());
	}

	@Test
	void createsInactiveHotel() {
		User manager = manager(3L);
		HotelDto request = new HotelDto();
		request.setName("Demo");
		when(hotelRepository.save(any(Hotel.class))).thenAnswer(invocation -> {
			Hotel hotel = invocation.getArgument(0);
			hotel.setId(1L);
			return hotel;
		});
		when(userRepository.findById(3L)).thenReturn(Optional.of(manager));

		HotelDto created = hotelService.createNewHotel(request, 3L);

		assertEquals(1L, created.getId());
		assertEquals("Demo", created.getName());
		assertFalse(created.getIsActive());
		verify(hotelRepository).save(org.mockito.ArgumentMatchers.argThat(hotel -> hotel.getOwner() == manager));
	}

	@Test
	void updatePreservesPathId() {
		Hotel existing = new Hotel();
		existing.setId(7L);
		existing.setName("Old name");
		existing.setIsActive(false);
		HotelDto request = new HotelDto();
		request.setName("New name");
		existing.setOwner(manager(3L));
		when(hotelRepository.findByIdAndOwner_Id(7L, 3L)).thenReturn(Optional.of(existing));
		when(hotelRepository.save(any(Hotel.class))).thenAnswer(invocation -> invocation.getArgument(0));

		HotelDto updated = hotelService.updateHotelById(7L, request, 3L);

		assertEquals(7L, updated.getId());
		assertEquals("New name", updated.getName());
	}

	@Test
	void deleteReportsSuccess() {
		Hotel hotel = new Hotel();
		hotel.setId(9L);
		hotel.setRooms(Collections.emptyList());
		hotel.setOwner(manager(3L));
		when(hotelRepository.findByIdAndOwner_Id(9L, 3L)).thenReturn(Optional.of(hotel));

		assertTrue(hotelService.deleteHotelById(9L, 3L));
		verify(hotelRepository).delete(hotel);
	}

	@Test
	void managerCannotOpenAnotherManagersHotel() {
		when(hotelRepository.findByIdAndOwner_Id(7L, 4L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> hotelService.getHotelById(7L, 4L));
	}

	private User manager(Long id) {
		User user = new User();
		user.setId(id);
		return user;
	}
}
