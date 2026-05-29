package com.rentalapp.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.Builder;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException exception) {
        return ResponseEntity.status(exception.getStatus())
                .body(ErrorResponse.of(exception.getCode(), exception.getMessage(), null));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of("FORBIDDEN", "You do not have permission to perform this action.", null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        List<ValidationError> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toValidationError)
                .toList();

        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("VALIDATION_ERROR", "Validation failed.", errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
        List<ValidationError> errors = exception.getConstraintViolations()
                .stream()
                .map(violation -> ValidationError.builder()
                        .field(violation.getPropertyPath().toString())
                        .message(violation.getMessage())
                        .build())
                .toList();

        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("VALIDATION_ERROR", "Validation failed.", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception exception) {
        log.error("Unhandled exception", exception);
        return ResponseEntity.internalServerError()
                .body(ErrorResponse.of("INTERNAL_SERVER_ERROR", "An unexpected error occurred.", null));
    }

    private ValidationError toValidationError(FieldError fieldError) {
        return ValidationError.builder()
                .field(fieldError.getField())
                .message(fieldError.getDefaultMessage())
                .build();
    }

    @Getter
    @Builder
    public static class ValidationError {
        private final String field;
        private final String message;
    }

    @Getter
    @Builder
    public static class ErrorResponse {
        private final boolean success;
        private final String code;
        private final String message;
        private final List<ValidationError> errors;
        private final Instant timestamp;

        public static ErrorResponse of(String code, String message, List<ValidationError> errors) {
            return ErrorResponse.builder()
                    .success(false)
                    .code(code)
                    .message(message)
                    .errors(errors)
                    .timestamp(Instant.now())
                    .build();
        }
    }
}
