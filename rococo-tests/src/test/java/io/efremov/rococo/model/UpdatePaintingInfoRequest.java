package io.efremov.rococo.model;

import java.util.UUID;

public record UpdatePaintingInfoRequest(
    UUID id,
    String title,
    String description,
    String content,
    ArtistInfoRequest artist,
    MuseumInfoRequest museum
) {

}
