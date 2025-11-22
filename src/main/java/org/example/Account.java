package org.example;

import java.util.UUID;

public record Account(UUID id, long balance, long version) implements Entity {

    @Override
    public UUID getId() {
        return id;
    }

    public Account withBalance(long balance) {
        return new Account(this.id, balance, this.version);
    }

}

