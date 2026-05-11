package io.efremov.rococo.service;

import io.efremov.rococo.common.grpc.GrpcServiceTemplate;
import io.efremov.rococo.common.grpc.PageMetadata;
import io.efremov.rococo.data.CountryEntity;
import io.efremov.rococo.grpc.CountriesByIdRequest;
import io.efremov.rococo.grpc.CountriesResponse;
import io.efremov.rococo.grpc.CountryPageRequest;
import io.efremov.rococo.grpc.CountryPageResponse;
import io.efremov.rococo.grpc.CountryResponse;
import io.efremov.rococo.grpc.GeoServiceGrpc;
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
public class GrpcGeoService extends GeoServiceGrpc.GeoServiceImplBase {

  private final CountryService countryService;

  @Override
  public void getAllCountries(CountryPageRequest request, StreamObserver<CountryPageResponse> responseObserver) {
    log.info("gRPC getAllCountries called, page={}, size={}", request.getPage(), request.getSize());
    GrpcServiceTemplate.unary(responseObserver, () -> {
      Page<CountryEntity> page = countryService.findAll(
          PageRequest.of(request.getPage(), request.getSize(), Sort.by("name").ascending()));
      PageMetadata meta = PageMetadata.from(page);
      return CountryPageResponse.newBuilder()
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
  public void getCountriesByIds(CountriesByIdRequest request, StreamObserver<CountriesResponse> responseObserver) {
    log.info("gRPC getCountriesByIds called, count={}", request.getIdCount());
    GrpcServiceTemplate.unary(responseObserver, () -> {
      List<UUID> ids = request.getIdList().stream().map(UUID::fromString).toList();
      List<CountryEntity> countries = countryService.findAllByIds(ids);
      return CountriesResponse.newBuilder()
          .addAllCountries(countries.stream().map(this::toResponse).toList())
          .build();
    });
  }

  private CountryResponse toResponse(CountryEntity country) {
    return CountryResponse.newBuilder()
        .setId(country.getId().toString())
        .setName(country.getName())
        .build();
  }
}
