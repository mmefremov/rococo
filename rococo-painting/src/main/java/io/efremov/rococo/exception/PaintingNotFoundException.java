package io.efremov.rococo.exception;

import java.util.UUID;

public class PaintingNotFoundException extends RuntimeException {

  public PaintingNotFoundException(UUID id) {
    super("Painting not found: " + id);
  }
}
