package com.swappy.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.swappy.entities.Hotel;
import com.swappy.repository.InventoryRepository;
import com.swappy.service.impl.InventoryServiceImpl;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTests {

    @Mock
    private InventoryRepository inventoryRepository;

    private InventoryServiceImpl inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryServiceImpl(inventoryRepository, new ModelMapper());
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
}
