package pl.wasyluva.spring_messengerapi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import pl.wasyluva.spring_messengerapi.data.repository.AccountRepository;
import pl.wasyluva.spring_messengerapi.data.repository.ProfileRepository;
import pl.wasyluva.spring_messengerapi.domain.userdetails.UserAuthority;
import pl.wasyluva.spring_messengerapi.domain.userdetails.Account;
import pl.wasyluva.spring_messengerapi.domain.userdetails.Profile;

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
	private AccountRepository accountRepository;
	@Autowired
	private ProfileRepository profileRepository;

	@Bean
	public CommandLineRunner run() {
		return (arg) -> {
			// Add a UserDetails object with the default admin user to the DB
			Account account1 = new Account("admin", "{bcrypt}$2a$12$psbR2EBlOAXlmrlMCpmSj.Wg/28HjOqRrgsHE1Ud0WTEwiJr5AVZu", Arrays.asList(UserAuthority.USER, UserAuthority.ADMIN));
			Account account2 = new Account("user", "{bcrypt}$2a$12$psbR2EBlOAXlmrlMCpmSj.Wg/28HjOqRrgsHE1Ud0WTEwiJr5AVZu", Collections.singletonList(UserAuthority.USER));

			Account savedUser1 = accountRepository.save(account1);
			Account savedUser2 = accountRepository.save(account2);

			log.info("Added UserDetails with ID: '" + savedUser1.getId() + "' to the database.");
			log.info("Added UserDetails with ID: '" + savedUser2.getId() + "' to the database.");

			// Add two basic Users' profiles to the DB
			Profile sourceUser = new Profile(savedUser1.getId(), "Marek", "Wasyluk", new Calendar.Builder().setDate(1999, 5, 16).build().getTime());
			Profile targetUser = new Profile(savedUser2.getId(), "Jan", "Pasieka", new Calendar.Builder().setDate(1995, 3, 28).build().getTime());
			log.info("Added User with ID: '" + profileRepository.save(sourceUser).getId() + "' to the database.");
			log.info("Added User with ID: '" + profileRepository.save(targetUser).getId() + "' to the database.");
		};
	}

}
