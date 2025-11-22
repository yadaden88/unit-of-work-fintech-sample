package org.example;

import java.util.UUID;

public record Transfer(UUID id, UUID fromAccountId, UUID toAccountId, long amount) implements Entity {

    @Override
    public UUID getId() {
        return id;
    }
}

