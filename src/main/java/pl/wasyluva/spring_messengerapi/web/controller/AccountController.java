package pl.wasyluva.spring_messengerapi.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.wasyluva.spring_messengerapi.data.service.AccountService;
import pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponse;
import pl.wasyluva.spring_messengerapi.domain.userdetails.Account;
import pl.wasyluva.spring_messengerapi.web.controller.support.PrincipalService;

@RequiredArgsConstructor

@RestController
@RequestMapping("/accounts")
public class AccountController {
    // TODO: Create method to register account with more Authorities than 'USER'
    //  The method should be secured with IP and user authorities check (whitelisted IP and 'ADMIN' authority)

    private final AccountService accountService;
    private final PrincipalService principalService;

    // TODO: Remove after tests
    @GetMapping
    public ResponseEntity<?> getAllAccounts(){
        return accountService.getAllAccounts().getResponseEntity();
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUserAccount(@RequestBody Account.AccountRegistrationForm newAccountForm){
        ServiceResponse<?> userAccount = accountService.createUserAccount(newAccountForm);
        return new ResponseEntity<>(userAccount, userAccount.getStatusCode());
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUserAccount(){
        ServiceResponse<?> userAccount = accountService.deleteUserAccount(principalService.getPrincipalAccountId());
        return new ResponseEntity<>(userAccount, userAccount.getStatusCode());
    }
}
