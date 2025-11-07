package com.dinnervery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DinnerveryApplication {

	public static void main(String[] args) {
		SpringApplication.run(DinnerveryApplication.class, args);
	}

}
