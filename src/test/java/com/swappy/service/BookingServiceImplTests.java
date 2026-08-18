package com.swappy.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

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
import com.swappy.entities.enums.Gender;
import com.swappy.exception.ResourceNotFoundException;
import com.swappy.repository.BookingRepository;
import com.swappy.repository.GuestRepository;
import com.swappy.repository.HotelRepository;
import com.swappy.repository.InventoryRepository;
import com.swappy.repository.RoomRepository;
import com.swappy.repository.UserRepository;
import com.swappy.service.impl.BookingServiceImpl;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTests {

    private static final String MANAGEMENT_TOKEN = "c2f58627-3207-4ee9-8f69-74d08fa9fb27";

    @Mock private BookingRepository bookingRepository;
    @Mock private HotelRepository hotelRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private GuestRepository guestRepository;
    @Mock private InventoryRepository inventoryRepository;
    @Mock private UserRepository userRepository;

    private BookingServiceImpl bookingService;
    private Hotel hotel;
    private Room room;
    private User user;

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceImpl(
                bookingRepository,
                hotelRepository,
                roomRepository,
                guestRepository,
                inventoryRepository,
                userRepository,
                new ModelMapper());

        hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Stayly Berlin");
        hotel.setIsActive(true);

        room = new Room();
        room.setId(2L);
        room.setHotel(hotel);
        room.setCapacity(2);

        user = new User();
        user.setId(3L);
        user.setEmail("guest@stayly.local");
    }

    @Test
    void initialisesBookingForNightsAndCalculatesInventoryTotal() {
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = checkIn.plusDays(2);
        BookingRequest request = request(checkIn, checkOut, 2);
        Inventory firstNight = inventory(new BigDecimal("40.00"));
        Inventory secondNight = inventory(new BigDecimal("55.00"));

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(roomRepository.findById(2L)).thenReturn(Optional.of(room));
        when(inventoryRepository.findAndLockAvailableInventory(2L, checkIn, checkOut, 2))
                .thenReturn(List.of(firstNight, secondNight));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(10L);
            return booking;
        });

        BookingDto result = bookingService.initialiseBooking(request);

        assertEquals(new BigDecimal("190.00"), result.getAmount());
        assertEquals(BookingStatus.RESERVED, result.getBookingStatus());
        assertEquals(2, firstNight.getReservedCount());
        assertEquals(2, secondNight.getReservedCount());
        assertTrue(result.getGuests().isEmpty());
        assertNotNull(result.getManagementToken());
        verify(inventoryRepository).saveAll(List.of(firstNight, secondNight));
    }

    @Test
    void rejectsCheckoutThatIsNotAfterCheckinBeforeAccessingRepositories() {
        LocalDate date = LocalDate.now().plusDays(2);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.initialiseBooking(request(date, date, 1)));

        assertEquals("Check-out date must be after check-in date", error.getMessage());
        verify(hotelRepository, never()).findById(any());
    }

    @Test
    void rejectsRoomFromAnotherHotel() {
        Hotel otherHotel = new Hotel();
        otherHotel.setId(99L);
        room.setHotel(otherHotel);
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(roomRepository.findById(2L)).thenReturn(Optional.of(room));

        assertThrows(IllegalArgumentException.class, () -> bookingService.initialiseBooking(
                request(LocalDate.now().plusDays(2), LocalDate.now().plusDays(3), 1)));

        verify(inventoryRepository, never()).findAndLockAvailableInventory(any(), any(), any(), any());
    }

    @Test
    void addsGuestsAndMovesBookingToGuestAdded() {
        Booking booking = reservedBooking();
        GuestDto guestDto = new GuestDto();
        guestDto.setName("Alex Doe");
        guestDto.setAge(28);
        guestDto.setGender(Gender.OTHER);
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(guestRepository.save(any(Guest.class))).thenAnswer(invocation -> {
            Guest guest = invocation.getArgument(0);
            guest.setId(11L);
            return guest;
        });
        when(bookingRepository.save(booking)).thenReturn(booking);

        BookingDto result = bookingService.addGuests(10L, MANAGEMENT_TOKEN, List.of(guestDto));

        assertEquals(BookingStatus.GUEST_ADDED, result.getBookingStatus());
        assertEquals(1, result.getGuests().size());
        assertEquals("Alex Doe", result.getGuests().iterator().next().getName());
    }

    @Test
    void rejectsGuestsBeyondBookedRoomCapacity() {
        Booking booking = reservedBooking();
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        List<GuestDto> guests = List.of(new GuestDto(), new GuestDto(), new GuestDto());

        assertThrows(IllegalArgumentException.class, () -> bookingService.addGuests(10L, MANAGEMENT_TOKEN, guests));

        verify(guestRepository, never()).save(any());
    }

    @Test
    void confirmsBookingAndConvertsReservedInventoryToBooked() {
        Booking booking = reservedBooking();
        booking.setBookingStatus(BookingStatus.GUEST_ADDED);
        Inventory firstNight = inventory(new BigDecimal("40.00"));
        firstNight.setReservedCount(3);
        firstNight.setBookedCount(1);
        when(bookingRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(booking));
        when(inventoryRepository.findAndLockInventoryForBooking(
                room.getId(), booking.getCheckInDate(), booking.getCheckOutDate()))
                .thenReturn(List.of(firstNight));
        when(bookingRepository.save(booking)).thenReturn(booking);

        BookingDto result = bookingService.confirmBooking(10L, MANAGEMENT_TOKEN);

        assertEquals(BookingStatus.CONFIRMED, result.getBookingStatus());
        assertEquals(2, firstNight.getReservedCount());
        assertEquals(2, firstNight.getBookedCount());
        verify(inventoryRepository).saveAll(List.of(firstNight));
    }

    @Test
    void confirmingAnAlreadyConfirmedBookingIsIdempotent() {
        Booking booking = reservedBooking();
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(booking));

        BookingDto result = bookingService.confirmBooking(10L, MANAGEMENT_TOKEN);

        assertEquals(BookingStatus.CONFIRMED, result.getBookingStatus());
        verify(inventoryRepository, never()).findAndLockInventoryForBooking(any(), any(), any());
    }

    @Test
    void cancelsConfirmedBookingAndReturnsBookedInventory() {
        Booking booking = reservedBooking();
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        Inventory inventory = inventory(new BigDecimal("40.00"));
        inventory.setBookedCount(2);
        when(bookingRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(booking));
        when(inventoryRepository.findAndLockInventoryForBooking(
                room.getId(), booking.getCheckInDate(), booking.getCheckOutDate()))
                .thenReturn(List.of(inventory));
        when(bookingRepository.save(booking)).thenReturn(booking);

        BookingDto result = bookingService.cancelBooking(10L, MANAGEMENT_TOKEN);

        assertEquals(BookingStatus.CANCELLED, result.getBookingStatus());
        assertEquals(1, inventory.getBookedCount());
        verify(inventoryRepository).saveAll(List.of(inventory));
    }

    @Test
    void cancellingAnAlreadyCancelledBookingIsIdempotent() {
        Booking booking = reservedBooking();
        booking.setBookingStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(booking));

        BookingDto result = bookingService.cancelBooking(10L, MANAGEMENT_TOKEN);

        assertEquals(BookingStatus.CANCELLED, result.getBookingStatus());
        verify(inventoryRepository, never()).findAndLockInventoryForBooking(any(), any(), any());
    }

    @Test
    void rejectsBookingAccessWithTheWrongManagementToken() {
        Booking booking = reservedBooking();
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));

        assertThrows(ResourceNotFoundException.class,
                () -> bookingService.getBooking(10L, "wrong-token"));
    }

    private BookingRequest request(LocalDate checkIn, LocalDate checkOut, int roomsCount) {
        BookingRequest request = new BookingRequest();
        request.setHotelId(1L);
        request.setRoomId(2L);
        request.setCheckInDate(checkIn);
        request.setCheckOutDate(checkOut);
        request.setRoomsCount(roomsCount);
        return request;
    }

    private Inventory inventory(BigDecimal price) {
        Inventory inventory = new Inventory();
        inventory.setPrice(price);
        inventory.setReservedCount(0);
        return inventory;
    }

    private Booking reservedBooking() {
        Booking booking = new Booking();
        booking.setId(10L);
        booking.setHotel(hotel);
        booking.setRoom(room);
        booking.setUser(user);
        booking.setRoomsCount(1);
        booking.setManagementToken(MANAGEMENT_TOKEN);
        booking.setCheckInDate(LocalDate.now().plusDays(2));
        booking.setCheckOutDate(LocalDate.now().plusDays(3));
        booking.setCreatedAt(LocalDateTime.now());
        booking.setBookingStatus(BookingStatus.RESERVED);
        booking.setAmount(new BigDecimal("40.00"));
        booking.setGuests(new HashSet<>());
        return booking;
    }
}
