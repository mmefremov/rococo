package io.efremov.rococo.model;

public record CreateMuseumInfoRequest(
    String title,
    String description,
    String photo,
    GeoInfoRequest geo
) {

}
