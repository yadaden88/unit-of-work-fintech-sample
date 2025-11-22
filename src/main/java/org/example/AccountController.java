package org.example;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    
    private final AccountRepository accountRepository;
    
    public AccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Account createAccount(@RequestBody CreateAccountRequest request) {
        return accountRepository.save(new Account(
            UUID.randomUUID(),
            request.balance(),
            request.currency()
        ));
    }
    
    @GetMapping
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }
    
    public record CreateAccountRequest(long balance, String currency) {
    }
}

