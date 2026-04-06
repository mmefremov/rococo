package io.efremov.rococo.service;

import io.efremov.rococo.grpc.ArtistByIdRequest;
import io.efremov.rococo.grpc.ArtistPageRequest;
import io.efremov.rococo.grpc.ArtistPageResponse;
import io.efremov.rococo.grpc.ArtistResponse;
import io.efremov.rococo.grpc.ArtistServiceGrpc;
import io.efremov.rococo.grpc.ArtistsResponse;
import io.efremov.rococo.model.ArtistInfoResponse;
import io.efremov.rococo.model.CreateArtistInfoRequest;
import io.efremov.rococo.model.PageResponse;
import io.efremov.rococo.model.UpdateArtistInfoRequest;
import io.grpc.Status;
import jakarta.validation.Valid;
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
public class GrpcArtistClient {

  @GrpcClient("artist")
  private ArtistServiceGrpc.ArtistServiceBlockingStub artistStub;

  public PageResponse<ArtistInfoResponse> getAllArtists(int page, int size, String name) {
    log.debug("Calling artist service: getAllArtists page={}, size={}, name={}", page, size, name);
    ArtistPageRequest.Builder builder = ArtistPageRequest.newBuilder().setPage(page).setSize(size);
    if (name != null && !name.isBlank()) {
      builder.setName(name);
    }
    ArtistPageResponse response = artistStub.getAllArtists(builder.build());
    List<ArtistInfoResponse> content = response.getContentList().stream()
        .map(this::toModel)
        .toList();
    return new PageResponse<>(content, response.getTotalElements(), response.getTotalPages(),
        response.getPageNumber(), response.getPageSize(), response.getLast(),
        response.getFirst(), response.getNumberOfElements());
  }

  public ArtistInfoResponse getArtistById(UUID id) {
    log.debug("Calling artist service: getArtistById id={}", id);
    ArtistsResponse response = artistStub.getArtistById(
        ArtistByIdRequest.newBuilder().addId(id.toString()).build()
    );
    if (response.getArtistsList().isEmpty()) {
      throw Status.NOT_FOUND.withDescription("Artist not found: " + id).asRuntimeException();
    }
    return toModel(response.getArtists(0));
  }

  public @NonNull Map<UUID, ArtistInfoResponse> getArtistsByIds(@NonNull Collection<UUID> ids) {
    if (ids.isEmpty()) {
      return Map.of();
    }
    log.debug("Calling artist service: getArtistsByIds count={}", ids.size());
    ArtistsResponse response = artistStub.getArtistById(
        ArtistByIdRequest.newBuilder()
            .addAllId(ids.stream().map(UUID::toString).toList())
            .build()
    );
    Map<UUID, ArtistInfoResponse> result = response.getArtistsList().stream()
        .collect(Collectors.toUnmodifiableMap(
            a -> UUID.fromString(a.getId()),
            this::toModel
        ));
    Set<UUID> requested = Set.copyOf(ids);
    if (!result.keySet().containsAll(requested)) {
      UUID missing = requested.stream().filter(id -> !result.containsKey(id)).findFirst().orElseThrow();
      throw Status.INVALID_ARGUMENT.withDescription("Artist not found: " + missing).asRuntimeException();
    }
    return result;
  }

  public ArtistInfoResponse createArtist(@Valid CreateArtistInfoRequest request) {
    log.debug("Calling artist service: createArtist name={}", request.name());
    return toModel(artistStub.createArtist(
        io.efremov.rococo.grpc.CreateArtistRequest.newBuilder()
            .setName(request.name())
            .setBiography(request.biography())
            .setPhoto(request.photo())
            .build()
    ));
  }

  public ArtistInfoResponse updateArtist(@Valid UpdateArtistInfoRequest request) {
    log.debug("Calling artist service: updateArtist id={}", request.id());
    return toModel(artistStub.updateArtist(
        io.efremov.rococo.grpc.UpdateArtistRequest.newBuilder()
            .setId(request.id().toString())
            .setName(request.name())
            .setBiography(request.biography())
            .setPhoto(request.photo())
            .build()
    ));
  }

  private ArtistInfoResponse toModel(ArtistResponse r) {
    return new ArtistInfoResponse(UUID.fromString(r.getId()), r.getName(), r.getBiography(), r.getPhoto());
  }
}
