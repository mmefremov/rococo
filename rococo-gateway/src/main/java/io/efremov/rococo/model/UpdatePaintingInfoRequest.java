package io.efremov.rococo.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UpdatePaintingInfoRequest(
    @NotNull
    UUID id,
    @NotBlank @Size(min = 3, max = 255)
    String title,
    @NotBlank @Size(min = 10, max = 1000)
    String description,
    @NotBlank
    String content,
    @Valid @NotNull
    ArtistInfoRequest artist,
    @Valid @NotNull
    MuseumInfoRequest museum
) {

}
