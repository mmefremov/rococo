package io.efremov.rococo.service;

import io.efremov.rococo.data.CountryEntity;
import io.efremov.rococo.grpc.CountriesByIdRequest;
import io.efremov.rococo.grpc.CountriesResponse;
import io.efremov.rococo.grpc.CountryPageRequest;
import io.efremov.rococo.grpc.CountryPageResponse;
import io.efremov.rococo.grpc.CountryResponse;
import io.efremov.rococo.grpc.GeoServiceGrpc;
import io.grpc.Status;
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
    try {
      Page<CountryEntity> page = countryService.findAll(
          PageRequest.of(request.getPage(), request.getSize(), Sort.by("name").ascending()));
      CountryPageResponse response = CountryPageResponse.newBuilder()
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
      log.error("Unexpected error in getAllCountries", e);
      responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
    }
  }

  @Override
  public void getCountriesByIds(CountriesByIdRequest request, StreamObserver<CountriesResponse> responseObserver) {
    log.info("gRPC getCountriesByIds called, count={}", request.getIdCount());
    try {
      List<UUID> ids = request.getIdList().stream().map(UUID::fromString).toList();
      List<CountryEntity> countries = countryService.findAllByIds(ids);
      CountriesResponse response = CountriesResponse.newBuilder()
          .addAllCountries(countries.stream().map(this::toResponse).toList())
          .build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (IllegalArgumentException e) {
      log.warn("Invalid country id in getCountriesByIds: {}", e.getMessage());
      responseObserver.onError(
          Status.INVALID_ARGUMENT.withDescription("Invalid id: " + e.getMessage()).asRuntimeException());
    } catch (Exception e) {
      log.error("Unexpected error in getCountriesByIds", e);
      responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
    }
  }

  private CountryResponse toResponse(CountryEntity country) {
    return CountryResponse.newBuilder()
        .setId(country.getId().toString())
        .setName(country.getName())
        .build();
  }
}
