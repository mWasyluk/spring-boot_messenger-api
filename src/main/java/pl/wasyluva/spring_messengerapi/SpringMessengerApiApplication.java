package pl.wasyluva.spring_messengerapi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import pl.wasyluva.spring_messengerapi.data.repository.UserDetailsRepository;
import pl.wasyluva.spring_messengerapi.data.repository.UserProfileRepository;
import pl.wasyluva.spring_messengerapi.domain.userdetails.UserAuthority;
import pl.wasyluva.spring_messengerapi.domain.userdetails.UserDetails;
import pl.wasyluva.spring_messengerapi.domain.userdetails.UserProfile;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;

@SpringBootApplication
@Slf4j
public class SpringMessengerApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringMessengerApiApplication.class, args);
	}


	@Autowired
	private UserDetailsRepository userDetailsRepository;
	@Autowired
	private UserProfileRepository userProfileRepository;

	@Bean
	public CommandLineRunner run() {
		return (arg) -> {
			// Add a UserDetails object with the default admin user to the DB
			UserDetails userDetails1 = new UserDetails("admin", "{bcrypt}$2a$12$psbR2EBlOAXlmrlMCpmSj.Wg/28HjOqRrgsHE1Ud0WTEwiJr5AVZu", Arrays.asList(UserAuthority.USER, UserAuthority.ADMIN));
			UserDetails userDetails2 = new UserDetails("user", "{bcrypt}$2a$12$psbR2EBlOAXlmrlMCpmSj.Wg/28HjOqRrgsHE1Ud0WTEwiJr5AVZu", Collections.singletonList(UserAuthority.USER));

			UserDetails savedUser1 = userDetailsRepository.save(userDetails1);
			UserDetails savedUser2 = userDetailsRepository.save(userDetails2);

			log.info("Added UserDetails with ID: '" + savedUser1.getId() + "' to the database.");
			log.info("Added UserDetails with ID: '" + savedUser2.getId() + "' to the database.");

			// Add two basic Users' profiles to the DB
			UserProfile sourceUser = new UserProfile(savedUser1.getId(), "Marek", "Wasyluk", new Calendar.Builder().setDate(1999, 5, 16).build().getTime());
			UserProfile targetUser = new UserProfile(savedUser2.getId(), "Jan", "Pasieka", new Calendar.Builder().setDate(1995, 3, 28).build().getTime());
			log.info("Added User with ID: '" + userProfileRepository.save(sourceUser).getId() + "' to the database.");
			log.info("Added User with ID: '" + userProfileRepository.save(targetUser).getId() + "' to the database.");
		};
	}

}
