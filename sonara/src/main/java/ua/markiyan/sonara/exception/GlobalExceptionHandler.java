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

    // 1. Помилки валідації (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.merge(error.getField(), error.getDefaultMessage(), (o, n) -> o + ", " + n)
        );
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }


// 2. Конфлікти (дублікати в БД)
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleResourceExists(ResourceAlreadyExistsException ex) {
        // Використовуємо твій допоміжний метод для консистентності
        Map<String, String> errors = Map.of(ex.getField(), ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "Resource conflict", errors);
    }

    // 3. Ресурс не знайдено (NotFound)
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    // 4. Помилки логіки (IllegalState або невірні типи в Enum)
    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    // Допоміжний метод, щоб не дублювати код створення ResponseEntity
    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, Map<String, String> errors) {
        ErrorResponse response = new ErrorResponse(
                status.value(),
                message,
                LocalDateTime.now(),
                errors
        );
        return new ResponseEntity<>(response, status);
    }
}