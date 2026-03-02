package com.example.demo.sum.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler({
      MissingServletRequestParameterException.class,
      MethodArgumentTypeMismatchException.class,
      BindException.class,
      MethodArgumentNotValidException.class
  })
  public ResponseEntity<ApiError> handleBadRequest(Exception ex, HttpServletRequest request) {
    if (ex instanceof MissingServletRequestParameterException missing) {
      return buildBadRequest(missing.getParameterName() + " is required", request.getRequestURI());
    }

    if (ex instanceof MethodArgumentTypeMismatchException mismatch) {
      String parameterName = mismatch.getName();
      return buildBadRequest(parameterName + " must be a valid number", request.getRequestURI());
    }

    return buildBadRequest("invalid request parameters", request.getRequestURI());
  }

  @ExceptionHandler(ArithmeticException.class)
  public ResponseEntity<ApiError> handleArithmetic(ArithmeticException ex, HttpServletRequest request) {
    return buildBadRequest("number overflow", request.getRequestURI());
  }

  private ResponseEntity<ApiError> buildBadRequest(String message, String path) {
    ApiError error = new ApiError(
        Instant.now(),
        HttpStatus.BAD_REQUEST.value(),
        "BAD_REQUEST",
        message,
        path
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }
}
