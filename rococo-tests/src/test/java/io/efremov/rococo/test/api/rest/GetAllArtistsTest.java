package io.efremov.rococo.test.api.rest;

import static io.efremov.rococo.config.Constants.INFO_API_TAG;

import io.efremov.rococo.api.validation.RestValidation;
import io.efremov.rococo.data.repository.ArtistRepository;
import io.efremov.rococo.jupiter.annotation.AnyArtist;
import io.efremov.rococo.model.ArtistInfoResponse;
import io.efremov.rococo.service.GatewayApiClient;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(INFO_API_TAG)
@Epic("API")
@Feature("rococo-gateway")
@Story("Get all artists")
class GetAllArtistsTest {

  private final GatewayApiClient client = new GatewayApiClient();
  private final int DEFAULT_PAGE = 0;
  private final int DEFAULT_SIZE = 18;

  @Test
  @DisplayName("Get all artists without name")
  void positiveGetAllArtistsWithoutNameTest() {
    var response = client.getAllArtists(DEFAULT_PAGE, DEFAULT_SIZE);

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    RestValidation.checkArtistsPageResponse(response);
  }

  @Test
  @AnyArtist
  @DisplayName("Get all artists with name")
  void positiveGetAllArtistsWithNameTest(ArtistInfoResponse artist) {
    var response = client.getAllArtists(DEFAULT_PAGE, DEFAULT_SIZE, artist.name());

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    RestValidation.checkArtistsPageResponse(response);
  }

  @Test
  @DisplayName("Get all artists with last page")
  void positiveGetAllArtistsWithLastPageTest() {
    int count = Math.toIntExact(new ArtistRepository().countAllByName(null));
    var response = client.getAllArtists(count / DEFAULT_SIZE, DEFAULT_SIZE);

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    RestValidation.checkArtistsPageResponse(response);
  }

  @Test
  @DisplayName("Attempt to get all artists with negative page")
  void negativeGetAllArtistsWithNegativePageTest() {
    var response = client.getAllArtists(-1, DEFAULT_SIZE);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHavePageIndexLessThanZeroError(response);
  }

  @Test
  @DisplayName("Attempt to get all artists with zero size")
  void negativeGetAllArtistsWithZeroSizeTest() {
    var response = client.getAllArtists(DEFAULT_PAGE, 0);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHavePageSizeLessThanOneError(response);
  }
}
