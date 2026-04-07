package io.efremov.rococo.model;

import java.util.UUID;

public record MuseumInfoResponse(
    UUID id,
    String title,
    String description,
    String photo,
    GeoInfoResponse geo
) {

}
