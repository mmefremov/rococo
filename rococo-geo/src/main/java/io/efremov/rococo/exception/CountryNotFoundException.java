package io.efremov.rococo.exception;

import java.util.UUID;

public class CountryNotFoundException extends RuntimeException {

  public CountryNotFoundException(UUID id) {
    super("Country not found: " + id);
  }
}
