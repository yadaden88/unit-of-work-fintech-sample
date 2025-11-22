package org.example;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.example.RepositoryConfig.repositories;

public class Batch {

    private final List<Entity> toInsert = new ArrayList<>();
    private final List<Entity> toUpdate = new ArrayList<>();

    public <T extends Entity> void insert(T entity) {
        toInsert.add(entity);
    }

    public <T extends Entity> void update(T entity) {
        toUpdate.add(entity);
    }

    void executeInserts() {
        toInsert.stream()
            .forEach(entity -> getRepository(entity.getClass()).save(entity));
    }

    void executeUpdates() {
        // sorting ensures that updates are executed in a consistent order to prevent deadlocks
        toUpdate.stream()
            .sorted(Comparator.comparing(Entity::getId))
            .forEach(entity -> getRepository(entity.getClass()).update(entity));
    }

    @SuppressWarnings("unchecked")
    private <T> Repository<T> getRepository(Class<?> entityClass) {
        var repository = repositories().get(entityClass);
        if (repository == null) {
            throw new IllegalStateException(
                "No repository registered for entity type: " + entityClass.getName() + ". " +
                    "Make sure to register the repository in RepositoryConfig."
            );
        }
        return (Repository<T>) repository;
    }

}

