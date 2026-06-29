package com.example.realworld.exception;

import org.springframework.http.HttpStatus;

/** 401 Unauthorized — missing or invalid credentials. */
public class UnauthorizedException extends AppException {

    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
