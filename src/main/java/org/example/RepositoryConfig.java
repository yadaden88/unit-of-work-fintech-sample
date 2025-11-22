package org.example;

import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static java.util.Map.copyOf;

@Configuration
public class RepositoryConfig {

    private static final Map<Class<?>, Repository<?>> repositories = new HashMap<>();

    public RepositoryConfig(
        AccountRepository accountRepository,
        TransferRepository transferRepository
    ) {
        repositories.put(Account.class, accountRepository);
        repositories.put(Transfer.class, transferRepository);
    }

    static Map<Class<?>, Repository<?>> repositories() {
        return copyOf(repositories);
    }
}

