package pl.wasyluva.spring_messengerapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import pl.wasyluva.spring_messengerapi.data.repository.UserRepository;
import pl.wasyluva.spring_messengerapi.domain.userdetails.UserAuthority;
import pl.wasyluva.spring_messengerapi.domain.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collections;

@SpringBootApplication
public class SpringMessengerApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringMessengerApiApplication.class, args);
	}


	@Autowired
	private UserRepository userRepository;

	@Bean
	public CommandLineRunner run() {
		return (arg) -> {
			UserDetails userDetails = new UserDetails("admin", "password", Arrays.asList(UserAuthority.USER, UserAuthority.ADMIN));
			userRepository.save(userDetails);
		};
	}

}
