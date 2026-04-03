package com.swappy.dto;

import com.swappy.entities.User;
import com.swappy.entities.enums.Gender;


public class GuestDto {


	private Long id;
	
	private User user;
	
	private String name;
	
	private Gender gender;
	
	private Integer age;

	public GuestDto() {

	}

	public GuestDto(Long id, User user, String name, Gender gender, Integer age) {
		super();
		this.id = id;
		this.user = user;
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

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
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
