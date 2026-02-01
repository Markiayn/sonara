package ua.markiyan.sonara.mapper;

import ua.markiyan.sonara.dto.request.PlaylistRequest;
import ua.markiyan.sonara.dto.response.PlaylistResponse;
import ua.markiyan.sonara.entity.Playlist;
import ua.markiyan.sonara.entity.User;

public final class PlaylistMapper {
    private PlaylistMapper() {}

    public static Playlist toEntity(PlaylistRequest req, User u) {
        return Playlist.builder()
                .title(req.title())
                .isPublic(Boolean.TRUE.equals(req.isPublic()))
                .user(u)
                .build();
    }

    public static PlaylistResponse toResponse(Playlist p) {
        return new PlaylistResponse(
                p.getId(),
                p.getUser() != null ? p.getUser().getId() : null,
                p.getTitle(),
                p.isPublic()
        );
    }
}
