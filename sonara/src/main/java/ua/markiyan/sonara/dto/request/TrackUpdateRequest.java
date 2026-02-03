package ua.markiyan.sonara.dto.request;

import jakarta.validation.constraints.*;

public record TrackUpdateRequest(
        @Size(min = 1, max = 500) String title,
        @NotNull @Min(1) @Max(9999) Integer durationSec,
        @NotNull Boolean explicitFlag,
        @NotBlank @Size(min = 2, max = 100) String audioKey,
        String audioUrl
) {}

