package io.efremov.rococo.test.api.rest;

import static io.efremov.rococo.config.Constants.INFO_API_TAG;

import io.efremov.rococo.api.validation.RestValidation;
import io.efremov.rococo.data.repository.PaintingRepository;
import io.efremov.rococo.provider.PaintingProvider;
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
@Story("Get paintings by artist")
class GetPaintingsByArtistTest {

  private final GatewayApiClient client = new GatewayApiClient();
  private final UUID artistId = PaintingProvider.getAnyPainting().artist().id();
  private final int DEFAULT_PAGE = 0;
  private final int DEFAULT_SIZE = 9;

  @Test
  @DisplayName("Get paintings by artist")
  void positiveGetPaintingsByArtistTest() {
    var response = client.getPaintingsByArtist(artistId, DEFAULT_PAGE, DEFAULT_SIZE);

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    RestValidation.checkPaintingsPageResponse(response);
  }

  @Test
  @DisplayName("Get paintings by artist with last page")
  void positiveGetPaintingsByArtistWithLastPageTest() {
    int count = Math.toIntExact(new PaintingRepository().countAllByArtistId(artistId));
    var response = client.getPaintingsByArtist(artistId, count / DEFAULT_SIZE, DEFAULT_SIZE);

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    RestValidation.checkPaintingsPageResponse(response);
  }

  @Test
  @DisplayName("Attempt to get paintings by artist with no paintings")
  void positiveGetPaintingsByArtistWithNoPaintingsTest() {
    var response = client.getPaintingsByArtist(UUID.randomUUID(), DEFAULT_PAGE, DEFAULT_SIZE);

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    RestValidation.checkPaintingsPageResponse(response);
  }

  @Test
  @DisplayName("Attempt to get paintings by artist with negative page")
  void negativeGetPaintingsByArtistWithNegativePageTest() {
    var response = client.getPaintingsByArtist(artistId, -1, DEFAULT_SIZE);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHavePageIndexLessThanZeroError(response);
  }

  @Test
  @DisplayName("Attempt to get paintings by artist with zero size")
  void negativeGetPaintingsByArtistWithZeroSizeTest() {
    var response = client.getPaintingsByArtist(artistId, DEFAULT_PAGE, 0);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHavePageSizeLessThanOneError(response);
  }
}
