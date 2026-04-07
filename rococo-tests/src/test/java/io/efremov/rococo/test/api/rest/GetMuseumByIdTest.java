package io.efremov.rococo.test.api.rest;

import static io.efremov.rococo.config.Constants.INFO_API_TAG;

import io.efremov.rococo.api.validation.RestValidation;
import io.efremov.rococo.jupiter.annotation.AnyMuseum;
import io.efremov.rococo.model.MuseumInfoResponse;
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
@Story("Get museum by id")
class GetMuseumByIdTest {

  private final GatewayApiClient client = new GatewayApiClient();

  @Test
  @AnyMuseum
  @DisplayName("Get museum by id")
  void positiveGetMuseumByIdTest(MuseumInfoResponse museum) {
    var response = client.getMuseumById(museum.id());

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    RestValidation.checkResponse(response.body(), museum.id());
  }

  @Test
  @DisplayName("Attempt to get museum by id with non-existent id")
  void negativeGetMuseumByIdWithNonExistentIdTest() {
    UUID id = UUID.randomUUID();
    var response = client.getMuseumById(id);

    RestValidation.responseMustHaveNotFoundStatus(response);
    String error = "Museum not found: %s".formatted(id);
    RestValidation.bodyMustHaveTheError(response, error);
  }
}
