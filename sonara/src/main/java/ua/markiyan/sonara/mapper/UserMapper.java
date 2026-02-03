package ua.markiyan.sonara.mapper;

import ua.markiyan.sonara.dto.request.UserRequest;
import ua.markiyan.sonara.dto.response.UserResponse;
import ua.markiyan.sonara.entity.User;

public final class UserMapper {
    private UserMapper() {}

    public static User toEntity(UserRequest dto) {
        return User.builder()
                .email(dto.email())
                .name(dto.name())
                .country(dto.country())
                .status(User.Status.ACTIVE) // дефолт
                .build();
    }

    public static UserResponse toResponse(User e) {
        return new UserResponse(
                e.getId(),
                e.getEmail(),
                e.getName(),
                e.getCountry(),
                e.getStatus().name(),
                e.getCreatedAt().toString(),
                e.getRole().name()
        );
    }
}

