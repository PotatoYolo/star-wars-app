package com.starwars.backend.exception;

public class CharacterCreationException extends RuntimeException {
    public CharacterCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}