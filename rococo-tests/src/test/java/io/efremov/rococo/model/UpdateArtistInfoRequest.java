package io.efremov.rococo.model;

import java.util.UUID;

public record UpdateArtistInfoRequest(
    UUID id,
    String name,
    String biography,
    String photo
) {

}
