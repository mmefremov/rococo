package io.efremov.rococo.model;

import java.util.UUID;

public record PaintingInfoResponse(
    UUID id,
    String title,
    String description,
    String content,
    ArtistInfoResponse artist,
    MuseumInfoResponse museum
) {

}
