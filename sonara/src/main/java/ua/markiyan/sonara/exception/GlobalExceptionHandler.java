package ua.markiyan.sonara.exception;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import ua.markiyan.sonara.dto.response.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.merge(
                    error.getField(),
                    error.getDefaultMessage(),
                    (oldValue, newValue) -> oldValue + ", " + newValue // Склеюємо через кому
            );
        });

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                LocalDateTime.now(),
                errors
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleResourceExists(ResourceAlreadyExistsException ex) {
        Map<String, String> errors = Map.of(ex.getField(), ex.getMessage());

        ErrorResponse response = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Resource conflict",
                LocalDateTime.now(),
                errors
        );

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }
}
