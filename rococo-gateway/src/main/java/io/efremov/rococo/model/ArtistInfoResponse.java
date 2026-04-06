package io.efremov.rococo.model;

import java.util.UUID;

public record ArtistInfoResponse(
    UUID id,
    String name,
    String biography,
    String photo
) {

}
