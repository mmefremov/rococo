package io.efremov.rococo.test.api.rest;

import static io.efremov.rococo.config.Constants.INFO_API_TAG;

import io.efremov.rococo.api.validation.RestValidation;
import io.efremov.rococo.jupiter.annotation.AnyArtist;
import io.efremov.rococo.model.ArtistInfoResponse;
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
@Story("Get artist by id")
class GetArtistByIdTest {

  private final GatewayApiClient client = new GatewayApiClient();

  @Test
  @AnyArtist
  @DisplayName("Get artist by id")
  void positiveGetArtistByIdTest(ArtistInfoResponse artist) {
    var response = client.getArtistById(artist.id());

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    RestValidation.checkResponse(response.body(), artist.id());
  }

  @Test
  @DisplayName("Attempt to get artist by id with non-existent id")
  void negativeGetArtistByIdWithNonExistentIdTest() {
    UUID id = UUID.randomUUID();
    var response = client.getArtistById(id);

    RestValidation.responseMustHaveNotFoundStatus(response);
    String error = "Artist not found: %s".formatted(id);
    RestValidation.bodyMustHaveTheError(response, error);
  }
}
