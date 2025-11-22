package org.example;

import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Function;

public class UnitOfWork {

    private static final int MAX_RETRIES = 10;

    private final TransactionTemplate transactionTemplate;

    public UnitOfWork(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    public <T> T execute(Function<Batch, T> businessLogic) {
        int attempt = 0;
        OptimisticLockException lastException = null;

        while (attempt < MAX_RETRIES) {
            try {
                Batch batch = new Batch();
                T result = businessLogic.apply(batch);
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
        transactionTemplate.executeWithoutResult(status -> {
            try {
                batch.executeInserts();
                batch.executeUpdates();

            } catch (OptimisticLockException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("Failed to commit unit of work", e);
            }
        });
    }
}

