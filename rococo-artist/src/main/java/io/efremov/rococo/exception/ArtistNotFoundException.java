package io.efremov.rococo.exception;

import java.util.UUID;

public class ArtistNotFoundException extends RuntimeException {

  public ArtistNotFoundException(UUID id) {
    super("Artist not found: " + id);
  }
}
