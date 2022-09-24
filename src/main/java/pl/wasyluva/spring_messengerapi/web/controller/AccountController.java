package pl.wasyluva.spring_messengerapi.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pl.wasyluva.spring_messengerapi.data.service.AccountService;
import pl.wasyluva.spring_messengerapi.domain.userdetails.Account;

import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    // TODO: Create method to register account with more Authorities than 'USER'
    //  The method should be secured with IP and user authorities check (whitelisted IP and 'ADMIN' authority)

    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;

    public AccountController(AccountService accountService, PasswordEncoder passwordEncoder) {
        this.accountService = accountService;
        this.passwordEncoder = passwordEncoder;
    }

    // TODO: Remove after tests
    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts(){
        return new ResponseEntity<>(accountService.getAllAccounts(), HttpStatus.OK);
    }


    @PostMapping("/register")
    public ResponseEntity<Account> registerUserAccount(@RequestBody(required = false) Account newAccount){
        if (newAccount == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Account account = accountService.createUserAccount(newAccount);
        return account != null ?
                new ResponseEntity<>(account, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
