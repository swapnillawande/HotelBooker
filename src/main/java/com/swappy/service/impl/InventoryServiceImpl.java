package com.swappy.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.swappy.dto.HotelDto;
import com.swappy.dto.HotelSearchDto;
import com.swappy.entities.Hotel;
import com.swappy.entities.Inventory;
import com.swappy.entities.Room;
import com.swappy.repository.InventoryRepository;
import com.swappy.service.InventoryService;

@Service
public class InventoryServiceImpl implements InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryServiceImpl.class);

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ModelMapper modelMapper;

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
    public Page<HotelDto> searchHotels(HotelSearchDto hotelSearchDto) {

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
        ) + 1;

        Page<Hotel> hotelPage = inventoryRepository.findHotelsWithAvailableInventory(
                hotelSearchDto.getCity(),
                hotelSearchDto.getStartDate(),
                hotelSearchDto.getEndDate(),
                hotelSearchDto.getRoomCount(),
                dateCount,
                pageable
        );

        logger.info("Found {} hotels for search query", hotelPage.getTotalElements());

        return hotelPage.map(ele -> modelMapper.map(ele, HotelDto.class));
    }
}