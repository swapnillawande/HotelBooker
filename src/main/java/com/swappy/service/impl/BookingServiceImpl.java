package com.swappy.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Value;

import com.swappy.dto.BookingDto;
import com.swappy.dto.BookingRequest;
import com.swappy.dto.GuestDto;
import com.swappy.dto.DemoPaymentRequest;
import com.swappy.entities.Booking;
import com.swappy.entities.Guest;
import com.swappy.entities.Hotel;
import com.swappy.entities.Inventory;
import com.swappy.entities.Payment;
import com.swappy.entities.Room;
import com.swappy.entities.User;
import com.swappy.entities.enums.BookingStatus;
import com.swappy.entities.enums.PaymentStatus;
import com.swappy.entities.enums.Role;
import com.swappy.exception.ResourceNotFoundException;
import com.swappy.repository.BookingRepository;
import com.swappy.repository.GuestRepository;
import com.swappy.repository.HotelRepository;
import com.swappy.repository.InventoryRepository;
import com.swappy.repository.PaymentRepository;
import com.swappy.repository.RoomRepository;
import com.swappy.repository.UserRepository;
import com.swappy.service.BookingService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService{

	private static final String GUEST_EMAIL = "guest@stayly.local";
	private static final String DEMO_PAYMENT_TOKEN = "tok_demo_visa";
	private static final String DEMO_PAYMENT_PROVIDER = "STAYLY_DEMO";

	private final BookingRepository bookingRepository;
	
	private final HotelRepository hotelRepository;
	
	private final RoomRepository roomRepository;
	
	private final GuestRepository guestRepository;
	
	private final InventoryRepository inventoryRepository;
	
	private final UserRepository userRepository;

	private final PaymentRepository paymentRepository;

	private final ModelMapper modelMapper;

	@Value("${payments.demo-enabled:false}")
	private boolean demoPaymentsEnabled;
	
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
		booking.setManagementToken(UUID.randomUUID().toString());
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
	public BookingDto addGuests(Long bookingId, String managementToken, List<GuestDto> guestDtoList) {

	    //log.info("Adding guests for booking with id: {}", bookingId);

	    Booking booking = bookingRepository.findById(bookingId)
	            .orElseThrow(() ->
	                new ResourceNotFoundException("Booking not found with id: " + bookingId));
	    validateManagementToken(booking, managementToken);

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
	        guest.setUser(booking.getUser());
	        Guest savedGuest = guestRepository.save(guest);
	        booking.getGuests().add(savedGuest);
	    }

	    booking.setBookingStatus(BookingStatus.GUEST_ADDED);
	    
	    booking = bookingRepository.save(booking);
	    
	    return toDto(booking);
	}

	@Override
	@Transactional
	public BookingDto confirmBooking(Long bookingId, String managementToken) {
		Booking booking = bookingRepository.findByIdForUpdate(bookingId)
				.orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
		validateManagementToken(booking, managementToken);

		if (booking.getBookingStatus() == BookingStatus.CONFIRMED) {
			return toDto(booking);
		}
		Payment payment = paymentRepository.findByBooking_Id(bookingId)
				.filter(candidate -> candidate.getPaymentStatus() == PaymentStatus.CONFIRMED)
				.orElseThrow(() -> new IllegalStateException("Successful payment is required before confirmation"));
		booking.setPayment(payment);
		booking.setBookingStatus(BookingStatus.PAYMENT_PENDING);
		return confirmPaidBooking(booking);
	}

	@Override
	@Transactional
	public BookingDto payBooking(
			Long bookingId,
			String managementToken,
			String idempotencyKey,
			DemoPaymentRequest request) {
		Booking booking = bookingRepository.findByIdForUpdate(bookingId)
				.orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
		validateManagementToken(booking, managementToken);

		if (booking.getBookingStatus() == BookingStatus.CONFIRMED) {
			return toDto(booking);
		}
		if (hasBookingExpired(booking)) {
			throw new IllegalStateException("Booking has already expired");
		}
		if (booking.getBookingStatus() != BookingStatus.GUEST_ADDED) {
			throw new IllegalStateException("Guest details must be added before payment");
		}
		validateIdempotencyKey(idempotencyKey);
		if (!demoPaymentsEnabled) {
			throw new IllegalStateException("Demo payments are disabled in this environment");
		}
		if (!DEMO_PAYMENT_TOKEN.equals(request.paymentToken())) {
			throw new IllegalArgumentException("Payment was declined by the demo provider");
		}

		Payment payment = paymentRepository.findByBooking_Id(bookingId).orElse(null);
		if (payment != null) {
			if (!payment.getIdempotencyKey().equals(idempotencyKey)) {
				throw new IllegalStateException("A different payment attempt already exists for this booking");
			}
			booking.setPayment(payment);
			if (payment.getPaymentStatus() == PaymentStatus.CONFIRMED) {
				booking.setBookingStatus(BookingStatus.PAYMENT_PENDING);
				return confirmPaidBooking(booking);
			}
		} else {
			payment = new Payment();
			payment.setTransactionId(UUID.randomUUID().toString());
			payment.setIdempotencyKey(idempotencyKey);
			payment.setProvider(DEMO_PAYMENT_PROVIDER);
			payment.setCardLastFour("4242");
			payment.setPaymentStatus(PaymentStatus.PENDING);
			payment.setAmount(booking.getAmount());
			payment.setBooking(booking);
			payment = paymentRepository.save(payment);
		}

		booking.setPayment(payment);
		booking.setBookingStatus(BookingStatus.PAYMENT_PENDING);
		bookingRepository.save(booking);

		payment.setPaymentStatus(PaymentStatus.CONFIRMED);
		paymentRepository.save(payment);
		return confirmPaidBooking(booking);
	}

	private BookingDto confirmPaidBooking(Booking booking) {
		if (booking.getPayment() == null
				|| booking.getPayment().getPaymentStatus() != PaymentStatus.CONFIRMED) {
			throw new IllegalStateException("Successful payment is required before confirmation");
		}
		if (booking.getBookingStatus() != BookingStatus.PAYMENT_PENDING) {
			throw new IllegalStateException("Booking is not awaiting payment confirmation");
		}

		List<Inventory> inventories = inventoryRepository.findAndLockInventoryForBooking(
				booking.getRoom().getId(),
				booking.getCheckInDate(),
				booking.getCheckOutDate());
		long expectedNights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
		if (inventories.size() != expectedNights) {
			throw new IllegalStateException("Booking inventory is incomplete");
		}

		for (Inventory inventory : inventories) {
			int reservedCount = inventory.getReservedCount() == null ? 0 : inventory.getReservedCount();
			if (reservedCount < booking.getRoomsCount()) {
				throw new IllegalStateException("Booking reservation is no longer available");
			}
			inventory.setReservedCount(reservedCount - booking.getRoomsCount());
			int bookedCount = inventory.getBookedCount() == null ? 0 : inventory.getBookedCount();
			inventory.setBookedCount(bookedCount + booking.getRoomsCount());
		}
		inventoryRepository.saveAll(inventories);

		booking.setBookingStatus(BookingStatus.CONFIRMED);
		return toDto(bookingRepository.save(booking));
	}

	@Override
	@Transactional
	public BookingDto getBooking(Long bookingId, String managementToken) {
		Booking booking = bookingRepository.findById(bookingId)
				.orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
		validateManagementToken(booking, managementToken);
		return toDto(booking);
	}

	@Override
	@Transactional
	public BookingDto cancelBooking(Long bookingId, String managementToken) {
		Booking booking = bookingRepository.findByIdForUpdate(bookingId)
				.orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
		validateManagementToken(booking, managementToken);
		return cancelLockedBooking(booking);
	}

	@Override
	@Transactional
	public List<BookingDto> getBookingsForUser(Long userId) {
		return bookingRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
				.map(this::toAccountDto)
				.toList();
	}

	@Override
	@Transactional
	public BookingDto cancelBookingForUser(Long bookingId, Long userId) {
		Booking booking = bookingRepository.findByIdForUpdate(bookingId)
				.orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
		if (!booking.getUser().getId().equals(userId)) {
			throw new ResourceNotFoundException("Booking not found with id: " + bookingId);
		}
		return withoutManagementToken(cancelLockedBooking(booking));
	}

	private BookingDto cancelLockedBooking(Booking booking) {
		if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
			return toDto(booking);
		}
		if (booking.getBookingStatus() == BookingStatus.EXPIRED) {
			throw new IllegalStateException("Expired bookings cannot be cancelled");
		}
		if (!booking.getCheckInDate().isAfter(LocalDate.now())) {
			throw new IllegalStateException("Bookings cannot be cancelled on or after check-in");
		}

		List<Inventory> inventories = inventoryRepository.findAndLockInventoryForBooking(
				booking.getRoom().getId(), booking.getCheckInDate(), booking.getCheckOutDate());
		long expectedNights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
		if (inventories.size() != expectedNights) {
			throw new IllegalStateException("Booking inventory is incomplete");
		}

		boolean confirmed = booking.getBookingStatus() == BookingStatus.CONFIRMED;
		for (Inventory inventory : inventories) {
			if (confirmed) {
				int bookedCount = inventory.getBookedCount() == null ? 0 : inventory.getBookedCount();
				if (bookedCount < booking.getRoomsCount()) {
					throw new IllegalStateException("Booked inventory is inconsistent");
				}
				inventory.setBookedCount(bookedCount - booking.getRoomsCount());
			} else {
				int reservedCount = inventory.getReservedCount() == null ? 0 : inventory.getReservedCount();
				if (reservedCount < booking.getRoomsCount()) {
					throw new IllegalStateException("Reserved inventory is inconsistent");
				}
				inventory.setReservedCount(reservedCount - booking.getRoomsCount());
			}
		}
		inventoryRepository.saveAll(inventories);
		Payment payment = booking.getPayment();
		if (payment != null && payment.getPaymentStatus() == PaymentStatus.CONFIRMED) {
			payment.setPaymentStatus(PaymentStatus.REFUNDED);
			paymentRepository.save(payment);
		}
		booking.setBookingStatus(BookingStatus.CANCELLED);
		return toDto(bookingRepository.save(booking));
	}

	public boolean hasBookingExpired(Booking booking) {
	    return booking.getCreatedAt()
	            .plusMinutes(10)
	            .isBefore(LocalDateTime.now());
	}

	private User getCurrentUser() {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof User user) {
			return user;
		}
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

	private void validateManagementToken(Booking booking, String managementToken) {
		if (managementToken == null || !managementToken.equals(booking.getManagementToken())) {
			throw new ResourceNotFoundException("Booking not found with id: " + booking.getId());
		}
	}

	private void validateIdempotencyKey(String idempotencyKey) {
		if (idempotencyKey == null
				|| idempotencyKey.isBlank()
				|| idempotencyKey.length() < 8
				|| idempotencyKey.length() > 64) {
			throw new IllegalArgumentException("Idempotency-Key must contain between 8 and 64 characters");
		}
	}

	private BookingDto toDto(Booking booking) {
		BookingDto dto = new BookingDto();
		dto.setId(booking.getId());
		dto.setHotelId(booking.getHotel().getId());
		dto.setRoomId(booking.getRoom().getId());
		dto.setRoomsCount(booking.getRoomsCount());
		dto.setManagementToken(booking.getManagementToken());
		dto.setCheckInDate(booking.getCheckInDate());
		dto.setCheckOutDate(booking.getCheckOutDate());
		dto.setCreatedAt(booking.getCreatedAt());
		dto.setUpdatedAt(booking.getUpdatedAt());
		dto.setBookingStatus(booking.getBookingStatus());
		dto.setAmount(booking.getAmount());
		Payment payment = booking.getPayment();
		if (payment != null) {
			dto.setPaymentStatus(payment.getPaymentStatus());
			dto.setPaymentReference(payment.getTransactionId());
		}
		dto.setGuests(booking.getGuests() == null ? Set.of() : booking.getGuests().stream()
				.map(guest -> modelMapper.map(guest, GuestDto.class))
				.collect(java.util.stream.Collectors.toSet()));
		return dto;
	}

	private BookingDto toAccountDto(Booking booking) {
		return withoutManagementToken(toDto(booking));
	}

	private BookingDto withoutManagementToken(BookingDto dto) {
		dto.setManagementToken(null);
		return dto;
	}

}
