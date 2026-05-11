package io.efremov.rococo.common.exception;

import java.util.UUID;
import org.jspecify.annotations.NonNull;

public class EntityNotFoundException extends RuntimeException {

  public EntityNotFoundException(@NonNull String entityType, @NonNull UUID id) {
    super(entityType + " not found: " + id);
  }

  public EntityNotFoundException(@NonNull String entityType, @NonNull String identifier) {
    super(entityType + " not found: " + identifier);
  }
}
