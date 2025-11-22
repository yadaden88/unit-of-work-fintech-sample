package org.example;

import java.util.UUID;

public record Account(UUID id, long balance, String currency) {
}

