package com.knowledgeassistant.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AuthServicApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServicApplication.class, args);
	}

}
