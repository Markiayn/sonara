package ua.markiyan.sonara.dto.response;


public record ArtistResponse (
        Long id,
        String name,
        String country,
        Integer startYear,
        String bio
){}
