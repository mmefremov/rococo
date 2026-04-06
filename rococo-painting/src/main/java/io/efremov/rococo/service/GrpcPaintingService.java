package io.efremov.rococo.service;

import io.efremov.rococo.data.PaintingEntity;
import io.efremov.rococo.exception.PaintingNotFoundException;
import io.efremov.rococo.grpc.CreatePaintingRequest;
import io.efremov.rococo.grpc.PaintingByArtistRequest;
import io.efremov.rococo.grpc.PaintingByIdRequest;
import io.efremov.rococo.grpc.PaintingPageRequest;
import io.efremov.rococo.grpc.PaintingPageResponse;
import io.efremov.rococo.grpc.PaintingResponse;
import io.efremov.rococo.grpc.PaintingServiceGrpc;
import io.efremov.rococo.grpc.PaintingsByIdRequest;
import io.efremov.rococo.grpc.PaintingsResponse;
import io.efremov.rococo.grpc.UpdatePaintingRequest;
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
public class GrpcPaintingService extends PaintingServiceGrpc.PaintingServiceImplBase {

  private final PaintingService paintingService;

  @Override
  public void getAllPaintings(PaintingPageRequest request, StreamObserver<PaintingPageResponse> responseObserver) {
    log.info("gRPC getAllPaintings called, page={}, size={}", request.getPage(), request.getSize());
    try {
      String title = request.hasTitle() ? request.getTitle() : null;
      Page<PaintingEntity> page = paintingService.findAll(title,
          PageRequest.of(request.getPage(), request.getSize(), Sort.by("title").ascending()));
      responseObserver.onNext(toPageResponse(page));
      responseObserver.onCompleted();
    } catch (IllegalArgumentException e) {
      responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
    } catch (Exception e) {
      log.error("Unexpected error in getAllPaintings", e);
      responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
    }
  }

  @Override
  public void getPaintingById(PaintingByIdRequest request, StreamObserver<PaintingResponse> responseObserver) {
    log.info("gRPC getPaintingById called, id={}", request.getId());
    try {
      PaintingEntity painting = paintingService.findById(UUID.fromString(request.getId()));
      responseObserver.onNext(toResponse(painting));
      responseObserver.onCompleted();
    } catch (PaintingNotFoundException e) {
      log.warn("Painting not found: {}", request.getId());
      responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
    } catch (IllegalArgumentException e) {
      log.warn("Invalid painting id: {}", request.getId());
      responseObserver.onError(
          Status.INVALID_ARGUMENT.withDescription("Invalid id: " + request.getId()).asRuntimeException());
    } catch (Exception e) {
      log.error("Unexpected error in getPaintingById for id: {}", request.getId(), e);
      responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
    }
  }

