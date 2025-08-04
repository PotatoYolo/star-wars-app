package com.starwars.backend.exception;

public class SpeciesRetrievalException extends RuntimeException {
    public SpeciesRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}
