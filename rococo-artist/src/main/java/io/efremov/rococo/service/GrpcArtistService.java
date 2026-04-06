package io.efremov.rococo.service;

import io.efremov.rococo.data.ArtistEntity;
import io.efremov.rococo.exception.ArtistNotFoundException;
import io.efremov.rococo.grpc.ArtistByIdRequest;
import io.efremov.rococo.grpc.ArtistPageRequest;
import io.efremov.rococo.grpc.ArtistPageResponse;
import io.efremov.rococo.grpc.ArtistResponse;
import io.efremov.rococo.grpc.ArtistServiceGrpc;
import io.efremov.rococo.grpc.ArtistsResponse;
import io.efremov.rococo.grpc.CreateArtistRequest;
import io.efremov.rococo.grpc.UpdateArtistRequest;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class GrpcArtistService extends ArtistServiceGrpc.ArtistServiceImplBase {

  private final ArtistService artistService;

  @Override
  public void getAllArtists(ArtistPageRequest request, StreamObserver<ArtistPageResponse> responseObserver) {
    log.info("gRPC getAllArtists called, page={}, size={}", request.getPage(), request.getSize());
    try {
      String name = request.hasName() ? request.getName() : null;
      Page<ArtistEntity> page = artistService.findAll(name,
          PageRequest.of(request.getPage(), request.getSize(), Sort.by("name").ascending()));
      ArtistPageResponse response = ArtistPageResponse.newBuilder()
          .addAllContent(page.getContent().stream().map(this::toResponse).toList())
          .setTotalElements((int) page.getTotalElements())
          .setTotalPages(page.getTotalPages())
          .setPageNumber(page.getNumber())
          .setPageSize(page.getSize())
          .setLast(page.isLast())
          .setFirst(page.isFirst())
          .setNumberOfElements(page.getNumberOfElements())
          .build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (IllegalArgumentException e) {
      responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
    } catch (Exception e) {
      log.error("Unexpected error in getAllArtists", e);
      responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
    }
  }

  @Override
  public void getArtistById(ArtistByIdRequest request, StreamObserver<ArtistsResponse> responseObserver) {
    log.info("gRPC getArtistById called, ids count={}", request.getIdCount());
    try {
      List<UUID> ids = request.getIdList().stream().map(UUID::fromString).toList();
      List<ArtistEntity> artists = artistService.findAllByIds(ids);
      ArtistsResponse response = ArtistsResponse.newBuilder()
          .addAllArtists(artists.stream().map(this::toResponse).toList())
          .build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (IllegalArgumentException e) {
      log.warn("Invalid artist id in getArtistById: {}", e.getMessage());
      responseObserver.onError(
          Status.INVALID_ARGUMENT.withDescription("Invalid id: " + e.getMessage()).asRuntimeException());
    } catch (Exception e) {
      log.error("Unexpected error in getArtistById", e);
      responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
    }
  }

  @Override
  public void createArtist(CreateArtistRequest request, StreamObserver<ArtistResponse> responseObserver) {
    log.info("gRPC createArtist called, name={}", request.getName());
    try {
      ArtistEntity artist = artistService.create(request.getName(), request.getBiography(), request.getPhoto());
      responseObserver.onNext(toResponse(artist));
      responseObserver.onCompleted();
    } catch (DataIntegrityViolationException e) {
      log.warn("Artist already exists: {}", request.getName());
      responseObserver.onError(
          Status.ALREADY_EXISTS.withDescription("Artist already exists: " + request.getName()).asRuntimeException());
    } catch (IllegalArgumentException e) {
      log.warn("Invalid argument in artist operation: {}", e.getMessage());
      responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
    } catch (Exception e) {
      log.error("Unexpected error in createArtist", e);
      responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
    }
  }

  @Override
  public void updateArtist(UpdateArtistRequest request, StreamObserver<ArtistResponse> responseObserver) {
    log.info("gRPC updateArtist called, id={}", request.getId());
    try {
      ArtistEntity artist = artistService.update(
          UUID.fromString(request.getId()),
          request.getName(),
          request.getBiography(),
          request.getPhoto()
      );
      responseObserver.onNext(toResponse(artist));
      responseObserver.onCompleted();
    } catch (ArtistNotFoundException e) {
      log.warn("Artist not found for update: {}", request.getId());
      responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
    } catch (DataIntegrityViolationException e) {
      log.warn("Duplicate artist name on update: {}", request.getName());
      responseObserver.onError(Status.ALREADY_EXISTS.withDescription("Artist name already taken: " + request.getName())
          .asRuntimeException());
    } catch (IllegalArgumentException e) {
      log.warn("Invalid argument in updateArtist: {}", e.getMessage());
      responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
    } catch (Exception e) {
      log.error("Unexpected error in updateArtist for id: {}", request.getId(), e);
      responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
    }
  }

  private ArtistResponse toResponse(ArtistEntity artist) {
    return ArtistResponse.newBuilder()
        .setId(artist.getId().toString())
        .setName(artist.getName())
        .setBiography(artist.getBiography())
        .setPhoto(new String(artist.getPhoto()))
        .build();
  }
}
