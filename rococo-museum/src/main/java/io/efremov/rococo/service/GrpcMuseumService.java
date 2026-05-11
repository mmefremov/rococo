package io.efremov.rococo.service;

import io.efremov.rococo.common.grpc.GrpcServiceTemplate;
import io.efremov.rococo.common.grpc.PageMetadata;
import io.efremov.rococo.data.MuseumEntity;
import io.efremov.rococo.grpc.CreateMuseumRequest;
import io.efremov.rococo.grpc.MuseumByIdRequest;
import io.efremov.rococo.grpc.MuseumPageRequest;
import io.efremov.rococo.grpc.MuseumPageResponse;
import io.efremov.rococo.grpc.MuseumResponse;
import io.efremov.rococo.grpc.MuseumServiceGrpc;
import io.efremov.rococo.grpc.MuseumsByIdRequest;
import io.efremov.rococo.grpc.MuseumsResponse;
import io.efremov.rococo.grpc.UpdateMuseumRequest;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
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
    GrpcServiceTemplate.unary(responseObserver, () -> {
      String title = request.hasTitle() ? request.getTitle() : null;
      Page<MuseumEntity> page = museumService.findAll(title,
          PageRequest.of(request.getPage(), request.getSize(), Sort.by("title").ascending()));
      PageMetadata meta = PageMetadata.from(page);
      return MuseumPageResponse.newBuilder()
          .addAllContent(page.getContent().stream().map(this::toResponse).toList())
          .setTotalElements(meta.totalElements())
          .setTotalPages(meta.totalPages())
          .setPageNumber(meta.pageNumber())
          .setPageSize(meta.pageSize())
          .setLast(meta.last())
          .setFirst(meta.first())
          .setNumberOfElements(meta.numberOfElements())
          .build();
    });
  }

  @Override
  public void getMuseumById(MuseumByIdRequest request, StreamObserver<MuseumResponse> responseObserver) {
    log.info("gRPC getMuseumById called, id={}", request.getId());
    GrpcServiceTemplate.unary(responseObserver, () -> {
      MuseumEntity museum = museumService.findById(UUID.fromString(request.getId()));
      return toResponse(museum);
    });
  }

  @Override
  public void getMuseumsByIds(MuseumsByIdRequest request, StreamObserver<MuseumsResponse> responseObserver) {
    log.info("gRPC getMuseumsByIds called, count={}", request.getIdCount());
    GrpcServiceTemplate.unary(responseObserver, () -> {
      List<UUID> ids = request.getIdList().stream().map(UUID::fromString).toList();
      List<MuseumEntity> museums = museumService.findAllByIds(ids);
      return MuseumsResponse.newBuilder()
          .addAllMuseums(museums.stream().map(this::toResponse).toList())
          .build();
    });
  }

  @Override
  public void createMuseum(CreateMuseumRequest request, StreamObserver<MuseumResponse> responseObserver) {
    log.info("gRPC createMuseum called, title={}", request.getTitle());
    GrpcServiceTemplate.unary(responseObserver, () -> {
      UUID countryId = UUID.fromString(request.getCountryId());
      MuseumEntity museum = museumService.create(
          request.getTitle(), request.getDescription(), request.getPhoto(),
          request.getCity(), countryId
      );
      return toResponse(museum);
    });
  }

  @Override
  public void updateMuseum(UpdateMuseumRequest request, StreamObserver<MuseumResponse> responseObserver) {
    log.info("gRPC updateMuseum called, id={}", request.getId());
    GrpcServiceTemplate.unary(responseObserver, () -> {
      UUID id = UUID.fromString(request.getId());
      UUID countryId = UUID.fromString(request.getCountryId());
      MuseumEntity museum = museumService.update(
          id, request.getTitle(), request.getDescription(),
          request.getPhoto(), request.getCity(), countryId
      );
      return toResponse(museum);
    });
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
