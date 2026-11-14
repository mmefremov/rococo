package io.efremov.rococo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.efremov.rococo.grpc.CreatePaintingRequest;
import io.efremov.rococo.grpc.PaintingByArtistRequest;
import io.efremov.rococo.grpc.PaintingByIdRequest;
import io.efremov.rococo.grpc.PaintingPageRequest;
import io.efremov.rococo.grpc.PaintingPageResponse;
import io.efremov.rococo.grpc.PaintingResponse;
import io.efremov.rococo.grpc.PaintingServiceGrpc;
import io.efremov.rococo.grpc.UpdatePaintingRequest;
import io.efremov.rococo.model.ArtistInfoRequest;
import io.efremov.rococo.model.ArtistInfoResponse;
import io.efremov.rococo.model.CountryInfoResponse;
import io.efremov.rococo.model.CreatePaintingInfoRequest;
import io.efremov.rococo.model.GeoInfoResponse;
import io.efremov.rococo.model.MuseumInfoRequest;
import io.efremov.rococo.model.MuseumInfoResponse;
import io.efremov.rococo.model.PageResponse;
import io.efremov.rococo.model.PaintingInfoResponse;
import io.efremov.rococo.model.UpdatePaintingInfoRequest;
import io.grpc.Status;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionException;
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
class GrpcPaintingClientTest {

  @Mock
  private PaintingServiceGrpc.PaintingServiceBlockingStub paintingStub;
  @Mock
  private GrpcArtistClient artistClient;
  @Mock
  private GrpcMuseumClient museumClient;

  private GrpcPaintingClient client;
  private final UUID id = UUID.randomUUID();
  private final UUID artistId = UUID.randomUUID();
  private final UUID museumId = UUID.randomUUID();
  private final ArtistInfoResponse artist = new ArtistInfoResponse(artistId, "Monet", "French painter", "photo");
  private final MuseumInfoResponse museum = new MuseumInfoResponse(museumId, "Louvre", "French museum", "photo",
      new GeoInfoResponse("Paris", new CountryInfoResponse(UUID.randomUUID(), "France")));

