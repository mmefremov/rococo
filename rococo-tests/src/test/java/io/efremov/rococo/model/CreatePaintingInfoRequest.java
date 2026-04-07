package io.efremov.rococo.model;

public record CreatePaintingInfoRequest(
    String title,
    String description,
    String content,
    ArtistInfoRequest artist,
    MuseumInfoRequest museum
) {

}
