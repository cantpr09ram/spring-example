package com.example.realworld.exception;

import org.springframework.http.HttpStatus;

/** 409 Conflict — e.g. duplicate email or username. */
public class ConflictException extends AppException {

    private final String field;

    public ConflictException(String field, String message) {
        super(HttpStatus.CONFLICT, field + " " + message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
