package org.example;

/**
 * Exception thrown when an optimistic lock conflict is detected.
 * This occurs when an entity's version has changed between read and update operations.
 */
public class OptimisticLockException extends RuntimeException {

    public OptimisticLockException(String message) {
        super(message);
    }

    public OptimisticLockException(String message, Throwable cause) {
        super(message, cause);
    }
}

