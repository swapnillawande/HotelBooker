package com.swappy.dto;

import com.swappy.entities.Hotel;

public class HotelPriceDto {

	private Hotel hotel;
	private Double price;
	
	
	public HotelPriceDto() {

	}
	
	public HotelPriceDto(Hotel hotel, Double price) {
		super();
		this.hotel = hotel;
		this.price = price;
	}

	public Hotel getHotel() {
		return hotel;
	}
	public void setHotel(Hotel hotel) {
		this.hotel = hotel;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
  
	
	
	
}
