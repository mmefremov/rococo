package io.efremov.rococo.model;

public record CreateArtistInfoRequest(
    String name,
    String biography,
    String photo
) {

}