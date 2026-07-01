package cl.techstore.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TechstoreApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TechstoreApiApplication.class, args);
	}
	
	@org.springframework.context.annotation.Bean
	public com.fasterxml.jackson.databind.ObjectMapper objectMapper() {
    	return new com.fasterxml.jackson.databind.ObjectMapper();
}

}
