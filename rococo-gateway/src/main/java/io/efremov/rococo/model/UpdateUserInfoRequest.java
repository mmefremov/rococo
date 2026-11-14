package io.efremov.rococo.model;

import jakarta.validation.constraints.Size;

public record UpdateUserInfoRequest(
    @Size(max = 255)
    String firstname,
    @Size(max = 255)
    String lastname,
    String avatar
) {

}
