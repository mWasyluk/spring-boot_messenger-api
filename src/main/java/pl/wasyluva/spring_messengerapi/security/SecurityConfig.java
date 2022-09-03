package pl.wasyluva.spring_messengerapi.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;

import javax.sql.DataSource;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder (){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService jdbcUserDetailsService(DataSource dataSource) {
        String usersByUsernameQuery = "SELECT username, password, enabled FROM users WHERE username = ?";
        String authsByUsernameQuery = "SELECT username, authority FROM users_authorities WHERE username = ?";

        JdbcUserDetailsManager usersManager = new JdbcUserDetailsManager(dataSource);

        usersManager.setUsersByUsernameQuery (usersByUsernameQuery);
        usersManager.setAuthoritiesByUsernameQuery (authsByUsernameQuery);

        return usersManager;
    }
}
