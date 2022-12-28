package pl.wasyluva.spring_messengerapi.web.http.support;

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
        if (!byId.isPresent()){
            return null;
        }
        if (byId.get().getProfile() == null){
            return null;
        }
        return byId.get().getProfile().getId();
    }

}
