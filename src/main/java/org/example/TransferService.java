package org.example;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;

    public TransferService(AccountRepository accountRepository, TransferRepository transferRepository) {
        this.accountRepository = accountRepository;
        this.transferRepository = transferRepository;
    }

    @Transactional
    public Transfer createTransfer(UUID fromAccountId, UUID toAccountId, long amount) {
        // Fetch complete account entities
        Account fromAccount = accountRepository.findById(fromAccountId);
        Account toAccount = accountRepository.findById(toAccountId);

        // Perform balance calculations in service layer
        Account updatedFromAccount = new Account(
            fromAccount.id(),
            fromAccount.balance() - amount,
            fromAccount.currency()
        );
        Account updatedToAccount = new Account(
            toAccount.id(),
            toAccount.balance() + amount,
            toAccount.currency()
        );

        // Update accounts through repository
        accountRepository.update(updatedFromAccount);
        accountRepository.update(updatedToAccount);

        // Create transfer record
        Transfer transfer = new Transfer(
            UUID.randomUUID(),
            fromAccountId,
            toAccountId,
            amount
        );

        return transferRepository.save(transfer);
    }
}

