package io.efremov.rococo.model;

import java.time.Instant;

public record SessionResponse(
    String username,
    Instant issuedAt,
    Instant expiresAt
) {

}
