package io.efremov.rococo.test.api.grpc;

import static io.efremov.rococo.config.Constants.INFO_API_TAG;

import io.efremov.rococo.api.validation.GrpcValidation;
import io.efremov.rococo.data.repository.PaintingRepository;
import io.efremov.rococo.grpc.PaintingPageRequest;
import io.efremov.rococo.jupiter.annotation.AnyPainting;
import io.efremov.rococo.model.PaintingInfoResponse;
import io.grpc.Status;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(INFO_API_TAG)
@Epic("API")
@Feature("rococo-painting")
@Story("Get all paintings")
class GetAllPaintingsTest extends BaseGrpcTest {

  private static final int DEFAULT_SIZE = 9;
  private static final PaintingPageRequest prototype = PaintingPageRequest.newBuilder()
      .setSize(DEFAULT_SIZE)
      .build();

  @Test
  @DisplayName("Get all paintings")
  void positiveGetAllPaintingsTest() {
    var response = PAINTING_BLOCKING_STUB.getAllPaintings(prototype);

    GrpcValidation.checkPageResponse(prototype, response);
  }

  @Test
  @AnyPainting
  @DisplayName("Get all paintings by title")
  void positiveGetAllPaintingsByTitleTest(PaintingInfoResponse painting) {
    var request = PaintingPageRequest.newBuilder(prototype)
        .setTitle(painting.title())
        .build();
    var response = PAINTING_BLOCKING_STUB.getAllPaintings(request);

    GrpcValidation.checkPageResponse(request, response);
  }

  @Test
  @DisplayName("Get all paintings with last page")
  void positiveGetAllPaintingsWithLastPageTest() {
    int count = Math.toIntExact(new PaintingRepository().countAllByTitle(null));
    var request = PaintingPageRequest.newBuilder(prototype)
        .setPage(count / DEFAULT_SIZE)
        .build();
    var response = PAINTING_BLOCKING_STUB.getAllPaintings(request);

    GrpcValidation.checkPageResponse(request, response);
  }

  @Test
  @DisplayName("Attempt to get paintings with negative page")
  void negativeGetAllPaintingsWithNegativePageTest() {
    var request = PaintingPageRequest.newBuilder(prototype)
        .setPage(-1)
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> PAINTING_BLOCKING_STUB.getAllPaintings(request),
        Status.Code.INVALID_ARGUMENT,
        "Page index must not be less than zero"
    );
  }

  @Test
  @DisplayName("Attempt to get paintings with zero size")
  void negativeGetAllPaintingsWithZeroSizeTest() {
    var request = PaintingPageRequest.newBuilder(prototype)
        .setSize(0)
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> PAINTING_BLOCKING_STUB.getAllPaintings(request),
        Status.Code.INVALID_ARGUMENT,
        "Page size must not be less than one"
    );
  }
}
