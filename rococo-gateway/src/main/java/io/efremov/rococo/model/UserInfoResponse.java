package io.efremov.rococo.model;

import java.util.UUID;

public record UserInfoResponse(
    UUID id,
    String username,
    String firstname,
    String lastname,
    String avatar
) {

}
