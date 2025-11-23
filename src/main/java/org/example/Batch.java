package org.example;

import java.util.ArrayList;
import java.util.List;

import static java.util.Comparator.comparing;

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
        toInsert.forEach(entity -> repositoryRegistry.getRepository(entity.getClass()).save(entity));
    }

    public void executeUpdates(RepositoryRegistry repositoryRegistry) {
        toUpdate.stream()
            .sorted(comparing(Entity::getId))
            .forEach(entity -> repositoryRegistry.getRepository(entity.getClass()).update(entity));
    }

}

