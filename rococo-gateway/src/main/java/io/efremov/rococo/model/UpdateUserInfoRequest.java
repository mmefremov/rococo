package io.efremov.rococo.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserInfoRequest(
    @NotBlank @Size(max = 255)
    String firstname,
    @NotBlank @Size(max = 255)
    String lastname,
    @NotBlank @Size(max = 255)
    String avatar
) {

}
