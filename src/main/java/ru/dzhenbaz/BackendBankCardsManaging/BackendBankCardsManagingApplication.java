package ru.dzhenbaz.BackendBankCardsManaging;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BackendBankCardsManagingApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendBankCardsManagingApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}
}
