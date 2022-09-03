package pl.wasyluva.spring_messengerapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import pl.wasyluva.spring_messengerapi.data.repository.UserRepository;
import pl.wasyluva.spring_messengerapi.domain.userdetails.UserAuthority;
import pl.wasyluva.spring_messengerapi.domain.userdetails.UserDetails;

import javax.sql.DataSource;
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
			UserDetails userDetails = new UserDetails("admin", "{bcrypt}$2a$12$DMm64rBiqoiI.gARFG5mJOBotWkrazCq.0v4iwQmgj4hmpQ010zcq", Arrays.asList(UserAuthority.USER, UserAuthority.ADMIN));
			userRepository.save(userDetails);
		};
	}

}
