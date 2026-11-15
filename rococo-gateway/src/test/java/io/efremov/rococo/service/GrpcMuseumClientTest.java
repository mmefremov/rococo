package io.efremov.rococo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.efremov.rococo.grpc.CreateMuseumRequest;
import io.efremov.rococo.grpc.MuseumByIdRequest;
import io.efremov.rococo.grpc.MuseumPageRequest;
import io.efremov.rococo.grpc.MuseumPageResponse;
import io.efremov.rococo.grpc.MuseumResponse;
import io.efremov.rococo.grpc.MuseumServiceGrpc;
import io.efremov.rococo.grpc.MuseumsByIdRequest;
import io.efremov.rococo.grpc.MuseumsResponse;
import io.efremov.rococo.grpc.UpdateMuseumRequest;
import io.efremov.rococo.model.CountryInfoRequest;
import io.efremov.rococo.model.CountryInfoResponse;
import io.efremov.rococo.model.CreateMuseumInfoRequest;
import io.efremov.rococo.model.GeoInfoRequest;
import io.efremov.rococo.model.GeoInfoResponse;
import io.efremov.rococo.model.MuseumInfoResponse;
import io.efremov.rococo.model.PageResponse;
import io.efremov.rococo.model.UpdateMuseumInfoRequest;
import io.grpc.Status;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class GrpcMuseumClientTest {

  @Mock
  private MuseumServiceGrpc.MuseumServiceBlockingStub museumStub;
  @Mock
  private GrpcGeoClient geoClient;
  private GrpcMuseumClient client;

  private final UUID id = UUID.randomUUID();
  private final UUID countryId = UUID.randomUUID();
  private final CountryInfoResponse country = new CountryInfoResponse(countryId, "France");

  @BeforeEach
  void setUp() {
    client = new GrpcMuseumClient(geoClient);
    ReflectionTestUtils.setField(client, "museumStub", museumStub);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "Louvre"})
  void getAllMuseums_withFilter_enrichesPage(String title) {
    when(museumStub.getAllMuseums(any())).thenReturn(MuseumPageResponse.newBuilder()
        .addContent(museum()).setTotalElements(3).setTotalPages(2).setPageNumber(1)
        .setPageSize(2).setLast(true).setFirst(false).setNumberOfElements(1).build());
    when(geoClient.getCountriesByIds(Set.of(countryId))).thenReturn(Map.of(countryId, country));

    assertThat(client.getAllMuseums(1, 2, title)).as("museum page")
        .isEqualTo(new PageResponse<>(List.of(model()), 3, 2, 1, 2, true, false, 1));
    var expected = MuseumPageRequest.newBuilder().setPage(1).setSize(2);
    if (title != null && !title.isBlank()) {
      expected.setTitle("Louvre");
    }
    verify(museumStub).getAllMuseums(expected.build());
  }

  @Test
  void getAllMuseums_emptyPage_skipsEnrichment() {
    when(museumStub.getAllMuseums(any())).thenReturn(MuseumPageResponse.newBuilder()
        .setPageSize(10).setFirst(true).setLast(true).build());

    assertThat(client.getAllMuseums(0, 10, null)).as("empty page")
        .isEqualTo(new PageResponse<>(List.of(), 0, 0, 0, 10, true, true, 0));
    verifyNoInteractions(geoClient);
  }

  @Test
  void getAllMuseums_sharedCountry_enrichesInSingleBatch() {
    UUID secondId = UUID.randomUUID();
    when(museumStub.getAllMuseums(any())).thenReturn(MuseumPageResponse.newBuilder()
        .addContent(museum()).addContent(museum().toBuilder().setId(secondId.toString())).build());
    when(geoClient.getCountriesByIds(Set.of(countryId))).thenReturn(Map.of(countryId, country));

    var result = client.getAllMuseums(0, 10, null);

    assertThat(result.content()).as("museum order")
        .extracting(MuseumInfoResponse::id).containsExactly(id, secondId);
    assertThat(result.content()).as("enriched countries")
        .extracting(m -> m.geo().country()).containsExactly(country, country);
    verify(geoClient).getCountriesByIds(Set.of(countryId));
  }

  @Test
  void getMuseumsByIds_emptyIds_skipsRpc() {
    assertThat(client.getMuseumsByIds(List.of())).as("museums").isEmpty();
    verifyNoInteractions(museumStub, geoClient);
  }

  @Test
  void getMuseumsByIds_existingIds_returnsEnrichedMap() {
    when(museumStub.getMuseumsByIds(MuseumsByIdRequest.newBuilder().addId(id.toString()).build()))
        .thenReturn(MuseumsResponse.newBuilder().addMuseums(museum()).build());
    when(geoClient.getCountriesByIds(Set.of(countryId))).thenReturn(Map.of(countryId, country));

    assertThat(client.getMuseumsByIds(List.of(id))).as("museums").isEqualTo(Map.of(id, model()));
  }

  @Test
  void getMuseumsByIds_partiallyMissingIds_failsBeforeEnrichment() {
    UUID missing = UUID.randomUUID();
    when(museumStub.getMuseumsByIds(any()))
        .thenReturn(MuseumsResponse.newBuilder().addMuseums(museum()).build());

    assertThatThrownBy(() -> client.getMuseumsByIds(List.of(id, missing))).as("missing museum")
        .hasMessage("INVALID_ARGUMENT: Museum not found: " + missing);
    verifyNoInteractions(geoClient);
  }

  @Test
  void getMuseumById_existingId_enrichesCountry() {
    when(museumStub.getMuseumById(MuseumByIdRequest.newBuilder().setId(id.toString()).build()))
        .thenReturn(museum());
    when(geoClient.getCountriesByIds(Set.of(countryId))).thenReturn(Map.of(countryId, country));

    assertThat(client.getMuseumById(id)).as("museum").isEqualTo(model());
  }

  @Test
  void createMuseum_validRequest_mapsRequestAndResponse() {
    when(geoClient.getCountriesByIds(Set.of(countryId))).thenReturn(Map.of(countryId, country));
    when(museumStub.createMuseum(CreateMuseumRequest.newBuilder().setTitle("Louvre")
        .setDescription("French museum").setPhoto("photo").setCity("Paris")
        .setCountryId(countryId.toString()).build())).thenReturn(museum());

    assertThat(client.createMuseum(new CreateMuseumInfoRequest("Louvre", "French museum", "photo", geo())))
        .as("created museum").isEqualTo(model());
  }

  @Test
  void updateMuseum_validRequest_mapsRequestAndResponse() {
    when(geoClient.getCountriesByIds(Set.of(countryId))).thenReturn(Map.of(countryId, country));
    when(museumStub.updateMuseum(UpdateMuseumRequest.newBuilder().setId(id.toString()).setTitle("Louvre")
        .setDescription("French museum").setPhoto("photo").setCity("Paris")
        .setCountryId(countryId.toString()).build())).thenReturn(museum());

    assertThat(client.updateMuseum(new UpdateMuseumInfoRequest(id, "Louvre", "French museum", "photo", geo())))
        .as("updated museum").isEqualTo(model());
  }

  @Test
  void createMuseum_missingCountry_doesNotCreateMuseum() {
    var error = Status.INVALID_ARGUMENT.withDescription("Country not found: " + countryId).asRuntimeException();
    when(geoClient.getCountriesByIds(Set.of(countryId))).thenThrow(error);

    assertThatThrownBy(() -> client.createMuseum(
        new CreateMuseumInfoRequest("Louvre", "French museum", "photo", geo())))
        .as("missing country").isSameAs(error);
    verifyNoInteractions(museumStub);
  }

  @Test
  void updateMuseum_missingCountry_doesNotUpdateMuseum() {
    var error = Status.INVALID_ARGUMENT.asRuntimeException();
    when(geoClient.getCountriesByIds(Set.of(countryId))).thenThrow(error);

    assertThatThrownBy(() -> client.updateMuseum(
        new UpdateMuseumInfoRequest(id, "Louvre", "French museum", "photo", geo())))
        .as("missing country").isSameAs(error);
    verifyNoInteractions(museumStub);
  }

  private GeoInfoRequest geo() {
    return new GeoInfoRequest("Paris", new CountryInfoRequest(countryId));
  }

  private MuseumResponse museum() {
    return MuseumResponse.newBuilder().setId(id.toString()).setTitle("Louvre")
        .setDescription("French museum").setPhoto("photo").setCity("Paris")
        .setCountryId(countryId.toString()).build();
  }

  private MuseumInfoResponse model() {
    return new MuseumInfoResponse(id, "Louvre", "French museum", "photo", new GeoInfoResponse("Paris", country));
  }
}
