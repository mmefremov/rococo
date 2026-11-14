package io.efremov.rococo.common.validation;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class FieldValidator {

  private static final Set<String> IMAGE_PREFIXES = Set.of(
      "data:image/jpeg;base64,", "data:image/png;base64,", "data:image/webp;base64,");

  private FieldValidator() {}

  public static void requireImageBase64(@Nullable String value, @NonNull String fieldName) {
    requireNotBlank(value, fieldName);
    requireMaxByteSize(value, fieldName, 1_048_576);
    if (IMAGE_PREFIXES.stream().noneMatch(value::startsWith)) {
      throw new IllegalArgumentException(fieldName + ": must be a base64-encoded JPEG, PNG or WebP image");
    }
    byte[] decoded;
    try {
      decoded = Base64.getDecoder().decode(value.substring(value.indexOf(',') + 1));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(fieldName + ": invalid base64 encoding", e);
    }
    if (decoded.length == 0) {
      throw new IllegalArgumentException(fieldName + ": image payload must not be empty");
    }
  }

  public static void requireNotBlank(@Nullable String value, @NonNull String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + ": must not be blank");
    }
  }

  public static void requireLength(@NonNull String value, @NonNull String fieldName, int min, int max) {
    if (value.length() < min || value.length() > max) {
      throw new IllegalArgumentException(fieldName + ": must be from " + min + " to " + max + " characters");
    }
  }

  public static void requireMaxByteSize(@Nullable String value, @NonNull String fieldName, int maxBytes) {
    if (value != null && value.getBytes(StandardCharsets.UTF_8).length > maxBytes) {
      throw new IllegalArgumentException(fieldName + ": must not exceed " + (maxBytes / (1024 * 1024)) + " MB");
    }
  }

  public static void requireNotNull(@Nullable Object value, @NonNull String fieldName) {
    if (value == null) {
      throw new IllegalArgumentException(fieldName + ": must not be null");
    }
  }
}
