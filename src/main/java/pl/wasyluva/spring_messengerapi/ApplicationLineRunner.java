package pl.wasyluva.spring_messengerapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import pl.wasyluva.spring_messengerapi.data.repository.AccountRepository;
import pl.wasyluva.spring_messengerapi.domain.userdetails.Account;
import pl.wasyluva.spring_messengerapi.domain.userdetails.UserAuthority;

import java.util.Arrays;

@Configuration
public class ApplicationLineRunner implements CommandLineRunner {
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private String localHostUrl;

    @Override
    public void run(String... args) {
		// print local URL of the running API server
		System.out.println(">>>>>>>>>>>>> APP IS RUNNING UNDER THE FOLLOWING URL >>>>>>>>>>>>>>> " + localHostUrl);
		// create and persist the admin Account if it does not exist
        if (accountRepository.findAll().stream().noneMatch((account ->
				account.getUsername().equals("admin")))) {
				// create an Account with the admin auth
				Account account1 = new Account(
						"admin",
						"{bcrypt}$2a$12$psbR2EBlOAXlmrlMCpmSj.Wg/28HjOqRrgsHE1Ud0WTEwiJr5AVZu",
						Arrays.asList(UserAuthority.USER, UserAuthority.ADMIN));

                // persist the Account
				Account savedUserWithProfile1 = accountRepository.save(account1);
			}
    }
}
