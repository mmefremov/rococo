package io.efremov.rococo.common.grpc;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;

public record PageMetadata(
    int totalElements,
    int totalPages,
    int pageNumber,
    int pageSize,
    boolean last,
    boolean first,
    int numberOfElements
) {
  public static @NonNull PageMetadata from(@NonNull Page<?> page) {
    return new PageMetadata(
        (int) page.getTotalElements(),
        page.getTotalPages(),
        page.getNumber(),
        page.getSize(),
        page.isLast(),
        page.isFirst(),
        page.getNumberOfElements()
    );
  }
}
