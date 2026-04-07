package io.efremov.rococo.model;

public record GeoInfoResponse(
    String city,
    CountryInfoResponse country
) {

}