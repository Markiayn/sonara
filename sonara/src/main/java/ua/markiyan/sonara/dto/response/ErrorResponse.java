package ua.markiyan.sonara.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse (
        int status,
        String message,
        @com.fasterxml.jackson.annotation.JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
        LocalDateTime timestamp,
        Map<String, String> validationErrors // Optional, for form errors
) {}
