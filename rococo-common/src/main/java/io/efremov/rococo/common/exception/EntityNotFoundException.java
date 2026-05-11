package io.efremov.rococo.common.exception;

import java.io.Serial;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

public class EntityNotFoundException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;

  public EntityNotFoundException(@NonNull String entityType, @NonNull UUID id) {
    super(entityType + " not found: " + id);
  }

  public EntityNotFoundException(@NonNull String entityType, @NonNull String identifier) {
    super(entityType + " not found: " + identifier);
  }
}
