package com.starwars.backend.exception;

public class VehicleRetrievalException extends RuntimeException {
    public VehicleRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}
