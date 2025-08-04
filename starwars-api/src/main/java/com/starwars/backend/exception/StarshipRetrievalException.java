package com.starwars.backend.exception;

public class StarshipRetrievalException extends RuntimeException {
    public StarshipRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}
