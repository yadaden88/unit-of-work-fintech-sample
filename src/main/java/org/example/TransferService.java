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
        var fromAccount = accountRepository.findById(fromAccountId);
        var toAccount = accountRepository.findById(toAccountId);

        var updatedFromAccount = fromAccount.withBalance(fromAccount.balance() - amount);
        var updatedToAccount = toAccount.withBalance(toAccount.balance() + amount);

        accountRepository.update(updatedFromAccount);
        accountRepository.update(updatedToAccount);

        return transferRepository.save(new Transfer(
            UUID.randomUUID(),
            fromAccountId,
            toAccountId,
            amount
        ));
    }
}

