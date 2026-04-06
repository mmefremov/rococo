package io.efremov.rococo.exception;

import java.util.UUID;

public class MuseumNotFoundException extends RuntimeException {

  public MuseumNotFoundException(UUID id) {
    super("Museum not found: " + id);
  }
}
