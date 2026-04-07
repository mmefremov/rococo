package io.efremov.rococo.test.api.rest;

import static io.efremov.rococo.config.Constants.INFO_API_TAG;

import io.efremov.rococo.api.validation.RestValidation;
import io.efremov.rococo.data.repository.PaintingRepository;
import io.efremov.rococo.jupiter.annotation.AnyPainting;
import io.efremov.rococo.model.PaintingInfoResponse;
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
@Story("Get all paintings")
class GetAllPaintingsTest {

  private final GatewayApiClient client = new GatewayApiClient();
  private final int DEFAULT_PAGE = 0;
  private final int DEFAULT_SIZE = 9;

  @Test
  @DisplayName("Get all paintings without title")
  void positiveGetAllPaintingsWithoutTitleTest() {
    var response = client.getAllPaintings(DEFAULT_PAGE, DEFAULT_SIZE);

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    RestValidation.checkPaintingsPageResponse(response);
  }

  @Test
  @AnyPainting
  @DisplayName("Get all paintings with title")
  void positiveGetAllPaintingsWithTitleTest(PaintingInfoResponse painting) {
    var response = client.getAllPaintings(DEFAULT_PAGE, DEFAULT_SIZE, painting.title());

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    RestValidation.checkPaintingsPageResponse(response);
  }

  @Test
  @DisplayName("Get all paintings with last page")
  void positiveGetAllPaintingsWithLastPageTest() {
    int count = Math.toIntExact(new PaintingRepository().countAllByTitle(null));
    var response = client.getAllPaintings(count / DEFAULT_SIZE, DEFAULT_SIZE);

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    RestValidation.checkPaintingsPageResponse(response);
  }

  @Test
  @DisplayName("Attempt to get all paintings with negative page")
  void negativeGetAllPaintingsWithNegativePageTest() {
    var response = client.getAllPaintings(-1, DEFAULT_SIZE);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHavePageIndexLessThanZeroError(response);
  }

  @Test
  @DisplayName("Attempt to get all paintings with zero size")
  void negativeGetAllPaintingsWithZeroSizeTest() {
    var response = client.getAllPaintings(DEFAULT_PAGE, 0);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHavePageSizeLessThanOneError(response);
  }
}
