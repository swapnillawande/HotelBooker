package com.swappy.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.swappy.dto.BookingDto;
import com.swappy.dto.BookingRequest;
import com.swappy.dto.GuestDto;
import com.swappy.entities.Booking;
import com.swappy.entities.Guest;
import com.swappy.entities.Hotel;
import com.swappy.entities.Inventory;
import com.swappy.entities.Room;
import com.swappy.entities.User;
import com.swappy.entities.enums.BookingStatus;
import com.swappy.entities.enums.Role;
import com.swappy.exception.ResourceNotFoundException;
import com.swappy.repository.BookingRepository;
import com.swappy.repository.GuestRepository;
import com.swappy.repository.HotelRepository;
import com.swappy.repository.InventoryRepository;
import com.swappy.repository.RoomRepository;
import com.swappy.repository.UserRepository;
import com.swappy.service.BookingService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService{

	private static final String GUEST_EMAIL = "guest@stayly.local";

	private final BookingRepository bookingRepository;
	
	private final HotelRepository hotelRepository;
	
	private final RoomRepository roomRepository;
	
	private final GuestRepository guestRepository;
	
	private final InventoryRepository inventoryRepository;
	
	private final UserRepository userRepository;

	private final ModelMapper modelMapper;
	
	Logger logger = LoggerFactory.getLogger(BookingServiceImpl.class);


	@Override
	@Transactional
	public BookingDto initialiseBooking(BookingRequest bookingRequest) {
		validateStayDates(bookingRequest);
		
		Hotel hotel = hotelRepository.findById(bookingRequest.getHotelId())
				 .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: "+ bookingRequest.getHotelId()));


		Room room = roomRepository.findById(bookingRequest.getRoomId())
		.orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: "+ bookingRequest.getRoomId()));

		if (!room.getHotel().getId().equals(hotel.getId())) {
			throw new IllegalArgumentException("Selected room does not belong to the selected hotel");
		}
		if (!Boolean.TRUE.equals(hotel.getIsActive())) {
			throw new IllegalStateException("Hotel is not active for booking");
		}
		
		List<Inventory> inventoryList = inventoryRepository.findAndLockAvailableInventory(
		room.getId(), 
		bookingRequest.getCheckInDate(),
		bookingRequest.getCheckOutDate(),
		bookingRequest.getRoomsCount());
		
		
		long daysCount = ChronoUnit.DAYS.between(
		bookingRequest.getCheckInDate(),
		bookingRequest.getCheckOutDate()
		);
		
		if (inventoryList.size() != daysCount) {
		throw new IllegalStateException("Room is not available anymore");
		}
		
		for(Inventory inventory : inventoryList) {
		inventory.setReservedCount(inventory.getReservedCount()+ bookingRequest.getRoomsCount());
		}
		
		inventoryRepository.saveAll(inventoryList);
		
		
		Booking booking = new Booking();
		
		booking.setBookingStatus(BookingStatus.RESERVED);
		booking.setHotel(hotel);
		booking.setRoom(room);
		booking.setCheckInDate(bookingRequest.getCheckInDate());
		booking.setCheckOutDate(bookingRequest.getCheckOutDate());
		booking.setUser(getCurrentUser());
		booking.setRoomsCount(bookingRequest.getRoomsCount());
		booking.setGuests(new HashSet<>());
		BigDecimal amount = inventoryList.stream()
				.map(Inventory::getPrice)
				.reduce(BigDecimal.ZERO, BigDecimal::add)
				.multiply(BigDecimal.valueOf(bookingRequest.getRoomsCount()));
		booking.setAmount(amount);
		
		
		booking = bookingRepository.save(booking);
		
		
		
		return toDto(booking);
	}


	@Transactional
	public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList) {

	    //log.info("Adding guests for booking with id: {}", bookingId);

	    Booking booking = bookingRepository.findById(bookingId)
	            .orElseThrow(() ->
	                new ResourceNotFoundException("Booking not found with id: " + bookingId));

	    if (hasBookingExpired(booking)) {
	        throw new IllegalStateException("Booking has already expired");
	    }

	    if (booking.getBookingStatus() != BookingStatus.RESERVED) {
	        throw new IllegalStateException("Booking is not under reserved state. Cannot add guests");
	    }
	    int maximumGuests = booking.getRoom().getCapacity() * booking.getRoomsCount();
	    if (guestDtoList.size() > maximumGuests) {
	        throw new IllegalArgumentException("Guest count exceeds the selected room capacity");
	    }
	    if (booking.getGuests() == null) {
	        booking.setGuests(new HashSet<>());
	    }

	    for (GuestDto guestDto : guestDtoList) {
	        Guest guest = modelMapper.map(guestDto, Guest.class);
	        guest.setUser(getCurrentUser());
	        Guest savedGuest = guestRepository.save(guest);
	        booking.getGuests().add(savedGuest);
	    }

	    booking.setBookingStatus(BookingStatus.GUEST_ADDED);
	    
	    booking = bookingRepository.save(booking);
	    
	    return toDto(booking);
	}

	public boolean hasBookingExpired(Booking booking) {
	    return booking.getCreatedAt()
	            .plusMinutes(10)
	            .isBefore(LocalDateTime.now());
	}

	private User getCurrentUser() {
	    return userRepository.findByEmail(GUEST_EMAIL).orElseGet(() -> {
	        User user = new User();
	        user.setEmail(GUEST_EMAIL);
	        user.setPassword("authentication-not-configured");
	        user.setName("Stayly Guest");
	        user.setRoles(Set.of(Role.GUEST));
	        return userRepository.save(user);
	    });
	}

	private void validateStayDates(BookingRequest bookingRequest) {
		if (!bookingRequest.getCheckOutDate().isAfter(bookingRequest.getCheckInDate())) {
			throw new IllegalArgumentException("Check-out date must be after check-in date");
		}
	}

	private BookingDto toDto(Booking booking) {
		BookingDto dto = new BookingDto();
		dto.setId(booking.getId());
		dto.setHotelId(booking.getHotel().getId());
		dto.setRoomId(booking.getRoom().getId());
		dto.setRoomsCount(booking.getRoomsCount());
		dto.setCheckInDate(booking.getCheckInDate());
		dto.setCheckOutDate(booking.getCheckOutDate());
		dto.setCreatedAt(booking.getCreatedAt());
		dto.setUpdatedAt(booking.getUpdatedAt());
		dto.setBookingStatus(booking.getBookingStatus());
		dto.setAmount(booking.getAmount());
		dto.setGuests(booking.getGuests() == null ? Set.of() : booking.getGuests().stream()
				.map(guest -> modelMapper.map(guest, GuestDto.class))
				.collect(java.util.stream.Collectors.toSet()));
		return dto;
	}
	
}







