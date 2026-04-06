package io.efremov.rococo.model;

import java.util.List;

public record PageResponse<T>(
    List<T> content,
    int totalElements,
    int totalPages,
    int pageNumber,
    int pageSize,
    boolean last,
    boolean first,
    int numberOfElements
) {

}
