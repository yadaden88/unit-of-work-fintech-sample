package org.example;

import java.util.UUID;

public record Account(UUID id, long balance, long version) {

    public Account withBalance(long balance) {
        return new Account(this.id, balance, this.version);
    }

}

