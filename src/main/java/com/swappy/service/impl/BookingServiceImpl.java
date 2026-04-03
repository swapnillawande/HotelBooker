package com.swappy.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.swappy.exception.ResourceNotFoundException;
import com.swappy.repository.BookingRepository;
import com.swappy.repository.GuestRepository;
import com.swappy.repository.HotelRepository;
import com.swappy.repository.InventoryRepository;
import com.swappy.repository.RoomRepository;
import com.swappy.service.BookingService;

import jakarta.transaction.Transactional;

@Service
public class BookingServiceImpl implements BookingService{

	@Autowired
	private BookingRepository bookingRepository;
	
	@Autowired
	private HotelRepository hotelRepository;
	
	@Autowired
	private RoomRepository roomRepository;
	
	@Autowired
	private GuestRepository guestRepository;
	
	@Autowired
	private InventoryRepository inventoryRepository;
	
	@Autowired
	private ModelMapper modelMapper;
	
	Logger logger = LoggerFactory.getLogger(BookingServiceImpl.class);


	@Override
	@Transactional
	public BookingDto initaliseBooking(BookingRequest bookingRequest) {
		
		Hotel hotel = hotelRepository.findById(bookingRequest.getHotelId())
				 .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: "+ bookingRequest.getHotelId()));


		Room room = roomRepository.findById(bookingRequest.getRoomId())
		.orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: "+ bookingRequest.getRoomId()));
		
		List<Inventory> inventoryList = inventoryRepository.findAndLockAvailableInventory(
		room.getId(), 
		bookingRequest.getCheckInDate(),
		bookingRequest.getCheckOutDate(),
		bookingRequest.getRoomsCount());
		
		
		long daysCount = ChronoUnit.DAYS.between(
		bookingRequest.getCheckInDate(),
		bookingRequest.getCheckOutDate()
		) + 1;
		
		if (inventoryList.size() != daysCount) {
		throw new IllegalStateException("Room is not available anymore");
		}
		
		for(Inventory inventory : inventoryList) {
		inventory.setReservedCount(inventory.getReservedCount()+ bookingRequest.getRoomsCount());
		}
		
		inventoryRepository.saveAll(inventoryList);
		
		
		User user = new User();
		user.setId(1L);
		
		Booking booking = new Booking();
		
		booking.setBookingStatus(BookingStatus.RESERVED);
		booking.setHotel(hotel);
		booking.setRoom(room);
		booking.setCheckInDate(bookingRequest.getCheckInDate());
		booking.setCheckOutDate(bookingRequest.getCheckOutDate());
		booking.setUser(getCurrentUser());
		booking.setRoomsCount(bookingRequest.getRoomsCount());
		booking.setAmount(BigDecimal.TEN);
		
		
		booking = bookingRepository.save(booking);
		
		
		
		return modelMapper.map(booking, BookingDto.class);
	}


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

	    for (GuestDto guestDto : guestDtoList) {
	        Guest guest = modelMapper.map(guestDto, Guest.class);
	        guest.setUser(getCurrentUser());
	        Guest savedGuest = guestRepository.save(guest);
	        booking.getGuests().add(savedGuest);
	    }

	    booking.setBookingStatus(BookingStatus.GUEST_ADDED);
	    
	    booking = bookingRepository.save(booking);
	    
	    return modelMapper.map(booking, BookingDto.class);
	}

	public boolean hasBookingExpired(Booking booking) {
	    return booking.getCreatedAt()
	            .plusMinutes(10)
	            .isBefore(LocalDateTime.now());
	}

	public User getCurrentUser() {
	    User user = new User();
	    user.setId(1L); // TODO: REMOVE DUMMY USER
	    return user;
	}
	
}








