package com.swappy.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Data;

@Data
public class RoomDto {


	private Long id;
	
	@NotBlank(message = "Room type is required")
	private String type;
	
	@NotNull(message = "Base price is required")
	@DecimalMin(value = "0.01", message = "Base price must be greater than zero")
	private BigDecimal basePrice;
	
	private List<String> amenities;

	private List<String> photos;
	
	@NotNull(message = "Total room count is required")
	@Positive(message = "Total room count must be positive")
	private Integer totalCount;
	
	@NotNull(message = "Room capacity is required")
	@Positive(message = "Room capacity must be positive")
	private Integer capacity;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public BigDecimal getBasePrice() {
		return basePrice;
	}

	public void setBasePrice(BigDecimal basePrice) {
		this.basePrice = basePrice;
	}

	public List<String> getAmenities() {
		return amenities;
	}

	public void setAmenities(List<String> amenities) {
		this.amenities = amenities;
	}

	public List<String> getPhotos() {
		return photos;
	}

	public void setPhotos(List<String> photos) {
		this.photos = photos;
	}

	public Integer getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}

	public Integer getCapacity() {
		return capacity;
	}

	public void setCapacity(Integer capacity) {
		this.capacity = capacity;
	}
	
	
}
