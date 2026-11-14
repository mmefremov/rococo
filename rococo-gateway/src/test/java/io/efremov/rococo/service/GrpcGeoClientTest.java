package io.efremov.rococo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.efremov.rococo.grpc.CountriesByIdRequest;
import io.efremov.rococo.grpc.CountriesResponse;
import io.efremov.rococo.grpc.CountryPageRequest;
import io.efremov.rococo.grpc.CountryPageResponse;
import io.efremov.rococo.grpc.CountryResponse;
import io.efremov.rococo.grpc.GeoServiceGrpc;
import io.efremov.rococo.model.CountryInfoResponse;
import io.efremov.rococo.model.PageResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GrpcGeoClientTest {

  @Mock
  private GeoServiceGrpc.GeoServiceBlockingStub geoStub;
  @InjectMocks
  private GrpcGeoClient client;

  private final UUID id = UUID.randomUUID();

  @Test
  void getAllCountries_returnsMappedPage() {
    when(geoStub.getAllCountries(CountryPageRequest.newBuilder().setPage(1).setSize(2).build()))
        .thenReturn(CountryPageResponse.newBuilder().addContent(country()).setTotalElements(3)
            .setTotalPages(2).setPageNumber(1).setPageSize(2).setFirst(false).setLast(true)
            .setNumberOfElements(1).build());

    assertThat(client.getAllCountries(1, 2)).as("country page").isEqualTo(
        new PageResponse<>(List.of(new CountryInfoResponse(id, "France")), 3, 2, 1, 2, true, false, 1));
  }

  @Test
  void getCountriesByIds_emptyIds_skipsRpc() {
    assertThat(client.getCountriesByIds(List.of())).as("countries").isEmpty();
    verifyNoInteractions(geoStub);
  }

  @Test
  void getCountriesByIds_existingIds_returnsMap() {
    when(geoStub.getCountriesByIds(CountriesByIdRequest.newBuilder().addId(id.toString()).build()))
        .thenReturn(CountriesResponse.newBuilder().addCountries(country()).build());

    assertThat(client.getCountriesByIds(List.of(id))).as("countries")
        .isEqualTo(Map.of(id, new CountryInfoResponse(id, "France")));
  }

  @Test
  void getCountriesByIds_partiallyMissingIds_throwsInvalidArgument() {
    UUID missing = UUID.randomUUID();
    when(geoStub.getCountriesByIds(any()))
        .thenReturn(CountriesResponse.newBuilder().addCountries(country()).build());

    assertThatThrownBy(() -> client.getCountriesByIds(List.of(id, missing))).as("missing country")
        .hasMessage("INVALID_ARGUMENT: Country not found: " + missing);
  }

  private CountryResponse country() {
    return CountryResponse.newBuilder().setId(id.toString()).setName("France").build();
  }
}
