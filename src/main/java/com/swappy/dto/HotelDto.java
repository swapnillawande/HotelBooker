package com.swappy.dto;


import java.util.List;

import com.swappy.entities.HotelContactInfo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class HotelDto {


	private Long id;

	private String name;
	
	private String city;
	
	private List<String> amenities;

	private List<String> photos;
	
	private HotelContactInfo contactInfo;

	private Boolean isActive;

	
	
	public HotelDto() {
		// TODO Auto-generated constructor stub
	}

	public HotelDto(Long id, String name, String city, List<String> amenities, List<String> photos,
			HotelContactInfo contactInfo, Boolean isActive) {
		super();
		this.id = id;
		this.name = name;
		this.city = city;
		this.amenities = amenities;
		this.photos = photos;
		this.contactInfo = contactInfo;
		this.isActive = isActive;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
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

	public HotelContactInfo getContactInfo() {
		return contactInfo;
	}

	public void setContactInfo(HotelContactInfo contactInfo) {
		this.contactInfo = contactInfo;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}
	
	
	
	
}




