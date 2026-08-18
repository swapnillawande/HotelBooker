package com.swappy.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.swappy.dto.HotelDto;
import com.swappy.dto.HotelSearchDto;
import com.swappy.dto.RoomOfferDto;
import com.swappy.entities.Hotel;
import com.swappy.entities.Inventory;
import com.swappy.entities.Room;
import com.swappy.repository.HotelRepository;
import com.swappy.repository.InventoryRepository;
import com.swappy.service.impl.InventoryServiceImpl;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTests {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private HotelRepository hotelRepository;

    private InventoryServiceImpl inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryServiceImpl(inventoryRepository, hotelRepository, new ModelMapper());
    }

    @Test
    void searchIncludesTheLowestAvailableRoomPrice() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = checkIn.plusDays(2);

        Hotel hotel = new Hotel();
        hotel.setId(7L);
        hotel.setName("Stayly Berlin Mitte");
        hotel.setCity("Berlin");
        hotel.setAmenities(List.of("Free Wi-Fi"));
        hotel.setPhotos(List.of());
        hotel.setIsActive(true);

        HotelSearchDto search = new HotelSearchDto();
        search.setCity("Berlin");
        search.setStartDate(checkIn);
        search.setEndDate(checkOut);
        search.setRoomCount(1);

        PageRequest pageRequest = PageRequest.of(0, 10);
        when(inventoryRepository.findHotelsWithAvailableInventory(
                eq("Berlin"), eq(checkIn), eq(checkOut), eq(1), eq(2L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(hotel), pageRequest, 1));
        when(inventoryRepository.findMinimumAvailablePrice(7L, checkIn, checkOut, 1, 2L))
                .thenReturn(new BigDecimal("62.00"));

        Page<HotelDto> results = inventoryService.searchHotels(search);

        assertEquals(1, results.getTotalElements());
        assertEquals(new BigDecimal("62.00"), results.getContent().get(0).getStartingPrice());
    }

    @Test
    void roomOffersIncludeExactStayPriceAndLowestRemainingAvailability() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = checkIn.plusDays(2);
        Hotel hotel = activeHotel();
        Room room = room(hotel);
        Inventory firstNight = inventory(room, checkIn, "60.00", 2);
        Inventory secondNight = inventory(room, checkIn.plusDays(1), "70.00", 4);

        when(hotelRepository.findById(7L)).thenReturn(java.util.Optional.of(hotel));
        when(inventoryRepository.findAvailableRoomInventory(7L, checkIn, checkOut, 2))
                .thenReturn(List.of(firstNight, secondNight));

        List<RoomOfferDto> offers = inventoryService.getRoomOffers(7L, checkIn, checkOut, 2);

        assertEquals(1, offers.size());
        assertEquals(2, offers.get(0).availableRooms());
        assertEquals(new BigDecimal("65.00"), offers.get(0).nightlyPrice());
        assertEquals(new BigDecimal("260.00"), offers.get(0).totalPrice());
    }

    @Test
    void roomOffersExcludeRoomsWithoutAvailabilityForEveryNight() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = checkIn.plusDays(2);
        Hotel hotel = activeHotel();
        Room room = room(hotel);
        when(hotelRepository.findById(7L)).thenReturn(java.util.Optional.of(hotel));
        when(inventoryRepository.findAvailableRoomInventory(7L, checkIn, checkOut, 1))
                .thenReturn(List.of(inventory(room, checkIn, "60.00", 2)));

        assertEquals(0, inventoryService.getRoomOffers(7L, checkIn, checkOut, 1).size());
    }

    @Test
    void roomOffersRejectPastDates() {
        assertThrows(IllegalArgumentException.class, () -> inventoryService.getRoomOffers(
                7L, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), 1));
    }

    private Hotel activeHotel() {
        Hotel hotel = new Hotel();
        hotel.setId(7L);
        hotel.setIsActive(true);
        return hotel;
    }

    private Room room(Hotel hotel) {
        Room room = new Room();
        room.setId(11L);
        room.setHotel(hotel);
        room.setType("Family room");
        room.setCapacity(4);
        room.setAmenities(List.of("Private bathroom"));
        room.setPhotos(List.of());
        return room;
    }

    private Inventory inventory(Room room, LocalDate date, String price, int availableRooms) {
        Inventory inventory = new Inventory();
        inventory.setRoom(room);
        inventory.setDate(date);
        inventory.setPrice(new BigDecimal(price));
        inventory.setTotalCount(availableRooms + 3);
        inventory.setBookedCount(2);
        inventory.setReservedCount(1);
        return inventory;
    }
}
