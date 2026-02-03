package ua.markiyan.sonara.dto.response;

public record UserResponse(
        Long id,
        String email,
        String name,
        String country,
        String status,
        String createdAt,
        String role
) {}
