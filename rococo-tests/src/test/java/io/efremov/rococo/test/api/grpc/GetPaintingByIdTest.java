package io.efremov.rococo.test.api.grpc;

import static io.efremov.rococo.config.Constants.INFO_API_TAG;

import io.efremov.rococo.api.validation.GrpcValidation;
import io.efremov.rococo.grpc.PaintingByIdRequest;
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
@Story("Get painting by id")
class GetPaintingByIdTest extends BaseGrpcTest {

  @Test
  @AnyPainting
  @DisplayName("Get painting by id")
  void positiveGetPaintingByIdTest(PaintingInfoResponse painting) {
    var request = PaintingByIdRequest.newBuilder()
        .setId(painting.id().toString())
        .build();
    var response = PAINTING_BLOCKING_STUB.getPaintingById(request);

    GrpcValidation.checkEntity(request, response);
  }

  @Test
  @DisplayName("Attempt to get painting by id with non-existent id")
  void negativeGetPaintingByIdWithNonExistentIdTest() {
    String nonExistentId = UUID.randomUUID().toString();
    var request = PaintingByIdRequest.newBuilder()
        .setId(nonExistentId)
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> PAINTING_BLOCKING_STUB.getPaintingById(request),
        Status.Code.NOT_FOUND,
        "Painting not found: %s".formatted(nonExistentId)
    );
  }
}
