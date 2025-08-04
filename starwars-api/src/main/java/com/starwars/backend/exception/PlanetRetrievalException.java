package com.starwars.backend.exception;

public class PlanetRetrievalException extends RuntimeException {
    public PlanetRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}