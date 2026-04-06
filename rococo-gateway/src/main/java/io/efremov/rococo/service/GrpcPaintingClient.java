package io.efremov.rococo.service;

import io.efremov.rococo.grpc.CreatePaintingRequest;
import io.efremov.rococo.grpc.PaintingByArtistRequest;
import io.efremov.rococo.grpc.PaintingByIdRequest;
import io.efremov.rococo.grpc.PaintingPageRequest;
import io.efremov.rococo.grpc.PaintingPageResponse;
import io.efremov.rococo.grpc.PaintingResponse;
import io.efremov.rococo.grpc.PaintingServiceGrpc;
import io.efremov.rococo.grpc.UpdatePaintingRequest;
import io.efremov.rococo.model.ArtistInfoResponse;
import io.efremov.rococo.model.CreatePaintingInfoRequest;
import io.efremov.rococo.model.MuseumInfoResponse;
import io.efremov.rococo.model.PageResponse;
import io.efremov.rococo.model.PaintingInfoResponse;
import io.efremov.rococo.model.UpdatePaintingInfoRequest;
import io.grpc.StatusRuntimeException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrpcPaintingClient {

  @GrpcClient("painting")
  private PaintingServiceGrpc.PaintingServiceBlockingStub paintingStub;

  private final GrpcArtistClient artistClient;
  private final GrpcMuseumClient museumClient;
  @Qualifier("enrichmentExecutor")
  private final Executor enrichmentExecutor;

  public PageResponse<PaintingInfoResponse> getAllPaintings(int page, int size, String title) {
    log.debug("Calling painting service: getAllPaintings page={}, size={}, title={}", page, size, title);
    PaintingPageRequest.Builder builder = PaintingPageRequest.newBuilder().setPage(page).setSize(size);
    if (title != null && !title.isBlank()) {
      builder.setTitle(title);
    }
    PaintingPageResponse response = paintingStub.getAllPaintings(builder.build());
    return enrichPage(response);
  }

  public PageResponse<PaintingInfoResponse> getPaintingsByArtist(UUID artistId, int page, int size) {
    log.debug("Calling painting service: getPaintingsByArtist artistId={}", artistId);
    PaintingPageResponse response = paintingStub.getPaintingsByArtist(
        PaintingByArtistRequest.newBuilder()
            .setArtistId(artistId.toString())
            .setPage(page)
            .setSize(size)
            .build()
    );
    return enrichPage(response);
  }

  public PaintingInfoResponse getPaintingById(UUID id) {
    log.debug("Calling painting service: getPaintingById id={}", id);
    PaintingResponse response = paintingStub.getPaintingById(
        PaintingByIdRequest.newBuilder().setId(id.toString()).build()
    );
    return enrichOne(response);
  }

  public PaintingInfoResponse createPainting(@Valid CreatePaintingInfoRequest request) {
    log.debug("Calling painting service: createPainting title={}", request.title());
    UUID artistId = request.artist().id();
    UUID museumId = request.museum() != null ? request.museum().id() : null;

    validateReferences(artistId, museumId);

    var builder = CreatePaintingRequest.newBuilder()
        .setTitle(request.title())
        .setDescription(request.description())
        .setContent(request.content())
        .setArtistId(artistId.toString());
    if (museumId != null) {
      builder.setMuseumId(museumId.toString());
    }
    PaintingResponse response = paintingStub.createPainting(builder.build());
    return enrichOne(response);
  }

  public PaintingInfoResponse updatePainting(@Valid UpdatePaintingInfoRequest request) {
    log.debug("Calling painting service: updatePainting id={}", request.id());
    UUID artistId = request.artist().id();
    UUID museumId = request.museum() != null ? request.museum().id() : null;

    validateReferences(artistId, museumId);

    var builder = UpdatePaintingRequest.newBuilder()
        .setId(request.id().toString())
        .setTitle(request.title())
        .setDescription(request.description())
        .setContent(request.content())
        .setArtistId(artistId.toString());
    if (museumId != null) {
      builder.setMuseumId(museumId.toString());
    }
    PaintingResponse response = paintingStub.updatePainting(builder.build());
    return enrichOne(response);
  }

  private void validateReferences(UUID artistId, @Nullable UUID museumId) {
    CompletableFuture<Void> artistCheck = CompletableFuture.runAsync(
        () -> artistClient.getArtistsByIds(Set.of(artistId)), enrichmentExecutor);
    CompletableFuture<Void> museumCheck = museumId == null
        ? CompletableFuture.completedFuture(null)
        : CompletableFuture.runAsync(() -> museumClient.getMuseumsByIds(Set.of(museumId)), enrichmentExecutor);
    joinAll(artistCheck, museumCheck);
  }

  private PageResponse<PaintingInfoResponse> enrichPage(PaintingPageResponse response) {
    List<PaintingResponse> paintings = response.getContentList();
    if (paintings.isEmpty()) {
      return new PageResponse<>(List.of(), response.getTotalElements(), response.getTotalPages(),
          response.getPageNumber(), response.getPageSize(), response.getLast(),
          response.getFirst(), response.getNumberOfElements());
    }

    Set<UUID> artistIds = paintings.stream()
        .map(p -> UUID.fromString(p.getArtistId()))
        .collect(Collectors.toUnmodifiableSet());
    Set<UUID> museumIds = paintings.stream()
        .filter(PaintingResponse::hasMuseumId)
        .map(p -> UUID.fromString(p.getMuseumId()))
        .collect(Collectors.toUnmodifiableSet());

    CompletableFuture<Map<UUID, ArtistInfoResponse>> artistsFut =
        CompletableFuture.supplyAsync(() -> artistClient.getArtistsByIds(artistIds), enrichmentExecutor);
    CompletableFuture<Map<UUID, MuseumInfoResponse>> museumsFut =
        CompletableFuture.supplyAsync(() -> museumClient.getMuseumsByIds(museumIds), enrichmentExecutor);

    joinAll(artistsFut, museumsFut);

    Map<UUID, ArtistInfoResponse> artists = artistsFut.join();
    Map<UUID, MuseumInfoResponse> museums = museumsFut.join();

    List<PaintingInfoResponse> content = paintings.stream()
        .map(p -> toInfo(p, artists, museums))
        .toList();
    return new PageResponse<>(content, response.getTotalElements(), response.getTotalPages(),
        response.getPageNumber(), response.getPageSize(), response.getLast(),
        response.getFirst(), response.getNumberOfElements());
  }

  private PaintingInfoResponse enrichOne(PaintingResponse p) {
    UUID artistId = UUID.fromString(p.getArtistId());
    Set<UUID> museumIds = p.hasMuseumId() ? Set.of(UUID.fromString(p.getMuseumId())) : Set.of();

    CompletableFuture<Map<UUID, ArtistInfoResponse>> artistsFut =
        CompletableFuture.supplyAsync(() -> artistClient.getArtistsByIds(Set.of(artistId)), enrichmentExecutor);
    CompletableFuture<Map<UUID, MuseumInfoResponse>> museumsFut =
        CompletableFuture.supplyAsync(() -> museumClient.getMuseumsByIds(museumIds), enrichmentExecutor);

    joinAll(artistsFut, museumsFut);

    return toInfo(p, artistsFut.join(), museumsFut.join());
  }

  private static PaintingInfoResponse toInfo(PaintingResponse p,
      Map<UUID, ArtistInfoResponse> artists, Map<UUID, MuseumInfoResponse> museums) {
    ArtistInfoResponse artist = artists.get(UUID.fromString(p.getArtistId()));
    MuseumInfoResponse museum = p.hasMuseumId() ? museums.get(UUID.fromString(p.getMuseumId())) : null;
    return new PaintingInfoResponse(UUID.fromString(p.getId()), p.getTitle(), p.getDescription(),
        p.getContent(), artist, museum);
  }

  private static void joinAll(CompletableFuture<?>... futures) {
    try {
      CompletableFuture.allOf(futures).join();
    } catch (CompletionException e) {
      if (e.getCause() instanceof StatusRuntimeException sre) {
        throw sre;
      }
      throw e;
    }
  }
}
