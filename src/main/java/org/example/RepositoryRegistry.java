package org.example;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RepositoryRegistry {

    private final Map<Class<?>, Repository<?>> repositories = new HashMap<>();

    public RepositoryRegistry(
        AccountRepository accountRepository,
        TransferRepository transferRepository
    ) {
        repositories.put(Account.class, accountRepository);
        repositories.put(Transfer.class, transferRepository);
    }

    @SuppressWarnings("unchecked")
    public <T> Repository<T> getRepository(Class<?> entityClass) {
        var repository = repositories.get(entityClass);
        if (repository == null) {
            throw new IllegalStateException(
                "No repository registered for entity type: " + entityClass.getName() + ". " +
                    "Make sure to register the repository in RepositoryRegistry."
            );
        }
        return (Repository<T>) repository;
    }
}

