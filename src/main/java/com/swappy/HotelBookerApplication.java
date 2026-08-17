package com.swappy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HotelBookerApplication {

	public static void main(String[] args) {
		SpringApplication.run(HotelBookerApplication.class, args);
	}

}
