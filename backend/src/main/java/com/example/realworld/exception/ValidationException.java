package com.example.realworld.exception;

import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

/**
 * 422 Unprocessable Entity — business-rule / field-level validation failures.
 * Carries the RealWorld {@code {"errors":{field:[messages]}}} payload.
 */
public class ValidationException extends AppException {

    private final Map<String, List<String>> errors;

    public ValidationException(String field, String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, field + " " + message);
        this.errors = Map.of(field, List.of(message));
    }

    public ValidationException(Map<String, List<String>> errors) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, "Validation failed");
        this.errors = errors;
    }

    public Map<String, List<String>> getErrors() {
        return errors;
    }
}
