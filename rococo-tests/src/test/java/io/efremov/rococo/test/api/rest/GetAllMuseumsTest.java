package io.efremov.rococo.test.api.rest;

import static io.efremov.rococo.config.Constants.INFO_API_TAG;

import io.efremov.rococo.api.validation.RestValidation;
import io.efremov.rococo.data.repository.MuseumRepository;
import io.efremov.rococo.jupiter.annotation.AnyMuseum;
import io.efremov.rococo.model.MuseumInfoResponse;
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
@Story("Get all museums")
class GetAllMuseumsTest {

  private final GatewayApiClient client = new GatewayApiClient();
  private final int DEFAULT_PAGE = 0;
  private final int DEFAULT_SIZE = 4;

  @Test
  @DisplayName("Get all museums without title")
  void positiveGetAllMuseumsWithoutTitleTest() {
    var response = client.getAllMuseums(DEFAULT_PAGE, DEFAULT_SIZE);

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    RestValidation.checkMuseumsPageResponse(response);
  }

  @Test
  @AnyMuseum
  @DisplayName("Get all museums with title")
  void positiveGetAllMuseumsWithTitleTest(MuseumInfoResponse museum) {
    var response = client.getAllMuseums(DEFAULT_PAGE, DEFAULT_SIZE, museum.title());

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    RestValidation.checkMuseumsPageResponse(response);
  }

  @Test
  @DisplayName("Get all museums with last page")
  void positiveGetAllMuseumsWithLastPageTest() {
    int count = Math.toIntExact(new MuseumRepository().countAllByTitle(null));
    var response = client.getAllMuseums(count / DEFAULT_SIZE, DEFAULT_SIZE);

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    RestValidation.checkMuseumsPageResponse(response);
  }

  @Test
  @DisplayName("Attempt to get all museums with negative page")
  void negativeGetAllMuseumsWithNegativePageTest() {
    var response = client.getAllMuseums(-1, DEFAULT_SIZE);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHavePageIndexLessThanZeroError(response);
  }

  @Test
  @DisplayName("Attempt to get all museums with zero size")
  void negativeGetAllMuseumsWithZeroSizeTest() {
    var response = client.getAllMuseums(DEFAULT_PAGE, 0);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHavePageSizeLessThanOneError(response);
  }
}
