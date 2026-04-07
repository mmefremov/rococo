package io.efremov.rococo.test.api.rest;

import static io.efremov.rococo.config.Constants.DEFAULT_PASSWORD;
import static io.efremov.rococo.config.Constants.MUTATION_API_TAG;
import static io.efremov.rococo.util.RandomDataUtils.GEN;

import io.efremov.rococo.api.validation.RestValidation;
import io.efremov.rococo.service.AuthApiClient;
import io.efremov.rococo.util.OauthUtils;
import io.efremov.rococo.util.RandomDataUtils;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.util.Objects;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import retrofit2.Response;

@Tag(MUTATION_API_TAG)
@Epic("API")
@Feature("rococo-auth")
@Story("Register")
class RegisterTest {

  private final AuthApiClient client = new AuthApiClient();
  private String csrf;

  @BeforeEach
  void setup() {
    Response<ResponseBody> formResponse = client.getRegisterForm();
    csrf = OauthUtils.findCsrfValue(Objects.requireNonNull(formResponse.body()));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @DisplayName("Attempt to register with empty username")
  void negativeRegisterWithEmptyUsernameTest(String username) {
    var response = client.register(username, DEFAULT_PASSWORD, csrf);

    RestValidation.responseMustHaveBadRequestStatus(response);
  }

  @ParameterizedTest
  @ValueSource(ints = {2, 51})
  @DisplayName("Attempt to register with username length that is out of range")
  void negativeRegisterWithUsernameLengthThatIsOutOfRangeTest(int length) {
    var response = client.register(
        GEN.string().length(length).get(),
        DEFAULT_PASSWORD, csrf);

    RestValidation.responseMustHaveBadRequestStatus(response);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @DisplayName("Attempt to register with empty password")
  void negativeRegisterWithEmptyPasswordTest(String password) {
    var response = client.register(RandomDataUtils.randomUsername(), password, csrf);

    RestValidation.responseMustHaveBadRequestStatus(response);
  }

  @ParameterizedTest
  @ValueSource(ints = {2, 13})
  @DisplayName("Attempt to register with password length that is out of range")
  void negativeRegisterWithPasswordLengthThatIsOutOfRangeTest(int length) {
    var response = client.register(
        RandomDataUtils.randomUsername(),
        GEN.string().length(length).get(), csrf);

    RestValidation.responseMustHaveBadRequestStatus(response);
  }
}
