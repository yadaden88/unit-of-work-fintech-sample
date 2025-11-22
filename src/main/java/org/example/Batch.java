package org.example;

import java.util.ArrayList;
import java.util.List;

public class Batch {

    private final List<Object> insertOperations = new ArrayList<>();
    private final List<Object> updateOperations = new ArrayList<>();

    public <T> void insert(T entity) {
        insertOperations.add(entity);
    }

    public <T> void update(T entity) {
        updateOperations.add(entity);
    }

    void executeInserts() {
        for (Object entity : insertOperations) {
            Repository<Object> repository = getRepository(entity.getClass());
            repository.save(entity);
        }
    }

    void executeUpdates() {
        for (Object entity : updateOperations) {
            Repository<Object> repository = getRepository(entity.getClass());
            repository.update(entity);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Repository<T> getRepository(Class<?> entityClass) {
        Repository<?> repository = RepositoryConfig.getAll().get(entityClass);
        if (repository == null) {
            throw new IllegalStateException(
                "No repository registered for entity type: " + entityClass.getName() + ". " +
                    "Make sure to register the repository in RepositoryConfig."
            );
        }
        return (Repository<T>) repository;
    }

}

