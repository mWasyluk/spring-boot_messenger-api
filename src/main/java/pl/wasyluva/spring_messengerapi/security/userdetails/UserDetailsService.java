package pl.wasyluva.spring_messengerapi.security.userdetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.wasyluva.spring_messengerapi.data.repository.UserDetailsRepository;
import pl.wasyluva.spring_messengerapi.domain.userdetails.UserDetails;

import java.util.Optional;

@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {
    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserDetails> byUsername = userDetailsRepository.findByUsername(username);
        if (byUsername.isPresent())
            return byUsername.get();
        throw new UsernameNotFoundException("Username '" + username + "' could not be found.");
    }
}
