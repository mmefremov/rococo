package io.efremov.rococo.test.api.grpc;

import static io.efremov.rococo.config.Constants.INFO_API_TAG;

import io.efremov.rococo.api.validation.GrpcValidation;
import io.efremov.rococo.grpc.MuseumByIdRequest;
import io.efremov.rococo.jupiter.annotation.AnyMuseum;
import io.efremov.rococo.model.MuseumInfoResponse;
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
@Feature("rococo-museum")
@Story("Get museum by id")
class GetMuseumByIdTest extends BaseGrpcTest {

  @Test
  @AnyMuseum
  @DisplayName("Get museum by id")
  void positiveGetMuseumByIdTest(MuseumInfoResponse museum) {
    var request = MuseumByIdRequest.newBuilder()
        .setId(museum.id().toString())
        .build();
    var response = MUSEUM_BLOCKING_STUB.getMuseumById(request);

    GrpcValidation.checkEntity(request, response);
  }

  @Test
  @DisplayName("Attempt to get museum by id with non-existent id")
  void negativeGetMuseumByIdWithNonExistentIdTest() {
    String nonExistentId = UUID.randomUUID().toString();
    var request = MuseumByIdRequest.newBuilder()
        .setId(nonExistentId)
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> MUSEUM_BLOCKING_STUB.getMuseumById(request),
        Status.Code.NOT_FOUND,
        "Museum not found: %s".formatted(nonExistentId)
    );
  }
}
