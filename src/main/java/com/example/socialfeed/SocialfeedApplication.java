package com.example.socialfeed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SocialfeedApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocialfeedApplication.class, args);
	}

}
