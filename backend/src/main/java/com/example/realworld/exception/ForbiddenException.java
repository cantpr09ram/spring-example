package com.example.realworld.exception;

import org.springframework.http.HttpStatus;

/** 403 Forbidden. */
public class ForbiddenException extends AppException {

    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
