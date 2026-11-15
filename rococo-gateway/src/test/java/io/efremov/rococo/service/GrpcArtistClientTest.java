package io.efremov.rococo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.efremov.rococo.grpc.ArtistByIdRequest;
import io.efremov.rococo.grpc.ArtistPageRequest;
import io.efremov.rococo.grpc.ArtistPageResponse;
import io.efremov.rococo.grpc.ArtistResponse;
import io.efremov.rococo.grpc.ArtistServiceGrpc;
import io.efremov.rococo.grpc.ArtistsResponse;
import io.efremov.rococo.grpc.CreateArtistRequest;
import io.efremov.rococo.grpc.UpdateArtistRequest;
import io.efremov.rococo.model.ArtistInfoResponse;
import io.efremov.rococo.model.CreateArtistInfoRequest;
import io.efremov.rococo.model.PageResponse;
import io.efremov.rococo.model.UpdateArtistInfoRequest;
import io.grpc.Status;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GrpcArtistClientTest {

  @Mock
  private ArtistServiceGrpc.ArtistServiceBlockingStub artistStub;
  @InjectMocks
  private GrpcArtistClient client;

  private final UUID id = UUID.randomUUID();

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "Monet"})
  void getAllArtists_withFilter_mapsPageAndRequest(String name) {
    when(artistStub.getAllArtists(any())).thenReturn(ArtistPageResponse.newBuilder()
        .addContent(artist()).setTotalElements(5).setTotalPages(3).setPageNumber(1)
        .setPageSize(2).setFirst(false).setLast(false).setNumberOfElements(1).build());

    var result = client.getAllArtists(1, 2, name);

    assertThat(result).as("artist page").isEqualTo(
        new PageResponse<>(List.of(model()), 5, 3, 1, 2, false, false, 1));
    var expected = ArtistPageRequest.newBuilder().setPage(1).setSize(2);
    if (name != null && !name.isBlank()) {
      expected.setName("Monet");
    }
    verify(artistStub).getAllArtists(expected.build());
  }

  @Test
  void getArtistById_existingId_returnsArtist() {
    when(artistStub.getArtistById(ArtistByIdRequest.newBuilder().addId(id.toString()).build()))
        .thenReturn(ArtistsResponse.newBuilder().addArtists(artist()).build());

    assertThat(client.getArtistById(id)).as("artist").isEqualTo(model());
  }

  @Test
  void getArtistById_missingId_throwsNotFound() {
    when(artistStub.getArtistById(any())).thenReturn(ArtistsResponse.getDefaultInstance());

    assertThatThrownBy(() -> client.getArtistById(id)).as("missing artist")
        .isInstanceOf(io.grpc.StatusRuntimeException.class)
        .hasMessage("NOT_FOUND: Artist not found: " + id);
  }

  @Test
  void getArtistsByIds_emptyIds_skipsRpc() {
    assertThat(client.getArtistsByIds(List.of())).as("artists").isEmpty();
    verifyNoInteractions(artistStub);
  }

  @Test
  void getArtistsByIds_existingIds_returnsMap() {
    when(artistStub.getArtistById(ArtistByIdRequest.newBuilder().addId(id.toString()).build()))
        .thenReturn(ArtistsResponse.newBuilder().addArtists(artist()).build());

    assertThat(client.getArtistsByIds(List.of(id))).as("artists").isEqualTo(Map.of(id, model()));
  }

  @Test
  void getArtistsByIds_partiallyMissingIds_throwsInvalidArgument() {
    UUID missing = UUID.randomUUID();
    when(artistStub.getArtistById(any()))
        .thenReturn(ArtistsResponse.newBuilder().addArtists(artist()).build());

    assertThatThrownBy(() -> client.getArtistsByIds(List.of(id, missing))).as("missing reference")
        .hasMessage("INVALID_ARGUMENT: Artist not found: " + missing);
  }

  @Test
  void createArtist_validRequest_mapsRequestAndResponse() {
    when(artistStub.createArtist(CreateArtistRequest.newBuilder().setName("Monet")
        .setBiography("French painter").setPhoto("photo").build())).thenReturn(artist());

    assertThat(client.createArtist(new CreateArtistInfoRequest("Monet", "French painter", "photo")))
        .as("created artist").isEqualTo(model());
  }

  @Test
  void updateArtist_validRequest_mapsRequestAndResponse() {
    when(artistStub.updateArtist(UpdateArtistRequest.newBuilder().setId(id.toString()).setName("Monet")
        .setBiography("French painter").setPhoto("photo").build())).thenReturn(artist());

    assertThat(client.updateArtist(new UpdateArtistInfoRequest(id, "Monet", "French painter", "photo")))
        .as("updated artist").isEqualTo(model());
  }

  @Test
  void createArtist_rpcFailure_propagatesException() {
    var error = Status.ALREADY_EXISTS.withDescription("duplicate").asRuntimeException();
    when(artistStub.createArtist(any())).thenThrow(error);

    assertThatThrownBy(() -> client.createArtist(new CreateArtistInfoRequest("Monet", "bio", "photo")))
        .as("RPC failure").isSameAs(error);
  }

  private ArtistResponse artist() {
    return ArtistResponse.newBuilder().setId(id.toString()).setName("Monet")
        .setBiography("French painter").setPhoto("photo").build();
  }

  private ArtistInfoResponse model() {
    return new ArtistInfoResponse(id, "Monet", "French painter", "photo");
  }
}
