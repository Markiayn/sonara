package ua.markiyan.sonara.dto.request;

import jakarta.validation.constraints.*;


public record TrackRequest (
        @NotBlank String title,
        @NotNull @Min(1) @Max(9999) Integer durationSec ,
        @NotBlank @Size(min = 2, max = 100) String audioKey,
        @NotNull Boolean explicitFlag,
        @NotNull Long albumId,
        @NotNull Long artistId
){}
