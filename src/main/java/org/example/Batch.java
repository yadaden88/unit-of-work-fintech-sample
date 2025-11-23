package org.example;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Batch {

    private final RepositoryRegistry repositoryRegistry;
    private final List<Entity> toInsert = new ArrayList<>();
    private final List<Entity> toUpdate = new ArrayList<>();

    public Batch(RepositoryRegistry repositoryRegistry) {
        this.repositoryRegistry = repositoryRegistry;
    }

    public <T extends Entity> void insert(T entity) {
        toInsert.add(entity);
    }

    public <T extends Entity> void update(T entity) {
        toUpdate.add(entity);
    }

    void executeInserts() {
        toInsert.stream()
            .forEach(entity -> repositoryRegistry.getRepository(entity.getClass()).save(entity));
    }

    void executeUpdates() {
        toUpdate.stream()
            .sorted(Comparator.comparing(Entity::getId))
            .forEach(entity -> repositoryRegistry.getRepository(entity.getClass()).update(entity));
    }

}

