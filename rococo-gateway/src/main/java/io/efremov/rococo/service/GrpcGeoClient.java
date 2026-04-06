package io.efremov.rococo.service;

import io.efremov.rococo.grpc.CountriesByIdRequest;
import io.efremov.rococo.grpc.CountriesResponse;
import io.efremov.rococo.grpc.CountryPageRequest;
import io.efremov.rococo.grpc.CountryPageResponse;
import io.efremov.rococo.grpc.GeoServiceGrpc;
import io.efremov.rococo.model.CountryInfoResponse;
import io.efremov.rococo.model.PageResponse;
import io.grpc.Status;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GrpcGeoClient {

  @GrpcClient("geo")
  private GeoServiceGrpc.GeoServiceBlockingStub geoStub;

  public PageResponse<CountryInfoResponse> getAllCountries(int page, int size) {
    log.debug("Calling geo service: getAllCountries page={}, size={}", page, size);
    CountryPageResponse response = geoStub.getAllCountries(
        CountryPageRequest.newBuilder().setPage(page).setSize(size).build()
    );
    List<CountryInfoResponse> content = response.getContentList().stream()
        .map(c -> new CountryInfoResponse(UUID.fromString(c.getId()), c.getName()))
        .toList();
    return new PageResponse<>(content, response.getTotalElements(), response.getTotalPages(),
        response.getPageNumber(), response.getPageSize(), response.getLast(),
        response.getFirst(), response.getNumberOfElements());
  }

  public @NonNull Map<UUID, CountryInfoResponse> getCountriesByIds(@NonNull Collection<UUID> ids) {
    if (ids.isEmpty()) {
      return Map.of();
    }
    log.debug("Calling geo service: getCountriesByIds count={}", ids.size());
    CountriesResponse response = geoStub.getCountriesByIds(
        CountriesByIdRequest.newBuilder()
            .addAllId(ids.stream().map(UUID::toString).toList())
            .build()
    );
    Map<UUID, CountryInfoResponse> result = response.getCountriesList().stream()
        .collect(Collectors.toUnmodifiableMap(
            c -> UUID.fromString(c.getId()),
            c -> new CountryInfoResponse(UUID.fromString(c.getId()), c.getName())
        ));
    Set<UUID> requested = Set.copyOf(ids);
    if (!result.keySet().containsAll(requested)) {
      UUID missing = requested.stream().filter(id -> !result.containsKey(id)).findFirst().orElseThrow();
      throw Status.INVALID_ARGUMENT.withDescription("Country not found: " + missing).asRuntimeException();
    }
    return result;
  }
}
