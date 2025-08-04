package com.starwars.backend.exception;

public class CharacterDeletionException extends RuntimeException {
    public CharacterDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}
