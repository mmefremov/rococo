package io.efremov.rococo.test.api.grpc;

import static io.efremov.rococo.config.Constants.INFO_API_TAG;

import io.efremov.rococo.api.validation.GrpcValidation;
import io.efremov.rococo.data.repository.ArtistRepository;
import io.efremov.rococo.grpc.ArtistPageRequest;
import io.efremov.rococo.jupiter.annotation.AnyArtist;
import io.efremov.rococo.model.ArtistInfoResponse;
import io.grpc.Status;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(INFO_API_TAG)
@Epic("API")
@Feature("rococo-artist")
@Story("Get all artists")
class GetAllArtistsTest extends BaseGrpcTest {

  private static final int DEFAULT_SIZE = 18;
  private static final ArtistPageRequest prototype = ArtistPageRequest.newBuilder()
      .setSize(DEFAULT_SIZE)
      .build();

  @Test
  @DisplayName("Get all artists")
  void positiveGetAllArtistsTest() {
    var response = ARTIST_BLOCKING_STUB.getAllArtists(prototype);

    GrpcValidation.checkPageResponse(prototype, response);
  }

  @Test
  @AnyArtist
  @DisplayName("Get all artists by name")
  void positiveGetAllArtistsByNameTest(ArtistInfoResponse artist) {
    var request = ArtistPageRequest.newBuilder(prototype)
        .setName(artist.name())
        .build();
    var response = ARTIST_BLOCKING_STUB.getAllArtists(request);

    GrpcValidation.checkPageResponse(request, response);
  }

  @Test
  @DisplayName("Get all artists with last page")
  void positiveGetAllArtistsWithLastPageTest() {
    int count = Math.toIntExact(new ArtistRepository().countAllByName(null));
    var request = ArtistPageRequest.newBuilder(prototype)
        .setPage(count / DEFAULT_SIZE)
        .build();
    var response = ARTIST_BLOCKING_STUB.getAllArtists(request);

    GrpcValidation.checkPageResponse(request, response);
  }

  @Test
  @DisplayName("Attempt to get all artists with negative page")
  void negativeGetAllArtistsWithNegativePageTest() {
    var request = ArtistPageRequest.newBuilder(prototype)
        .setPage(-1)
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> ARTIST_BLOCKING_STUB.getAllArtists(request),
        Status.Code.INVALID_ARGUMENT,
        "Page index must not be less than zero"
    );
  }

  @Test
  @DisplayName("Attempt to get all artists with empty size")
  void negativeGetAllArtistsWithEmptySizeTest() {
    var request = ArtistPageRequest.newBuilder(prototype)
        .clearSize()
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> ARTIST_BLOCKING_STUB.getAllArtists(request),
        Status.Code.INVALID_ARGUMENT,
        "Page size must not be less than one"
    );
  }

  @Test
  @DisplayName("Attempt to get all artists with zero size")
  void negativeGetAllArtistsWithZeroSizeTest() {
    var request = ArtistPageRequest.newBuilder(prototype)
        .setSize(0)
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> ARTIST_BLOCKING_STUB.getAllArtists(request),
        Status.Code.INVALID_ARGUMENT,
        "Page size must not be less than one"
    );
  }
}
