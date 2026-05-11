package io.efremov.rococo.service;

import io.efremov.rococo.common.grpc.GrpcServiceTemplate;
import io.efremov.rococo.common.grpc.PageMetadata;
import io.efremov.rococo.data.PaintingEntity;
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
public class GrpcPaintingService extends PaintingServiceGrpc.PaintingServiceImplBase {

  private final PaintingService paintingService;

  @Override
  public void getAllPaintings(PaintingPageRequest request, StreamObserver<PaintingPageResponse> responseObserver) {
    log.info("gRPC getAllPaintings called, page={}, size={}", request.getPage(), request.getSize());
    GrpcServiceTemplate.unary(responseObserver, () -> {
      String title = request.hasTitle() ? request.getTitle() : null;
      Page<PaintingEntity> page = paintingService.findAll(title,
          PageRequest.of(request.getPage(), request.getSize(), Sort.by("title").ascending()));
      return toPageResponse(page);
    });
  }

  @Override
  public void getPaintingById(PaintingByIdRequest request, StreamObserver<PaintingResponse> responseObserver) {
    log.info("gRPC getPaintingById called, id={}", request.getId());
    GrpcServiceTemplate.unary(responseObserver, () -> {
      PaintingEntity painting = paintingService.findById(UUID.fromString(request.getId()));
      return toResponse(painting);
    });
  }

  @Override
  public void getPaintingsByIds(PaintingsByIdRequest request, StreamObserver<PaintingsResponse> responseObserver) {
    log.info("gRPC getPaintingsByIds called, count={}", request.getIdCount());
    GrpcServiceTemplate.unary(responseObserver, () -> {
      List<UUID> ids = request.getIdList().stream().map(UUID::fromString).toList();
      List<PaintingEntity> paintings = paintingService.findAllByIds(ids);
      return PaintingsResponse.newBuilder()
          .addAllPaintings(paintings.stream().map(this::toResponse).toList())
          .build();
    });
  }

  @Override
  public void getPaintingsByArtist(PaintingByArtistRequest request,
      StreamObserver<PaintingPageResponse> responseObserver) {
    log.info("gRPC getPaintingsByArtist called, artistId={}", request.getArtistId());
    GrpcServiceTemplate.unary(responseObserver, () -> {
      Page<PaintingEntity> page = paintingService.findByArtistId(UUID.fromString(request.getArtistId()),
          PageRequest.of(request.getPage(), request.getSize(), Sort.by("title").ascending())
      );
      return toPageResponse(page);
    });
  }

  @Override
  public void createPainting(CreatePaintingRequest request, StreamObserver<PaintingResponse> responseObserver) {
    log.info("gRPC createPainting called, title={}", request.getTitle());
    GrpcServiceTemplate.unary(responseObserver, () -> {
      UUID artistId = UUID.fromString(request.getArtistId());
      UUID museumId = request.hasMuseumId() && !request.getMuseumId().isEmpty()
          ? UUID.fromString(request.getMuseumId()) : null;
      PaintingEntity painting = paintingService.create(
          request.getTitle(), request.getDescription(), request.getContent(), artistId, museumId
      );
      return toResponse(painting);
    });
  }

  @Override
  public void updatePainting(UpdatePaintingRequest request, StreamObserver<PaintingResponse> responseObserver) {
    log.info("gRPC updatePainting called, id={}", request.getId());
    GrpcServiceTemplate.unary(responseObserver, () -> {
      UUID id = UUID.fromString(request.getId());
      UUID artistId = UUID.fromString(request.getArtistId());
      UUID museumId = request.hasMuseumId() && !request.getMuseumId().isEmpty()
          ? UUID.fromString(request.getMuseumId()) : null;
      PaintingEntity painting = paintingService.update(
          id, request.getTitle(), request.getDescription(), request.getContent(), artistId, museumId
      );
      return toResponse(painting);
    });
  }

  private PaintingPageResponse toPageResponse(Page<PaintingEntity> page) {
    PageMetadata meta = PageMetadata.from(page);
    return PaintingPageResponse.newBuilder()
        .addAllContent(page.getContent().stream().map(this::toResponse).toList())
        .setTotalElements(meta.totalElements())
        .setTotalPages(meta.totalPages())
        .setPageNumber(meta.pageNumber())
        .setPageSize(meta.pageSize())
        .setLast(meta.last())
        .setFirst(meta.first())
        .setNumberOfElements(meta.numberOfElements())
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
