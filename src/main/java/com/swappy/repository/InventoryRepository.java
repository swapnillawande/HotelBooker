package com.swappy.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.swappy.entities.Hotel;
import com.swappy.entities.Inventory;
import com.swappy.entities.Room;

import jakarta.persistence.LockModeType;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long>{

	public void deleteByDateAfterAndRoom(LocalDate date, Room room);
	
	public long deleteByRoom_Id(Long roomId);

	public boolean existsByHotelIdAndRoomIdAndDate(Long hotelId, Long roomId, LocalDate date);
	
	
	@Query(""" 
			SELECT DISTINCT i.hotel
			FROM Inventory i
			WHERE i.city = :city 
				AND i.date >= :startDate AND i.date < :endDate
				AND i.closed = false 
				AND (i.totalCount - i.bookedCount - i.reservedCount) >= :roomsCount
			GROUP BY i.hotel, i.room
			HAVING COUNT(i.date) = :dateCount
			""")
	public Page<Hotel> findHotelsWithAvailableInventory(
			
			@Param("city") String city,
			@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate,
			@Param("roomsCount") Integer roomsCount,
			@Param("dateCount") Long dateCount,
			Pageable pageable

			
			);

	@Query("""
			SELECT MIN(i.price)
			FROM Inventory i
			WHERE i.hotel.id = :hotelId
			  AND i.date >= :startDate AND i.date < :endDate
			  AND i.closed = false
			  AND (i.totalCount - i.bookedCount - i.reservedCount) >= :roomsCount
			  AND i.room.id IN (
			      SELECT i2.room.id
			      FROM Inventory i2
			      WHERE i2.hotel.id = :hotelId
			        AND i2.date >= :startDate AND i2.date < :endDate
			        AND i2.closed = false
			        AND (i2.totalCount - i2.bookedCount - i2.reservedCount) >= :roomsCount
			      GROUP BY i2.room.id
			      HAVING COUNT(i2.date) = :dateCount
			  )
			""")
	BigDecimal findMinimumAvailablePrice(
			@Param("hotelId") Long hotelId,
			@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate,
			@Param("roomsCount") Integer roomsCount,
			@Param("dateCount") Long dateCount
	);
	
	
	@Query("""
		    SELECT i
		    FROM Inventory i
		    WHERE i.room.id = :roomId
		      AND i.date >= :startDate AND i.date < :endDate
		      AND i.closed = false
		      AND (i.totalCount - i.bookedCount - i.reservedCount) >= :roomsCount
		""")
		@Lock(LockModeType.PESSIMISTIC_WRITE)
		List<Inventory> findAndLockAvailableInventory(
		    @Param("roomId") Long roomId,
		    @Param("startDate") LocalDate startDate,
		    @Param("endDate") LocalDate endDate,
		    @Param("roomsCount") Integer roomsCount
		);

	@Query("""
			SELECT i
			FROM Inventory i
			WHERE i.room.id = :roomId
			  AND i.date >= :startDate AND i.date < :endDate
			""")
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	List<Inventory> findAndLockInventoryForBooking(
			@Param("roomId") Long roomId,
			@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate
	);
	
	
}
