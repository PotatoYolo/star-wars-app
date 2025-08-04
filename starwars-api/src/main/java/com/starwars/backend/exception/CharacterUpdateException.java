package com.starwars.backend.exception;

public class CharacterUpdateException extends RuntimeException {
    public CharacterUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
