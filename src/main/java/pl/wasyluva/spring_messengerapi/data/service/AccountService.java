package pl.wasyluva.spring_messengerapi.data.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.wasyluva.spring_messengerapi.data.repository.AccountRepository;
import pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponse;
import pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponseMessages;
import pl.wasyluva.spring_messengerapi.domain.userdetails.Account;
import pl.wasyluva.spring_messengerapi.util.BcryptUtils;
import pl.wasyluva.spring_messengerapi.util.UuidUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class AccountService implements UserDetailsService {
    private final AccountRepository accountRepository;

    // TODO: Remove after tests
    public ServiceResponse<?> getAllAccounts() {
        return new ServiceResponse<List<Account>>(accountRepository.findAll(), HttpStatus.OK);
    }

    @Override
    public Account loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        Optional<Account> byEmail = accountRepository.findByEmail(email);
        if (!byEmail.isPresent()){
            log.debug("Account with email " + email + " does not exist");
            throw new UsernameNotFoundException("Account with email " + email + " could not be found.");
        }
        log.debug("Account with email " + email + " has been loaded");
        return byEmail.get();
    }

    // TODO: test
    public ServiceResponse<?> getAccountById(@NonNull UUID requestingAccountUuid){
        Optional<Account> byId = accountRepository.findById(requestingAccountUuid);
        if (!byId.isPresent()){
            return ServiceResponse.INCORRECT_ID;
        }

        return new ServiceResponse<>(byId.get(), HttpStatus.OK);
    }

    public ServiceResponse<?> createAccount(@NonNull Account.AccountRegistrationForm form){
        Account account = new Account(form.getEmail(), form.getPassword());

        Optional<Account> byId = accountRepository.findById(account.getId());
        while (byId.isPresent()) {
            log.debug("Not possible just happened. The same UUID has been generated for two accounts. UUID: " + account.getId());
            account.setId(UUID.randomUUID());
            byId = accountRepository.findById(account.getId());
        }

        Optional<Account> byEmail = accountRepository.findByEmail(account.getEmail());
        if (byEmail.isPresent()){
            log.debug("User with email " + account.getEmail() + " already exists");
            return new ServiceResponse<>(ServiceResponseMessages.EMAIL_ALREADY_IN_USE, HttpStatus.CONFLICT);
        }

        String password = account.getPassword();
        if (!BcryptUtils.BCRYPT_PATTERN.matcher(password).matches()) {
            log.debug("Provided password is not a valid BCrypt pattern. Password: " + password);
            return new ServiceResponse<>(ServiceResponseMessages.PLAIN_PASSWORD_ERROR, HttpStatus.BAD_REQUEST);
        }
        String bcryptPrefix = "{bcrypt}";
        if (!password.startsWith(bcryptPrefix)){
            account.setPassword(bcryptPrefix + password);
        }

        log.debug("New account has been created. UUID: " + account.getId());
        return new ServiceResponse<>(accountRepository.save(account), HttpStatus.CREATED);
    }

    // TODO: Add method createAdminAccount()

    public ServiceResponse<?> deleteAccount(@NonNull UUID requestingUserUuid){
        Optional<Account> byId = accountRepository.findById(requestingUserUuid);
        if (!byId.isPresent()){
            return ServiceResponse.INCORRECT_ID;
        }
        this.accountRepository.deleteById(requestingUserUuid);
        return ServiceResponse.OK;
    }

    public ServiceResponse<?> deleteAccount(@NonNull String requestingUserStringUuid){
        if (!UuidUtils.isStringCorrectUuid(requestingUserStringUuid)){
            return ServiceResponse.INCORRECT_ID;
        }
        return deleteAccount(UUID.fromString(requestingUserStringUuid));
    }


}
