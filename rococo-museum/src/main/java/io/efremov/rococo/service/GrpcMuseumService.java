package io.efremov.rococo.service;

import io.efremov.rococo.data.MuseumEntity;
import io.efremov.rococo.exception.MuseumNotFoundException;
import io.efremov.rococo.grpc.CreateMuseumRequest;
import io.efremov.rococo.grpc.MuseumByIdRequest;
import io.efremov.rococo.grpc.MuseumPageRequest;
import io.efremov.rococo.grpc.MuseumPageResponse;
import io.efremov.rococo.grpc.MuseumResponse;
import io.efremov.rococo.grpc.MuseumServiceGrpc;
import io.efremov.rococo.grpc.MuseumsByIdRequest;
import io.efremov.rococo.grpc.MuseumsResponse;
import io.efremov.rococo.grpc.UpdateMuseumRequest;
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
public class GrpcMuseumService extends MuseumServiceGrpc.MuseumServiceImplBase {

  private final MuseumService museumService;

  @Override
  public void getAllMuseums(MuseumPageRequest request, StreamObserver<MuseumPageResponse> responseObserver) {
    log.info("gRPC getAllMuseums called, page={}, size={}", request.getPage(), request.getSize());
    try {
      String title = request.hasTitle() ? request.getTitle() : null;
      Page<MuseumEntity> page = museumService.findAll(title,
          PageRequest.of(request.getPage(), request.getSize(), Sort.by("title").ascending()));
      MuseumPageResponse response = MuseumPageResponse.newBuilder()
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
      log.error("Unexpected error in getAllMuseums", e);
      responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
    }
  }

  @Override
  public void getMuseumById(MuseumByIdRequest request, StreamObserver<MuseumResponse> responseObserver) {
    log.info("gRPC getMuseumById called, id={}", request.getId());
    try {
      MuseumEntity museum = museumService.findById(UUID.fromString(request.getId()));
      responseObserver.onNext(toResponse(museum));
      responseObserver.onCompleted();
    } catch (MuseumNotFoundException e) {
      log.warn("Museum not found: {}", request.getId());
      responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
    } catch (IllegalArgumentException e) {
      log.warn("Invalid museum id: {}", request.getId());
      responseObserver.onError(
          Status.INVALID_ARGUMENT.withDescription("Invalid id: " + request.getId()).asRuntimeException());
    } catch (Exception e) {
      log.error("Unexpected error in getMuseumById for id: {}", request.getId(), e);
      responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
    }
  }

  @Override
  public void getMuseumsByIds(MuseumsByIdRequest request, StreamObserver<MuseumsResponse> responseObserver) {
    log.info("gRPC getMuseumsByIds called, count={}", request.getIdCount());
    try {
      List<UUID> ids = request.getIdList().stream().map(UUID::fromString).toList();
      List<MuseumEntity> museums = museumService.findAllByIds(ids);
      MuseumsResponse response = MuseumsResponse.newBuilder()
          .addAllMuseums(museums.stream().map(this::toResponse).toList())
          .build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (IllegalArgumentException e) {
      log.warn("Invalid museum id in getMuseumsByIds: {}", e.getMessage());
      responseObserver.onError(
          Status.INVALID_ARGUMENT.withDescription("Invalid id: " + e.getMessage()).asRuntimeException());
    } catch (Exception e) {
      log.error("Unexpected error in getMuseumsByIds", e);
      responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
    }
  }

  @Override
  public void createMuseum(CreateMuseumRequest request, StreamObserver<MuseumResponse> responseObserver) {
    log.info("gRPC createMuseum called, title={}", request.getTitle());
    try {
      UUID countryId = UUID.fromString(request.getCountryId());
      MuseumEntity museum = museumService.create(
          request.getTitle(), request.getDescription(), request.getPhoto(),
          request.getCity(), countryId
      );
      responseObserver.onNext(toResponse(museum));
      responseObserver.onCompleted();
    } catch (DataIntegrityViolationException e) {
      log.warn("Museum already exists: {}", request.getTitle());
      responseObserver.onError(
          Status.ALREADY_EXISTS.withDescription("Museum title already taken: " + request.getTitle())
              .asRuntimeException());
    } catch (IllegalArgumentException e) {
      log.warn("Invalid argument in createMuseum: {}", e.getMessage());
      responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
    } catch (Exception e) {
      log.error("Unexpected error in createMuseum", e);
      responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
    }
  }

  @Override
  public void updateMuseum(UpdateMuseumRequest request, StreamObserver<MuseumResponse> responseObserver) {
    log.info("gRPC updateMuseum called, id={}", request.getId());
    try {
      UUID id = UUID.fromString(request.getId());
      UUID countryId = UUID.fromString(request.getCountryId());
      MuseumEntity museum = museumService.update(
          id, request.getTitle(), request.getDescription(),
          request.getPhoto(), request.getCity(), countryId
      );
      responseObserver.onNext(toResponse(museum));
      responseObserver.onCompleted();
    } catch (MuseumNotFoundException e) {
      log.warn("Museum not found for update: {}", request.getId());
      responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
    } catch (DataIntegrityViolationException e) {
      log.warn("Duplicate museum title on update: {}", request.getTitle());
      responseObserver.onError(
          Status.ALREADY_EXISTS.withDescription("Museum title already taken: " + request.getTitle())
              .asRuntimeException());
    } catch (IllegalArgumentException e) {
      log.warn("Invalid argument in updateMuseum: {}", e.getMessage());
      responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
    } catch (Exception e) {
      log.error("Unexpected error in updateMuseum for id: {}", request.getId(), e);
      responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
    }
  }

  private MuseumResponse toResponse(MuseumEntity museum) {
    return MuseumResponse.newBuilder()
        .setId(museum.getId().toString())
        .setTitle(museum.getTitle())
        .setDescription(museum.getDescription())
        .setPhoto(new String(museum.getPhoto()))
        .setCity(museum.getCity())
        .setCountryId(museum.getCountryId().toString())
        .build();
  }
}
