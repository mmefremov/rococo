package io.efremov.rococo.test.api.rest;

import static io.efremov.rococo.config.Constants.INFO_API_TAG;

import io.efremov.rococo.api.validation.RestValidation;
import io.efremov.rococo.data.repository.CountryRepository;
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
@Story("Get all countries")
class GetAllCountriesTest {

  private final GatewayApiClient client = new GatewayApiClient();
  private final int DEFAULT_PAGE = 0;
  private final int DEFAULT_SIZE = 20;

  @Test
  @DisplayName("Get all countries")
  void positiveGetAllCountriesTest() {
    var response = client.getAllCountries(DEFAULT_PAGE, DEFAULT_SIZE);

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    RestValidation.checkCountriesPageResponse(response);
  }

  @Test
  @DisplayName("Get all countries with last page")
  void positiveGetAllCountriesWithLastPageTest() {
    int count = Math.toIntExact(new CountryRepository().countAll());
    var response = client.getAllCountries(count / DEFAULT_SIZE, DEFAULT_SIZE);

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    RestValidation.checkCountriesPageResponse(response);
  }

  @Test
  @DisplayName("Attempt to get all countries with negative page")
  void negativeGetAllCountriesWithNegativePageTest() {
    var response = client.getAllCountries(-1, DEFAULT_SIZE);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHavePageIndexLessThanZeroError(response);
  }

  @Test
  @DisplayName("Attempt to get all countries with zero size")
  void negativeGetAllCountriesWithZeroSizeTest() {
    var response = client.getAllCountries(DEFAULT_PAGE, 0);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHavePageSizeLessThanOneError(response);
  }
}
