package pl.wasyluva.spring_messengerapi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import pl.wasyluva.spring_messengerapi.data.repository.AccountRepository;
import pl.wasyluva.spring_messengerapi.data.repository.ProfileRepository;
import pl.wasyluva.spring_messengerapi.domain.userdetails.Account;
import pl.wasyluva.spring_messengerapi.domain.userdetails.Profile;
import pl.wasyluva.spring_messengerapi.domain.userdetails.UserAuthority;

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
			if (accountRepository.findAll().stream().noneMatch((account ->
				account.getUsername().equals("admin") || account.getUsername().equals("user")
			))) {
				// Create and persist sample Accounts
				Account account1 = new Account(
						"admin",
						"{bcrypt}$2a$12$psbR2EBlOAXlmrlMCpmSj.Wg/28HjOqRrgsHE1Ud0WTEwiJr5AVZu",
						Arrays.asList(UserAuthority.USER, UserAuthority.ADMIN));
				Account account2 = new Account(
						"user",
						"{bcrypt}$2a$12$psbR2EBlOAXlmrlMCpmSj.Wg/28HjOqRrgsHE1Ud0WTEwiJr5AVZu",
						Collections.singletonList(UserAuthority.USER));
//
//			Account savedUser1 = accountRepository.save(account1);
//			Account savedUser2 = accountRepository.save(account2);
//
//			log.info("Persisted Account with ID " + savedUser1.getId());
//			log.info("Persisted Account with ID " + savedUser2.getId());

				// Create sample Profiles, assign them to Accounts and persist
				Profile profile1 = new Profile(
						"Marek",
						"Wasyluk",
						new Calendar.Builder().setDate(1999, 5, 16).build().getTime());
				Profile profile2 = new Profile(
						"Jan",
						"Pasieka",
						new Calendar.Builder().setDate(1995, 3, 28).build().getTime());

				account1.setProfile(profile1);
				account2.setProfile(profile2);

				Account savedUserWithProfile1 = accountRepository.save(account1);
				Account savedUserWithProfile2 = accountRepository.save(account2);

//				log.info("Updated Account with ID " + savedUserWithProfile1.getId() + " with Profile ID " + savedUserWithProfile1.getProfile().getId());
//				log.info("Updated Account with ID " + savedUserWithProfile2.getId() + " with Profile ID " + savedUserWithProfile2.getProfile().getId());
			}
		};
	}

}
