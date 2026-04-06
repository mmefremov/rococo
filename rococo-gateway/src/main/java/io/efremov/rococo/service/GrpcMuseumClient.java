package io.efremov.rococo.service;

import io.efremov.rococo.grpc.CreateMuseumRequest;
import io.efremov.rococo.grpc.MuseumByIdRequest;
import io.efremov.rococo.grpc.MuseumPageRequest;
import io.efremov.rococo.grpc.MuseumPageResponse;
import io.efremov.rococo.grpc.MuseumResponse;
import io.efremov.rococo.grpc.MuseumServiceGrpc;
import io.efremov.rococo.grpc.MuseumsByIdRequest;
import io.efremov.rococo.grpc.MuseumsResponse;
import io.efremov.rococo.grpc.UpdateMuseumRequest;
import io.efremov.rococo.model.CountryInfoResponse;
import io.efremov.rococo.model.CreateMuseumInfoRequest;
import io.efremov.rococo.model.GeoInfoResponse;
import io.efremov.rococo.model.MuseumInfoResponse;
import io.efremov.rococo.model.PageResponse;
import io.efremov.rococo.model.UpdateMuseumInfoRequest;
import io.grpc.Status;
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrpcMuseumClient {

  @GrpcClient("museum")
  private MuseumServiceGrpc.MuseumServiceBlockingStub museumStub;

  private final GrpcGeoClient geoClient;

  public PageResponse<MuseumInfoResponse> getAllMuseums(int page, int size, String title) {
    log.debug("Calling museum service: getAllMuseums page={}, size={}, title={}", page, size, title);
    var builder = MuseumPageRequest.newBuilder().setPage(page).setSize(size);
    if (title != null && !title.isBlank()) {
      builder.setTitle(title);
    }
    MuseumPageResponse response = museumStub.getAllMuseums(builder.build());
    List<MuseumInfoResponse> content = enrichMuseums(response.getContentList());
    return new PageResponse<>(content, response.getTotalElements(), response.getTotalPages(),
        response.getPageNumber(), response.getPageSize(), response.getLast(),
        response.getFirst(), response.getNumberOfElements());
  }

  public @NonNull Map<UUID, MuseumInfoResponse> getMuseumsByIds(@NonNull Collection<UUID> ids) {
    if (ids.isEmpty()) {
      return Map.of();
    }
    log.debug("Calling museum service: getMuseumsByIds count={}", ids.size());
    MuseumsResponse response = museumStub.getMuseumsByIds(
        MuseumsByIdRequest.newBuilder()
            .addAllId(ids.stream().map(UUID::toString).toList())
            .build()
    );
    Set<UUID> requested = Set.copyOf(ids);
    Set<UUID> returned = response.getMuseumsList().stream()
        .map(m -> UUID.fromString(m.getId()))
        .collect(Collectors.toUnmodifiableSet());
    if (!returned.containsAll(requested)) {
      UUID missing = requested.stream().filter(id -> !returned.contains(id)).findFirst().orElseThrow();
      throw Status.INVALID_ARGUMENT.withDescription("Museum not found: " + missing).asRuntimeException();
    }
    List<MuseumInfoResponse> enriched = enrichMuseums(response.getMuseumsList());
    return enriched.stream()
        .collect(Collectors.toUnmodifiableMap(MuseumInfoResponse::id, m -> m));
  }

  public MuseumInfoResponse getMuseumById(UUID id) {
    log.debug("Calling museum service: getMuseumById id={}", id);
    MuseumResponse response = museumStub.getMuseumById(
        MuseumByIdRequest.newBuilder().setId(id.toString()).build()
    );
    Set<UUID> countryIds = Set.of(UUID.fromString(response.getCountryId()));
    Map<UUID, CountryInfoResponse> countries = geoClient.getCountriesByIds(countryIds);
    return toInfo(response, countries);
  }

  public MuseumInfoResponse createMuseum(@Valid CreateMuseumInfoRequest request) {
    log.debug("Calling museum service: createMuseum title={}", request.title());
    UUID countryId = request.geo().country().id();
    Map<UUID, CountryInfoResponse> countries = geoClient.getCountriesByIds(Set.of(countryId));
    MuseumResponse response = museumStub.createMuseum(
        CreateMuseumRequest.newBuilder()
            .setTitle(request.title())
            .setDescription(request.description())
            .setPhoto(request.photo())
            .setCity(request.geo().city())
            .setCountryId(countryId.toString())
            .build()
    );
    return toInfo(response, countries);
  }

  public MuseumInfoResponse updateMuseum(@Valid UpdateMuseumInfoRequest request) {
    log.debug("Calling museum service: updateMuseum id={}", request.id());
    UUID countryId = request.geo().country().id();
    Map<UUID, CountryInfoResponse> countries = geoClient.getCountriesByIds(Set.of(countryId));
    MuseumResponse response = museumStub.updateMuseum(
        UpdateMuseumRequest.newBuilder()
            .setId(request.id().toString())
            .setTitle(request.title())
            .setDescription(request.description())
            .setPhoto(request.photo())
            .setCity(request.geo().city())
            .setCountryId(countryId.toString())
            .build()
    );
    return toInfo(response, countries);
  }

  private List<MuseumInfoResponse> enrichMuseums(List<MuseumResponse> museums) {
    if (museums.isEmpty()) {
      return List.of();
    }
    Set<UUID> countryIds = museums.stream()
        .map(m -> UUID.fromString(m.getCountryId()))
        .collect(Collectors.toUnmodifiableSet());
    Map<UUID, CountryInfoResponse> countries = geoClient.getCountriesByIds(countryIds);
    return museums.stream()
        .map(m -> toInfo(m, countries))
        .toList();
  }

  private static MuseumInfoResponse toInfo(MuseumResponse grpc, Map<UUID, CountryInfoResponse> countries) {
    UUID countryId = UUID.fromString(grpc.getCountryId());
    CountryInfoResponse country = countries.get(countryId);
    GeoInfoResponse geo = new GeoInfoResponse(grpc.getCity(), country);
    return new MuseumInfoResponse(UUID.fromString(grpc.getId()), grpc.getTitle(),
        grpc.getDescription(), grpc.getPhoto(), geo);
  }
}
