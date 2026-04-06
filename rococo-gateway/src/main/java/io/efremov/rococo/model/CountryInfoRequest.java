package io.efremov.rococo.model;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CountryInfoRequest(
    @NotNull
    UUID id
) {

}
