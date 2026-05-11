package io.efremov.rococo.service;

import io.efremov.rococo.common.grpc.GrpcServiceTemplate;
import io.efremov.rococo.common.grpc.PageMetadata;
import io.efremov.rococo.common.util.ByteStringCodec;
import io.efremov.rococo.data.ArtistEntity;
import io.efremov.rococo.grpc.ArtistByIdRequest;
import io.efremov.rococo.grpc.ArtistPageRequest;
import io.efremov.rococo.grpc.ArtistPageResponse;
import io.efremov.rococo.grpc.ArtistResponse;
import io.efremov.rococo.grpc.ArtistServiceGrpc;
import io.efremov.rococo.grpc.ArtistsResponse;
import io.efremov.rococo.grpc.CreateArtistRequest;
import io.efremov.rococo.grpc.UpdateArtistRequest;
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
public class GrpcArtistService extends ArtistServiceGrpc.ArtistServiceImplBase {

  private final ArtistService artistService;

  @Override
  public void getAllArtists(ArtistPageRequest request, StreamObserver<ArtistPageResponse> responseObserver) {
    log.info("gRPC getAllArtists called, page={}, size={}", request.getPage(), request.getSize());
    GrpcServiceTemplate.unary(responseObserver, () -> {
      String name = request.hasName() ? request.getName() : null;
      Page<ArtistEntity> page = artistService.findAll(name,
          PageRequest.of(request.getPage(), request.getSize(), Sort.by("name").ascending()));
      PageMetadata meta = PageMetadata.from(page);
      return ArtistPageResponse.newBuilder()
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
  public void getArtistById(ArtistByIdRequest request, StreamObserver<ArtistsResponse> responseObserver) {
    log.info("gRPC getArtistById called, ids count={}", request.getIdCount());
    GrpcServiceTemplate.unary(responseObserver, () -> {
      List<UUID> ids = request.getIdList().stream().map(UUID::fromString).toList();
      List<ArtistEntity> artists = artistService.findAllByIds(ids);
      return ArtistsResponse.newBuilder()
          .addAllArtists(artists.stream().map(this::toResponse).toList())
          .build();
    });
  }

  @Override
  public void createArtist(CreateArtistRequest request, StreamObserver<ArtistResponse> responseObserver) {
    log.info("gRPC createArtist called, name={}", request.getName());
    GrpcServiceTemplate.unary(responseObserver, () -> {
      ArtistEntity artist = artistService.create(request.getName(), request.getBiography(), request.getPhoto());
      return toResponse(artist);
    });
  }

  @Override
  public void updateArtist(UpdateArtistRequest request, StreamObserver<ArtistResponse> responseObserver) {
    log.info("gRPC updateArtist called, id={}", request.getId());
    GrpcServiceTemplate.unary(responseObserver, () -> {
      ArtistEntity artist = artistService.update(
          UUID.fromString(request.getId()),
          request.getName(),
          request.getBiography(),
          request.getPhoto()
      );
      return toResponse(artist);
    });
  }

  private ArtistResponse toResponse(ArtistEntity artist) {
    return ArtistResponse.newBuilder()
        .setId(artist.getId().toString())
        .setName(artist.getName())
        .setBiography(artist.getBiography())
        .setPhoto(ByteStringCodec.decode(artist.getPhoto()))
        .build();
  }
}
