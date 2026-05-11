package io.efremov.rococo.common.validation;

import java.nio.charset.StandardCharsets;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class FieldValidator {

  private FieldValidator() {}

  public static void requireNotBlank(@Nullable String value, @NonNull String fieldName) {
    if (value == null || value.trim().isBlank()) {
      throw new IllegalArgumentException(fieldName + ": must not be blank");
    }
  }

  public static void requireLength(@Nullable String value, @NonNull String fieldName, int min, int max) {
    if (value != null && (value.length() < min || value.length() > max)) {
      throw new IllegalArgumentException(fieldName + ": must be from " + min + " to " + max + " characters");
    }
  }

  public static void requireMaxByteSize(@Nullable String value, @NonNull String fieldName, int maxBytes) {
    if (value != null && value.getBytes(StandardCharsets.UTF_8).length > maxBytes) {
      throw new IllegalArgumentException(fieldName + ": must not exceed 1 MB");
    }
  }

  public static void requireNotNull(@Nullable Object value, @NonNull String fieldName) {
    if (value == null) {
      throw new IllegalArgumentException(fieldName + ": must not be null");
    }
  }
}