  @BeforeEach
  void setUp() {
    // Run submitted tasks immediately to keep enrichment and exception tests deterministic.
    client = new GrpcPaintingClient(artistClient, museumClient, Runnable::run);
    ReflectionTestUtils.setField(client, "paintingStub", paintingStub);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "Water lilies"})
  void getAllPaintings_withFilter_enrichesPage(String title) {
    when(paintingStub.getAllPaintings(any())).thenReturn(page(painting()));
    stubReferences();

    assertThat(client.getAllPaintings(1, 2, title)).as("painting page")
        .isEqualTo(new PageResponse<>(List.of(model(museum)), 3, 2, 1, 2, true, false, 1));
    var expected = PaintingPageRequest.newBuilder().setPage(1).setSize(2);
    if (title != null && !title.isBlank()) {
      expected.setTitle("Water lilies");
    }
    verify(paintingStub).getAllPaintings(expected.build());
  }

  @Test
  void getAllPaintings_emptyPage_skipsEnrichment() {
    when(paintingStub.getAllPaintings(any())).thenReturn(PaintingPageResponse.newBuilder()
        .setPageSize(10).setFirst(true).setLast(true).build());

    assertThat(client.getAllPaintings(0, 10, null)).as("empty page")
        .isEqualTo(new PageResponse<>(List.of(), 0, 0, 0, 10, true, true, 0));
    verifyNoInteractions(artistClient, museumClient);
  }

  @Test
  void getAllPaintings_sharedReferences_enrichesInSingleBatchAndPreservesOrder() {
    UUID secondId = UUID.randomUUID();
    when(paintingStub.getAllPaintings(any())).thenReturn(PaintingPageResponse.newBuilder()
        .addContent(painting()).addContent(painting().toBuilder().setId(secondId.toString()).clearMuseumId())
        .build());
    stubReferences();

    var result = client.getAllPaintings(0, 10, null);

    assertThat(result.content()).as("painting order")
        .extracting(PaintingInfoResponse::id).containsExactly(id, secondId);
    assertThat(result.content()).as("artists")
        .extracting(PaintingInfoResponse::artist).containsExactly(artist, artist);
    assertThat(result.content()).as("optional museums")
        .extracting(PaintingInfoResponse::museum).containsExactly(museum, null);
    verify(artistClient).getArtistsByIds(Set.of(artistId));
    verify(museumClient).getMuseumsByIds(Set.of(museumId));
  }

  @Test
  void getPaintingsByArtist_mapsRequestAndEnrichesPage() {
    when(paintingStub.getPaintingsByArtist(PaintingByArtistRequest.newBuilder()
        .setArtistId(artistId.toString()).setPage(1).setSize(2).build())).thenReturn(page(painting()));
    stubReferences();

    assertThat(client.getPaintingsByArtist(artistId, 1, 2)).as("artist paintings")
        .isEqualTo(new PageResponse<>(List.of(model(museum)), 3, 2, 1, 2, true, false, 1));
  }

  @Test
  void getPaintingById_existingId_enrichesReferences() {
    when(paintingStub.getPaintingById(PaintingByIdRequest.newBuilder().setId(id.toString()).build()))
        .thenReturn(painting());
    stubReferences();

    assertThat(client.getPaintingById(id)).as("painting").isEqualTo(model(museum));
  }

  @Test
  void getPaintingById_withoutMuseum_returnsNullMuseum() {
    when(paintingStub.getPaintingById(any())).thenReturn(painting().toBuilder().clearMuseumId().build());
    when(artistClient.getArtistsByIds(Set.of(artistId))).thenReturn(Map.of(artistId, artist));
    when(museumClient.getMuseumsByIds(Set.of())).thenReturn(Map.of());

    assertThat(client.getPaintingById(id)).as("painting without museum").isEqualTo(model(null));
    verify(museumClient).getMuseumsByIds(Set.of());
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void createPainting_optionalMuseum_mapsRequestAndResponse(boolean withMuseum) {
    var expected = CreatePaintingRequest.newBuilder().setTitle("Water lilies")
        .setDescription("Painting description").setContent("image").setArtistId(artistId.toString());
    var response = painting().toBuilder();
    when(artistClient.getArtistsByIds(Set.of(artistId))).thenReturn(Map.of(artistId, artist));
    if (withMuseum) {
      expected.setMuseumId(museumId.toString());
      when(museumClient.getMuseumsByIds(Set.of(museumId))).thenReturn(Map.of(museumId, museum));
    } else {
      response.clearMuseumId();
      when(museumClient.getMuseumsByIds(Set.of())).thenReturn(Map.of());
    }
    when(paintingStub.createPainting(expected.build())).thenReturn(response.build());

    var result = client.createPainting(new CreatePaintingInfoRequest("Water lilies", "Painting description",
        "image", new ArtistInfoRequest(artistId), withMuseum ? new MuseumInfoRequest(museumId) : null));

    assertThat(result).as("created painting").isEqualTo(model(withMuseum ? museum : null));
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void updatePainting_optionalMuseum_mapsRequestAndResponse(boolean withMuseum) {
    var expected = UpdatePaintingRequest.newBuilder().setId(id.toString()).setTitle("Water lilies")
        .setDescription("Painting description").setContent("image").setArtistId(artistId.toString());
    var response = painting().toBuilder();
    when(artistClient.getArtistsByIds(Set.of(artistId))).thenReturn(Map.of(artistId, artist));
    if (withMuseum) {
      expected.setMuseumId(museumId.toString());
      when(museumClient.getMuseumsByIds(Set.of(museumId))).thenReturn(Map.of(museumId, museum));
    } else {
      response.clearMuseumId();
      when(museumClient.getMuseumsByIds(Set.of())).thenReturn(Map.of());
    }
    when(paintingStub.updatePainting(expected.build())).thenReturn(response.build());

    var result = client.updatePainting(new UpdatePaintingInfoRequest(id, "Water lilies", "Painting description",
        "image", new ArtistInfoRequest(artistId), withMuseum ? new MuseumInfoRequest(museumId) : null));

    assertThat(result).as("updated painting").isEqualTo(model(withMuseum ? museum : null));
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void createPainting_missingReference_doesNotCreatePainting(boolean missingArtist) {
    var error = Status.INVALID_ARGUMENT.withDescription("Missing reference").asRuntimeException();
    if (missingArtist) {
      when(artistClient.getArtistsByIds(Set.of(artistId))).thenThrow(error);
    } else {
      when(museumClient.getMuseumsByIds(Set.of(museumId))).thenThrow(error);
    }

    assertThatThrownBy(() -> client.createPainting(new CreatePaintingInfoRequest("Water lilies",
        "Painting description", "image", new ArtistInfoRequest(artistId), new MuseumInfoRequest(museumId))))
        .as("reference failure").isSameAs(error);
    verifyNoInteractions(paintingStub);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void updatePainting_missingReference_doesNotUpdatePainting(boolean missingArtist) {
    var error = Status.INVALID_ARGUMENT.withDescription("Missing reference").asRuntimeException();
    if (missingArtist) {
      when(artistClient.getArtistsByIds(Set.of(artistId))).thenThrow(error);
    } else {
      when(museumClient.getMuseumsByIds(Set.of(museumId))).thenThrow(error);
    }

    assertThatThrownBy(() -> client.updatePainting(new UpdatePaintingInfoRequest(id, "Water lilies",
        "Painting description", "image", new ArtistInfoRequest(artistId), new MuseumInfoRequest(museumId))))
        .as("reference failure").isSameAs(error);
    verifyNoInteractions(paintingStub);
  }

  @Test
  void getPaintingById_enrichmentGrpcFailure_unwrapsException() {
    var error = Status.UNAVAILABLE.withDescription("Artist unavailable").asRuntimeException();
    when(paintingStub.getPaintingById(any())).thenReturn(painting());
    when(artistClient.getArtistsByIds(Set.of(artistId))).thenThrow(error);

    assertThatThrownBy(() -> client.getPaintingById(id)).as("enrichment RPC failure").isSameAs(error);
  }

  @Test
  void getAllPaintings_enrichmentGrpcFailure_unwrapsException() {
    var error = Status.UNAVAILABLE.asRuntimeException();
    when(paintingStub.getAllPaintings(any())).thenReturn(page(painting()));
    when(museumClient.getMuseumsByIds(Set.of(museumId))).thenThrow(error);

    assertThatThrownBy(() -> client.getAllPaintings(1, 2, null)).as("page enrichment failure").isSameAs(error);
  }

  @Test
  void getPaintingById_unexpectedEnrichmentFailure_preservesCause() {
    var error = new IllegalStateException("Unexpected error");
    when(paintingStub.getPaintingById(any())).thenReturn(painting());
    when(artistClient.getArtistsByIds(Set.of(artistId))).thenThrow(error);

    assertThatThrownBy(() -> client.getPaintingById(id)).as("unexpected enrichment failure")
        .isInstanceOf(CompletionException.class).hasCause(error);
  }

  private void stubReferences() {
    when(artistClient.getArtistsByIds(Set.of(artistId))).thenReturn(Map.of(artistId, artist));
    when(museumClient.getMuseumsByIds(Set.of(museumId))).thenReturn(Map.of(museumId, museum));
  }

  private PaintingResponse painting() {
    return PaintingResponse.newBuilder().setId(id.toString()).setTitle("Water lilies")
        .setDescription("Painting description").setContent("image").setArtistId(artistId.toString())
        .setMuseumId(museumId.toString()).build();
  }

  private PaintingInfoResponse model(MuseumInfoResponse expectedMuseum) {
    return new PaintingInfoResponse(id, "Water lilies", "Painting description", "image", artist, expectedMuseum);
  }

  private PaintingPageResponse page(PaintingResponse painting) {
    return PaintingPageResponse.newBuilder().addContent(painting).setTotalElements(3).setTotalPages(2)
        .setPageNumber(1).setPageSize(2).setLast(true).setFirst(false).setNumberOfElements(1).build();
  }
}
