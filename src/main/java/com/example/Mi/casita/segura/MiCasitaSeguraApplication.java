package com.example.Mi.casita.segura;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MiCasitaSeguraApplication {

	public static void main(String[] args) {
		SpringApplication.run(MiCasitaSeguraApplication.class, args);
	}

}
