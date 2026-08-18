package com.swappy.config;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.swappy.entities.Hotel;
import com.swappy.entities.Room;
import com.swappy.entities.User;
import com.swappy.entities.enums.Role;
import com.swappy.repository.HotelRepository;
import com.swappy.repository.RoomRepository;
import com.swappy.repository.UserRepository;
import com.swappy.service.InventoryService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@Profile("demo")
@RequiredArgsConstructor
public class DemoDataSeeder implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final InventoryService inventoryService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedUser("demo@stayly.local", "Stayly Guest", Role.GUEST);
        User manager = seedUser("manager@stayly.local", "Stayly Manager", Role.HOTEL_MANAGER);

        if (hotelRepository.count() > 0) {
            logger.info("Demo data already exists; skipping seed");
            return;
        }

        seedHotel(manager,
                "Stayly Berlin Mitte",
                "Berlin",
                List.of("Free Wi-Fi", "24-hour reception", "Guest kitchen", "Bar"),
                List.of(
                        room("Twin private room", "62.00", 2, 12,
                                List.of("Private bathroom", "Linen included", "Quiet floor")),
                        room("Family room", "88.00", 4, 8,
                                List.of("Four beds", "Private bathroom", "Kids welcome")),
                        room("Shared dorm bed", "22.00", 6, 24,
                                List.of("Secure locker", "Reading light", "Shared lounge"))));

        seedHotel(manager,
                "Stayly Hamburg Harbour",
                "Hamburg",
                List.of("Free Wi-Fi", "Breakfast", "Games room", "Family spaces"),
                List.of(
                        room("Double room", "58.00", 2, 10,
                                List.of("Harbour view", "Private bathroom", "Towels")),
                        room("Shared dorm bed", "19.00", 8, 32,
                                List.of("Secure locker", "Reading light", "Guest kitchen"))));

        logger.info("Demo profile ready with {} hotels", hotelRepository.count());
    }

    private User seedUser(String email, String name, Role role) {
        var existing = userRepository.findByEmailIgnoreCase(email);
        if (existing.isPresent()) {
            return existing.get();
        }
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPassword(passwordEncoder.encode("StaylyDemo123!"));
        user.setRoles(java.util.Set.of(role));
        return userRepository.save(user);
    }

    private void seedHotel(User owner, String name, String city, List<String> amenities, List<Room> rooms) {
        Hotel hotel = new Hotel();
        hotel.setName(name);
        hotel.setCity(city);
        hotel.setAmenities(amenities);
        hotel.setPhotos(List.of());
        hotel.setIsActive(false);
        hotel.setOwner(owner);
        hotel = hotelRepository.save(hotel);

        for (Room room : rooms) {
            room.setHotel(hotel);
        }
        rooms = roomRepository.saveAll(rooms);
        hotel.setRooms(rooms);
        hotel.setIsActive(true);
        hotelRepository.save(hotel);

        for (Room room : rooms) {
            inventoryService.initializeRoomForAYear(room);
        }
    }

    private Room room(String type, String price, int capacity, int totalCount, List<String> amenities) {
        Room room = new Room();
        room.setType(type);
        room.setBasePrice(new BigDecimal(price));
        room.setCapacity(capacity);
        room.setTotalCount(totalCount);
        room.setAmenities(amenities);
        room.setPhotos(List.of());
        return room;
    }
}
