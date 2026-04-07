package io.efremov.rococo.model;

public record GeoInfoRequest(
    String city,
    CountryInfoRequest country
) {

}
