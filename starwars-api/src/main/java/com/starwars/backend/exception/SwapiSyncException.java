package com.starwars.backend.exception;

public class SwapiSyncException extends RuntimeException {
    public SwapiSyncException(String message, Throwable cause) {
        super(message, cause);
    }
}
