package io.efremov.rococo.test.api.rest;

import static io.efremov.rococo.config.Constants.INFO_API_TAG;

import io.efremov.rococo.api.validation.RestValidation;
import io.efremov.rococo.jupiter.annotation.AnyPainting;
import io.efremov.rococo.model.PaintingInfoResponse;
import io.efremov.rococo.service.GatewayApiClient;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(INFO_API_TAG)
@Epic("API")
@Feature("rococo-gateway")
@Story("Get painting by id")
class GetPaintingByIdTest {

  private final GatewayApiClient client = new GatewayApiClient();

  @Test
  @AnyPainting
  @DisplayName("Get painting by id")
  void positiveGetPaintingByIdTest(PaintingInfoResponse painting) {
    var response = client.getPaintingById(painting.id());

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    RestValidation.checkResponse(response.body(), painting.id());
  }

  @Test
  @DisplayName("Attempt to get painting by id with non-existent id")
  void negativeGetPaintingByIdWithNonExistentIdTest() {
    UUID id = UUID.randomUUID();
    var response = client.getPaintingById(id);

    RestValidation.responseMustHaveNotFoundStatus(response);
    String error = "Painting not found: %s".formatted(id);
    RestValidation.bodyMustHaveTheError(response, error);
  }
}
