package io.efremov.rococo.model;

import java.util.UUID;

public record UpdateMuseumInfoRequest(
    UUID id,
    String title,
    String description,
    String photo,
    GeoInfoRequest geo
) {

}
