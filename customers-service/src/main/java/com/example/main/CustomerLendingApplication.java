package com.example.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CustomerLendingApplication {

	public static void main(String[] args) {
		SpringApplication.run(CustomerLendingApplication.class, args);
	}

}
