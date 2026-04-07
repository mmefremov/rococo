package io.efremov.rococo.test.api.grpc;

import static io.efremov.rococo.config.Constants.INFO_API_TAG;

import io.efremov.rococo.api.validation.GrpcValidation;
import io.efremov.rococo.data.repository.PaintingRepository;
import io.efremov.rococo.grpc.PaintingByArtistRequest;
import io.efremov.rococo.jupiter.annotation.AnyPainting;
import io.efremov.rococo.model.PaintingInfoResponse;
import io.grpc.Status;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(INFO_API_TAG)
@Epic("API")
@Feature("rococo-painting")
@Story("Get paintings by artist")
class GetPaintingsByArtistTest extends BaseGrpcTest {

  private static final int DEFAULT_SIZE = 9;

  @Test
  @AnyPainting
  @DisplayName("Get paintings by artist")
  void positiveGetPaintingsByArtistTest(PaintingInfoResponse painting) {
    var request = PaintingByArtistRequest.newBuilder()
        .setArtistId(painting.artist().id().toString())
        .setSize(DEFAULT_SIZE)
        .build();
    var response = PAINTING_BLOCKING_STUB.getPaintingsByArtist(request);

    GrpcValidation.checkPageResponse(request, response);
  }

  @Test
  @AnyPainting
  @DisplayName("Get paintings by artist with last page")
  void positiveGetPaintingsByArtistWithLastPageTest(PaintingInfoResponse painting) {
    String artistId = painting.artist().id().toString();
    int count = Math.toIntExact(new PaintingRepository().countAllByArtistId(UUID.fromString(artistId)));
    var request = PaintingByArtistRequest.newBuilder()
        .setArtistId(artistId)
        .setSize(DEFAULT_SIZE)
        .setPage(count / DEFAULT_SIZE)
        .build();
    var response = PAINTING_BLOCKING_STUB.getPaintingsByArtist(request);

    GrpcValidation.checkPageResponse(request, response);
  }

  @Test
  @DisplayName("Get paintings by non-existent artist")
  void positiveGetPaintingsByNonExistentArtistTest() {
    var request = PaintingByArtistRequest.newBuilder()
        .setArtistId(UUID.randomUUID().toString())
        .setSize(DEFAULT_SIZE)
        .build();
    var response = PAINTING_BLOCKING_STUB.getPaintingsByArtist(request);

    GrpcValidation.checkPageResponse(request, response);
  }

  @Test
  @AnyPainting
  @DisplayName("Attempt to get paintings by artist with negative page")
  void negativeGetPaintingsByArtistWithNegativePageTest(PaintingInfoResponse painting) {
    var request = PaintingByArtistRequest.newBuilder()
        .setArtistId(painting.artist().id().toString())
        .setSize(DEFAULT_SIZE)
        .setPage(-1)
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> PAINTING_BLOCKING_STUB.getPaintingsByArtist(request),
        Status.Code.INVALID_ARGUMENT,
        "Page index must not be less than zero"
    );
  }

  @Test
  @AnyPainting
  @DisplayName("Attempt to get paintings by artist with zero size")
  void negativeGetPaintingsByArtistWithZeroSizeTest(PaintingInfoResponse painting) {
    var request = PaintingByArtistRequest.newBuilder()
        .setArtistId(painting.artist().id().toString())
        .setSize(0)
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> PAINTING_BLOCKING_STUB.getPaintingsByArtist(request),
        Status.Code.INVALID_ARGUMENT,
        "Page size must not be less than one"
    );
  }
}
