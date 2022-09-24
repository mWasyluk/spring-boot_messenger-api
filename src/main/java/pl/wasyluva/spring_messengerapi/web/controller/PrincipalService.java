package pl.wasyluva.spring_messengerapi.web.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.wasyluva.spring_messengerapi.data.repository.AccountRepository;
import pl.wasyluva.spring_messengerapi.domain.userdetails.Account;

import java.util.Optional;
import java.util.UUID;

@Service
public class PrincipalService {
    private final AccountRepository accountRepository;

    public PrincipalService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public UUID getPrincipalAccountId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ((Account) authentication.getPrincipal()).getId();
    }

    public UUID getPrincipalProfileId(){
        Optional<Account> byId = accountRepository.findById(getPrincipalAccountId());
        return byId.map( account -> account.getProfile().getId()).orElse(null);
    }

}
