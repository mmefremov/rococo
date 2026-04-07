package io.efremov.rococo.model;

import java.time.OffsetDateTime;

public record SessionResponse(
    String username,
    OffsetDateTime issuedAt,
    OffsetDateTime expiresAt
) {

}
