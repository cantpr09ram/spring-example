package com.example.realworld.exception;

import org.springframework.http.HttpStatus;

/**
 * Base class for all application-level exceptions.
 * Carries the HTTP status that should be returned to the client.
 */
public abstract class AppException extends RuntimeException {

    private final HttpStatus status;

    protected AppException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
