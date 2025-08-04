package com.starwars.backend.exception;

public class FilmRetrievalException extends RuntimeException {
    public FilmRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}
