package io.efremov.rococo.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateArtistInfoRequest(
    @NotBlank @Size(min = 3, max = 255)
    String name,
    @NotBlank @Size(min = 10, max = 2000)
    String biography,
    @NotBlank
    String photo
) {

}
