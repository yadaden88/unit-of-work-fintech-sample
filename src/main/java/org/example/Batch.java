package org.example;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Batch {

    private final List<Entity> toInsert = new ArrayList<>();
    private final List<Entity> toUpdate = new ArrayList<>();

    public <T extends Entity> void insert(T entity) {
        toInsert.add(entity);
    }

    public <T extends Entity> void update(T entity) {
        toUpdate.add(entity);
    }

    public void executeInserts(RepositoryRegistry repositoryRegistry) {
        toInsert.stream()
            .forEach(entity -> repositoryRegistry.getRepository(entity.getClass()).save(entity));
    }

    public void executeUpdates(RepositoryRegistry repositoryRegistry) {
        toUpdate.stream()
            .sorted(Comparator.comparing(Entity::getId))
            .forEach(entity -> repositoryRegistry.getRepository(entity.getClass()).update(entity));
    }

}

