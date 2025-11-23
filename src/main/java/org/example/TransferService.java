package org.example;

import org.springframework.stereotype.Service;

import java.util.UUID;

import static java.util.UUID.randomUUID;

@Service
public class TransferService {

    private final AccountRepository accountRepository;
    private final UnitOfWork unitOfWork;

    public TransferService(
        AccountRepository accountRepository,
        UnitOfWork unitOfWork
    ) {
        this.accountRepository = accountRepository;
        this.unitOfWork = unitOfWork;
    }

    public Transfer createTransfer(UUID fromAccountId, UUID toAccountId, long amount) {
        return unitOfWork.executeRetriable(batch -> {
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

