package io.efremov.rococo.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GeoInfoRequest(
    @NotBlank @Size(min = 3, max = 255)
    String city,
    @Valid @NotNull
    CountryInfoRequest country
) {

}
