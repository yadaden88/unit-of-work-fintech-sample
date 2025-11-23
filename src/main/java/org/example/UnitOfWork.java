package org.example;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Function;

@Component
public class UnitOfWork {

    private static final int MAX_RETRIES = 10;

    private final TransactionTemplate transactionTemplate;
    private final RepositoryRegistry repositoryRegistry;

    public UnitOfWork(TransactionTemplate transactionTemplate, RepositoryRegistry repositoryRegistry) {
        this.transactionTemplate = transactionTemplate;
        this.repositoryRegistry = repositoryRegistry;
    }

    public <T> T executeRetriable(Function<Batch, T> idempotentRetriableLogic) {
        int attempt = 0;
        OptimisticLockException lastException = null;

        while (attempt < MAX_RETRIES) {
            try {
                Batch batch = new Batch();
                T result = idempotentRetriableLogic.apply(batch);
                commit(batch);
                return result;
            } catch (OptimisticLockException e) {
                lastException = e;
                attempt++;
            }
        }

        throw new OptimisticLockException(
            "Failed to complete operation after " + MAX_RETRIES + " attempts due to concurrent modifications",
            lastException
        );
    }

    private void commit(Batch batch) {
        transactionTemplate.executeWithoutResult(_ -> {
            try {
                batch.executeInserts(repositoryRegistry);
                batch.executeUpdates(repositoryRegistry);
            } catch (OptimisticLockException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("Failed to commit unit of work", e);
            }
        });
    }
}

