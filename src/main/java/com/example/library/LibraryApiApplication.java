package com.example.library;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LibraryApiApplication {
//	@Autowired
//	private EmailService emailService;

	@Bean
	public ModelMapper mapper() {
		return new ModelMapper();
	}

//	@Bean
//	public CommandLineRunner runner() {
//		return args -> {
//			List<String> emails = Arrays.asList("5fbf12758d-a78ff7@inbox.mailtrap.io");
//			emailService.sendMails("Teste e-mail!", "Testando serviço de e-mail.", emails);
//			System.out.println("EMAILS ENVIADOS");
//		};
//	}

	public static void main(String[] args) {
		SpringApplication.run(LibraryApiApplication.class, args);
	}
}
