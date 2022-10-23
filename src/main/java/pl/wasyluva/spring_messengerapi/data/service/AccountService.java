package pl.wasyluva.spring_messengerapi.data.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.wasyluva.spring_messengerapi.data.repository.AccountRepository;
import pl.wasyluva.spring_messengerapi.domain.userdetails.Account;
import pl.wasyluva.spring_messengerapi.domain.userdetails.UserAuthority;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AccountService implements UserDetailsService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // TODO: Remove after tests
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    public Account loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Account> byEmail = accountRepository.findByEmail(username);
        if (!byEmail.isPresent()){
            log.debug("Account with email " + username + " does not exist");
            throw new UsernameNotFoundException("Account with email " + username + " could not be found.");
        }
        log.debug("Account with email " + username + " has been loaded");
        return byEmail.get();
    }

    public Account createUserAccount(@NonNull Account account){
        if (account.getId() != null) {
            Optional<Account> byId = accountRepository.findById(account.getId());
            if (byId.isPresent()) {
                log.debug("User with ID " + account.getId() + " already exists");
                return null;
            }
        }
        Optional<Account> byEmail = accountRepository.findByEmail(account.getEmail());
        if (byEmail.isPresent()){
            log.debug("User with email " + account.getEmail() + " already exists");
            return null;
        }

        Account newAccount = new Account(account.getEmail(), passwordEncoder.encode(account.getPassword()), Collections.singleton(UserAuthority.USER));
        newAccount.setId(account.getId());
        log.debug("Account created");

        return accountRepository.save(newAccount);
    }

    // TODO: Add method createAdminAccount()

}
