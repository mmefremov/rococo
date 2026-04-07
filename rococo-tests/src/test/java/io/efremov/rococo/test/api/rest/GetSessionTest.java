package io.efremov.rococo.test.api.rest;

import static io.efremov.rococo.config.Constants.INFO_API_TAG;

import io.efremov.rococo.api.validation.RestValidation;
import io.efremov.rococo.jupiter.annotation.Authentication;
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
@Story("Get session")
class GetSessionTest {

  private final GatewayApiClient client = new GatewayApiClient();

  @Test
  @Authentication
  @DisplayName("Get session")
  void positiveGetSessionTest() {
    var response = client.getSession();

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.checkSessionResponseWithAuth(response.body());
  }

  @Test
  @DisplayName("Attempt to get session without auth")
  void negativeGetSessionTest() {
    var response = client.getSession();

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.checkSessionResponseWithoutAuth(response.body());
  }
}
