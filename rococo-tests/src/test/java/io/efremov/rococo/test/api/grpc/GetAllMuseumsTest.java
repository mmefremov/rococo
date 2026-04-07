package io.efremov.rococo.test.api.grpc;

import static io.efremov.rococo.config.Constants.INFO_API_TAG;

import io.efremov.rococo.api.validation.GrpcValidation;
import io.efremov.rococo.data.repository.MuseumRepository;
import io.efremov.rococo.grpc.MuseumPageRequest;
import io.efremov.rococo.jupiter.annotation.AnyMuseum;
import io.efremov.rococo.model.MuseumInfoResponse;
import io.grpc.Status;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(INFO_API_TAG)
@Epic("API")
@Feature("rococo-museum")
@Story("Get all museums")
class GetAllMuseumsTest extends BaseGrpcTest {

  private static final int DEFAULT_SIZE = 4;
  private static final MuseumPageRequest prototype = MuseumPageRequest.newBuilder()
      .setSize(DEFAULT_SIZE)
      .build();

  @Test
  @DisplayName("Get all museums")
  void positiveGetAllMuseumsTest() {
    var response = MUSEUM_BLOCKING_STUB.getAllMuseums(prototype);

    GrpcValidation.checkPageResponse(prototype, response);
  }

  @Test
  @AnyMuseum
  @DisplayName("Get all museums by title")
  void positiveGetAllMuseumsByTitleTest(MuseumInfoResponse museum) {
    var request = MuseumPageRequest.newBuilder(prototype)
        .setTitle(museum.title())
        .build();
    var response = MUSEUM_BLOCKING_STUB.getAllMuseums(request);

    GrpcValidation.checkPageResponse(request, response);
  }

  @Test
  @DisplayName("Get all museums with last page")
  void positiveGetAllMuseumsWithLastPageTest() {
    int count = Math.toIntExact(new MuseumRepository().countAllByTitle(null));
    var request = MuseumPageRequest.newBuilder(prototype)
        .setPage(count / DEFAULT_SIZE)
        .build();
    var response = MUSEUM_BLOCKING_STUB.getAllMuseums(request);

    GrpcValidation.checkPageResponse(request, response);
  }

  @Test
  @DisplayName("Attempt to get museums with negative page")
  void negativeGetAllMuseumsWithNegativePageTest() {
    var request = MuseumPageRequest.newBuilder(prototype)
        .setPage(-1)
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> MUSEUM_BLOCKING_STUB.getAllMuseums(request),
        Status.Code.INVALID_ARGUMENT,
        "Page index must not be less than zero"
    );
  }

  @Test
  @DisplayName("Attempt to get museums with zero size")
  void negativeGetAllMuseumsWithZeroSizeTest() {
    var request = MuseumPageRequest.newBuilder(prototype)
        .setSize(0)
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> MUSEUM_BLOCKING_STUB.getAllMuseums(request),
        Status.Code.INVALID_ARGUMENT,
        "Page size must not be less than one"
    );
  }
}
