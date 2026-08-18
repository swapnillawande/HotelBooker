package com.swappy.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.swappy.dto.HotelDto;
import com.swappy.dto.HotelSearchDto;
import com.swappy.dto.RoomOfferDto;
import com.swappy.entities.Hotel;
import com.swappy.entities.Inventory;
import com.swappy.entities.Room;
import com.swappy.exception.ResourceNotFoundException;
import com.swappy.repository.HotelRepository;
import com.swappy.repository.InventoryRepository;
import com.swappy.service.InventoryService;

import jakarta.transaction.Transactional;

@Service
public class InventoryServiceImpl implements InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryServiceImpl.class);

    private final InventoryRepository inventoryRepository;
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;

    public InventoryServiceImpl(
            InventoryRepository inventoryRepository,
            HotelRepository hotelRepository,
            ModelMapper modelMapper) {
        this.inventoryRepository = inventoryRepository;
        this.hotelRepository = hotelRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public void initializeRoomForAYear(Room room) {

        logger.info("Initializing inventory for roomId={} for next 1 year", room.getId());

        LocalDate today = LocalDate.now();
        LocalDate enddate = today.plusYears(1);

        int createdCount = 0;

        for (; !today.isAfter(enddate); today = today.plusDays(1)) {

            boolean inventoryAlreadyExists = inventoryRepository
                    .existsByHotelIdAndRoomIdAndDate(
                            room.getHotel().getId(),
                            room.getId(),
                            today
                    );

            if (inventoryAlreadyExists) {
                logger.debug("Inventory already exists for roomId={} on date={}", room.getId(), today);
                continue;
            }

            Inventory inventory = new Inventory();
            inventory.setHotel(room.getHotel());
            inventory.setRoom(room);
            inventory.setBookedCount(0);
            inventory.setReservedCount(0);
            inventory.setCity(room.getHotel().getCity());
            inventory.setDate(today);
            inventory.setPrice(room.getBasePrice());
            inventory.setSurgeFactor(BigDecimal.ONE);
            inventory.setTotalCount(room.getTotalCount());
            inventory.setClosed(false);

            inventoryRepository.save(inventory);
            createdCount++;
        }

        logger.info("Inventory initialization completed for roomId={}. Created {} records", 
                room.getId(), createdCount);
    }

    @Override
    public void deleteFutureInventories(Room room) {

        logger.warn("Deleting future inventories for roomId={} from today onwards", room.getId());

        LocalDate today = LocalDate.now();
        inventoryRepository.deleteByDateAfterAndRoom(today, room);

        logger.info("Future inventories deleted for roomId={}", room.getId());
    }

    @Override
    public void deleteInventoriesByRoomId(Long roomId) {

        logger.warn("Deleting ALL inventories for roomId={}", roomId);

        inventoryRepository.deleteByRoom_Id(roomId);
        inventoryRepository.flush();

        logger.info("All inventories deleted for roomId={}", roomId);
    }

    @Override
    @Transactional
    public Page<HotelDto> searchHotels(HotelSearchDto hotelSearchDto) {
		if (!hotelSearchDto.getEndDate().isAfter(hotelSearchDto.getStartDate())) {
			throw new IllegalArgumentException("End date must be after start date");
		}

        logger.info("Searching hotels in city={} from {} to {} for {} rooms",
                hotelSearchDto.getCity(),
                hotelSearchDto.getStartDate(),
                hotelSearchDto.getEndDate(),
                hotelSearchDto.getRoomCount());

        Pageable pageable = PageRequest.of(
                hotelSearchDto.getPage(),
                hotelSearchDto.getSize()
        );

        long dateCount = ChronoUnit.DAYS.between(
                hotelSearchDto.getStartDate(),
                hotelSearchDto.getEndDate()
        );

        Page<Hotel> hotelPage = inventoryRepository.findHotelsWithAvailableInventory(
                hotelSearchDto.getCity(),
                hotelSearchDto.getStartDate(),
                hotelSearchDto.getEndDate(),
                hotelSearchDto.getRoomCount(),
                dateCount,
                pageable
        );

        logger.info("Found {} hotels for search query", hotelPage.getTotalElements());

        return hotelPage.map(hotel -> {
            HotelDto result = modelMapper.map(hotel, HotelDto.class);
            result.setStartingPrice(inventoryRepository.findMinimumAvailablePrice(
                    hotel.getId(),
                    hotelSearchDto.getStartDate(),
                    hotelSearchDto.getEndDate(),
                    hotelSearchDto.getRoomCount(),
                    dateCount
            ));
            return result;
        });
    }

    @Override
    @Transactional
    public List<RoomOfferDto> getRoomOffers(
            Long hotelId,
            LocalDate startDate,
            LocalDate endDate,
            Integer roomsCount) {
        validateOfferRequest(startDate, endDate, roomsCount);
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + hotelId));
        if (!Boolean.TRUE.equals(hotel.getIsActive())) {
            throw new IllegalStateException("Hotel is not active for booking");
        }

        int nights = Math.toIntExact(ChronoUnit.DAYS.between(startDate, endDate));
        Map<Long, List<Inventory>> inventoryByRoom = inventoryRepository.findAvailableRoomInventory(
                        hotelId, startDate, endDate, roomsCount).stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        inventory -> inventory.getRoom().getId(),
                        LinkedHashMap::new,
                        java.util.stream.Collectors.toList()));

        return inventoryByRoom.values().stream()
                .filter(inventories -> inventories.size() == nights)
                .map(inventories -> toRoomOffer(inventories, nights, roomsCount))
                .toList();
    }

    private RoomOfferDto toRoomOffer(List<Inventory> inventories, int nights, int roomsCount) {
        Room room = inventories.get(0).getRoom();
        int availableRooms = inventories.stream()
                .mapToInt(inventory -> inventory.getTotalCount()
                        - inventory.getBookedCount()
                        - inventory.getReservedCount())
                .min()
                .orElse(0);
        BigDecimal singleRoomTotal = inventories.stream()
                .map(Inventory::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPrice = singleRoomTotal.multiply(BigDecimal.valueOf(roomsCount));
        BigDecimal nightlyPrice = singleRoomTotal
                .divide(BigDecimal.valueOf(nights), 2, RoundingMode.HALF_UP);

        return new RoomOfferDto(
                room.getId(),
                room.getType(),
                room.getAmenities() == null ? List.of() : List.copyOf(room.getAmenities()),
                room.getPhotos() == null ? List.of() : List.copyOf(room.getPhotos()),
                room.getCapacity(),
                availableRooms,
                nights,
                nightlyPrice,
                totalPrice);
    }

    private void validateOfferRequest(LocalDate startDate, LocalDate endDate, Integer roomsCount) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Check-in and check-out dates are required");
        }
        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past");
        }
        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }
        if (roomsCount == null || roomsCount < 1 || roomsCount > 20) {
            throw new IllegalArgumentException("Room count must be between 1 and 20");
        }
    }
}
