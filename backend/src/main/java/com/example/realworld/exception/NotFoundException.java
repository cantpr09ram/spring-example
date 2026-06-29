package com.example.realworld.exception;

import org.springframework.http.HttpStatus;

/** 404 Not Found. */
public class NotFoundException extends AppException {

    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
