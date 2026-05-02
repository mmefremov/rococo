package io.efremov.rococo.test.api.rest;

import static io.efremov.rococo.config.Constants.MUTATION_API_TAG;

import io.efremov.rococo.api.validation.RestValidation;
import io.efremov.rococo.jupiter.annotation.Authentication;
import io.efremov.rococo.model.UpdateUserInfoRequest;
import io.efremov.rococo.model.UserInfoResponse;
import io.efremov.rococo.provider.UserProvider;
import io.efremov.rococo.service.GatewayApiClient;
import io.efremov.rococo.util.RandomDataUtils;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.instancio.Instancio;
import org.instancio.Model;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import retrofit2.Response;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag(MUTATION_API_TAG)
@Epic("API")
@Feature("rococo-gateway")
@Story("Update current user")
class UpdateUserTest {

  private final GatewayApiClient client = new GatewayApiClient();
  private final Model<UpdateUserInfoRequest> model = UserProvider.getUpdateUserRequestModel();

  @Test
  @Authentication
  @DisplayName("Update user with firstname")
  void positiveUpdateUserWithFirstnameTest() {
    var request = Instancio.of(model)
        .set(Select.field(UpdateUserInfoRequest::firstname), RandomDataUtils.randomFirstName())
        .create();

    var response = client.updateUser(request);

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    var body = response.body();
    RestValidation.checkResponse(body);
    RestValidation.checkEntity(request, body);
  }

  @Test
  @Authentication
  @DisplayName("Update user with lastname")
  void positiveUpdateUserWithLastnameTest() {
    var request = Instancio.of(model)
        .set(Select.field(UpdateUserInfoRequest::lastname), RandomDataUtils.randomLastName())
        .create();

    var response = client.updateUser(request);

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    var body = response.body();
    RestValidation.checkResponse(body);
    RestValidation.checkEntity(request, body);
  }

  @Test
  @Authentication
  @DisplayName("Update user with avatar")
  void positiveUpdateUserWithAvatarTest() {
    var request = Instancio.of(model)
        .set(Select.field(UpdateUserInfoRequest::avatar), RandomDataUtils.randomPhoto())
        .create();

    var response = client.updateUser(request);

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    var body = response.body();
    RestValidation.checkResponse(body);
    RestValidation.checkEntity(request, body);
  }

  @Test
  @DisplayName("Attempt to update user without auth")
  void negativeUpdateUserWithoutAuthTest() {
    var request = Instancio.create(model);
    Response<UserInfoResponse> response = client.updateUser(request);

    RestValidation.responseMustHaveUnauthorizedStatus(response);
    RestValidation.bodyMustBeEmpty(response);
  }
}
