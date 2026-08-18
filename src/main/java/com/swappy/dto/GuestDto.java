package com.swappy.dto;

import com.swappy.entities.enums.Gender;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public class GuestDto {


	private Long id;
	
	@NotBlank(message = "Guest name is required")
	private String name;
	
	@NotNull(message = "Guest gender is required")
	private Gender gender;
	
	@NotNull(message = "Guest age is required")
	@Min(value = 1, message = "Guest age must be at least 1")
	@Max(value = 120, message = "Guest age must be at most 120")
	private Integer age;

	public GuestDto() {

	}

	public GuestDto(Long id, String name, Gender gender, Integer age) {
		super();
		this.id = id;
		this.name = name;
		this.gender = gender;
		this.age = age;
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

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}
	
	
	
}
