package com.example.realworld.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Central error handler. All responses use the RealWorld error envelope:
 * <pre>{"errors": {"&lt;field&gt;": ["&lt;message&gt;", ...]}}</pre>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ErrorResponse(Map<String, List<String>> errors) {
    }

    // ── Bean Validation (@Valid) ─────────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {
        Map<String, List<String>> errors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            String field = stripNestedPrefix(fieldError.getField());
            errors.computeIfAbsent(field, k -> new ArrayList<>())
                    .add(fieldError.getDefaultMessage());
        }
        if (errors.isEmpty()) {
            errors.put("body", List.of("is invalid"));
        }
        return ResponseEntity.unprocessableEntity().body(new ErrorResponse(errors));
    }

    // ── Application exceptions (AppException hierarchy) ──────────────────────

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        return ResponseEntity.unprocessableEntity().body(new ErrorResponse(ex.getErrors()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        return body(HttpStatus.NOT_FOUND, "body", ex.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex) {
        return body(HttpStatus.FORBIDDEN, "body", ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
        return body(HttpStatus.UNAUTHORIZED, "body", ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        return body(HttpStatus.CONFLICT, ex.getField(), ex.getMessage());
    }

    /** Catch-all for any other AppException subclass not listed above. */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleApp(AppException ex) {
        return body(ex.getStatus(), "body", ex.getMessage());
    }

    // ── Spring MVC / HTTP ─────────────────────────────────────────────────────

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        return body(HttpStatus.BAD_REQUEST, "body", "request body is missing or malformed");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex) {
        return body(HttpStatus.METHOD_NOT_ALLOWED, "method",
                ex.getMethod() + " is not supported on this endpoint");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(
            MissingServletRequestParameterException ex) {
        return body(HttpStatus.BAD_REQUEST, ex.getParameterName(), "is required");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        return body(HttpStatus.BAD_REQUEST, ex.getName(),
                "must be of type " + ex.getRequiredType().getSimpleName());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(
            NoResourceFoundException ex, HttpServletRequest request) {
        return body(HttpStatus.NOT_FOUND, "path", request.getRequestURI() + " not found");
    }

    // ── Fallback ─────────────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex, HttpServletRequest request) {
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "body", "an unexpected error occurred");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private ResponseEntity<ErrorResponse> body(HttpStatus status, String field, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(Map.of(field, List.of(message))));
    }

    /**
     * Strips the wrapper-record prefix from nested @Valid field paths.
     * e.g. {@code "user.email"} → {@code "email"}.
     */
    private String stripNestedPrefix(String field) {
        int dot = field.lastIndexOf('.');
        return dot >= 0 ? field.substring(dot + 1) : field;
    }
}