  @Override
  public void getPaintingsByIds(PaintingsByIdRequest request, StreamObserver<PaintingsResponse> responseObserver) {
    log.info("gRPC getPaintingsByIds called, count={}", request.getIdCount());
    try {
      List<UUID> ids = request.getIdList().stream().map(UUID::fromString).toList();
      List<PaintingEntity> paintings = paintingService.findAllByIds(ids);
      PaintingsResponse response = PaintingsResponse.newBuilder()
          .addAllPaintings(paintings.stream().map(this::toResponse).toList())
          .build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (IllegalArgumentException e) {
      log.warn("Invalid painting id in getPaintingsByIds: {}", e.getMessage());
      responseObserver.onError(
          Status.INVALID_ARGUMENT.withDescription("Invalid id: " + e.getMessage()).asRuntimeException());
    } catch (Exception e) {
      log.error("Unexpected error in getPaintingsByIds", e);
      responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
    }
  }

  @Override
  public void getPaintingsByArtist(PaintingByArtistRequest request,
      StreamObserver<PaintingPageResponse> responseObserver) {
    log.info("gRPC getPaintingsByArtist called, artistId={}", request.getArtistId());
    try {
      Page<PaintingEntity> page = paintingService.findByArtistId(UUID.fromString(request.getArtistId()),
          PageRequest.of(request.getPage(), request.getSize(), Sort.by("title").ascending())
      );
      responseObserver.onNext(toPageResponse(page));
      responseObserver.onCompleted();
    } catch (IllegalArgumentException e) {
      responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
    } catch (Exception e) {
      log.error("Unexpected error in getPaintingsByArtist for artistId: {}", request.getArtistId(), e);
      responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
    }
  }

  @Override
  public void createPainting(CreatePaintingRequest request, StreamObserver<PaintingResponse> responseObserver) {
    log.info("gRPC createPainting called, title={}", request.getTitle());
    try {
      UUID artistId = UUID.fromString(request.getArtistId());
      UUID museumId = request.hasMuseumId() && !request.getMuseumId().isEmpty()
          ? UUID.fromString(request.getMuseumId()) : null;
      PaintingEntity painting = paintingService.create(
          request.getTitle(), request.getDescription(), request.getContent(), artistId, museumId
      );
      responseObserver.onNext(toResponse(painting));
      responseObserver.onCompleted();
    } catch (DataIntegrityViolationException e) {
      log.warn("Painting already exists: {}", request.getTitle());
      responseObserver.onError(
          Status.ALREADY_EXISTS.withDescription("Painting already exists: " + request.getTitle()).asRuntimeException());
    } catch (IllegalArgumentException e) {
      log.warn("Invalid argument in createPainting: {}", e.getMessage());
      responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
    } catch (Exception e) {
      log.error("Unexpected error in createPainting", e);
      responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
    }
  }

  @Override
  public void updatePainting(UpdatePaintingRequest request, StreamObserver<PaintingResponse> responseObserver) {
    log.info("gRPC updatePainting called, id={}", request.getId());
    try {
      UUID id = UUID.fromString(request.getId());
      UUID artistId = UUID.fromString(request.getArtistId());
      UUID museumId = request.hasMuseumId() && !request.getMuseumId().isEmpty()
          ? UUID.fromString(request.getMuseumId()) : null;
      PaintingEntity painting = paintingService.update(
          id, request.getTitle(), request.getDescription(), request.getContent(), artistId, museumId
      );
      responseObserver.onNext(toResponse(painting));
      responseObserver.onCompleted();
    } catch (PaintingNotFoundException e) {
      log.warn("Painting not found for update: {}", request.getId());
      responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
    } catch (DataIntegrityViolationException e) {
      log.warn("Painting already exists: {}", request.getTitle());
      responseObserver.onError(
          Status.ALREADY_EXISTS.withDescription("Painting already exists: " + request.getTitle()).asRuntimeException());
    } catch (IllegalArgumentException e) {
      log.warn("Invalid argument in updatePainting: {}", e.getMessage());
      responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
    } catch (Exception e) {
      log.error("Unexpected error in updatePainting for id: {}", request.getId(), e);
      responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
    }
  }

  private PaintingPageResponse toPageResponse(Page<PaintingEntity> page) {
    return PaintingPageResponse.newBuilder()
        .addAllContent(page.getContent().stream().map(this::toResponse).toList())
        .setTotalElements((int) page.getTotalElements())
        .setTotalPages(page.getTotalPages())
        .setPageNumber(page.getNumber())
        .setPageSize(page.getSize())
        .setLast(page.isLast())
        .setFirst(page.isFirst())
        .setNumberOfElements(page.getNumberOfElements())
        .build();
  }

  private PaintingResponse toResponse(PaintingEntity painting) {
    PaintingResponse.Builder builder = PaintingResponse.newBuilder()
        .setId(painting.getId().toString())
        .setTitle(painting.getTitle())
        .setDescription(painting.getDescription())
        .setContent(new String(painting.getContent()))
        .setArtistId(painting.getArtistId().toString());
    if (painting.getMuseumId() != null) {
      builder.setMuseumId(painting.getMuseumId().toString());
    }
    return builder.build();
  }
}
