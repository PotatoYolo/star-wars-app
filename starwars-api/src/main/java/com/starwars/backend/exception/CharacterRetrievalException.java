package com.starwars.backend.exception;

public class CharacterRetrievalException extends RuntimeException {
    public CharacterRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}
