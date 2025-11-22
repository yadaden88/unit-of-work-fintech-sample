package org.example;

import java.util.UUID;

public record Account(UUID id, long balance, String currency, long version) {

    public Account withBalance(long balance) {
        return new Account(this.id, balance, this.currency, this.version);
    }

}

