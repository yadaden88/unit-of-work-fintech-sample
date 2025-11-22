package org.example;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static java.util.UUID.randomUUID;

@Service
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionTemplate transactionTemplate;

    public TransferService(
        AccountRepository accountRepository,
        TransactionTemplate transactionTemplate
    ) {
        this.accountRepository = accountRepository;
        this.transactionTemplate = transactionTemplate;
    }

    public Transfer createTransfer(UUID fromAccountId, UUID toAccountId, long amount) {
        return new UnitOfWork(transactionTemplate).execute(batch -> {
            var fromAccount = accountRepository.findById(fromAccountId);
            var toAccount = accountRepository.findById(toAccountId);

            var updatedFromAccount = fromAccount.withBalance(fromAccount.balance() - amount);
            var updatedToAccount = toAccount.withBalance(toAccount.balance() + amount);

            var transfer = new Transfer(
                randomUUID(),
                fromAccountId,
                toAccountId,
                amount
            );

            batch.update(updatedFromAccount);
            batch.update(updatedToAccount);
            batch.insert(transfer);

            return transfer;
        });
    }
}

