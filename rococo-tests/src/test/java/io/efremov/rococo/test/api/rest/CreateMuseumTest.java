package io.efremov.rococo.test.api.rest;

import static io.efremov.rococo.config.Constants.MAX_DESCRIPTION_LENGTH;
import static io.efremov.rococo.config.Constants.MAX_PHOTO_SIZE;
import static io.efremov.rococo.config.Constants.MAX_TITLE_LENGTH;
import static io.efremov.rococo.config.Constants.MIN_DESCRIPTION_LENGTH;
import static io.efremov.rococo.config.Constants.MIN_TITLE_LENGTH;
import static io.efremov.rococo.config.Constants.MUTATION_API_TAG;

import io.efremov.rococo.api.validation.RestValidation;
import io.efremov.rococo.jupiter.annotation.Authentication;
import io.efremov.rococo.jupiter.annotation.NewMuseum;
import io.efremov.rococo.model.CountryInfoRequest;
import io.efremov.rococo.model.CreateMuseumInfoRequest;
import io.efremov.rococo.model.GeoInfoRequest;
import io.efremov.rococo.model.MuseumInfoResponse;
import io.efremov.rococo.provider.MuseumProvider;
import io.efremov.rococo.service.GatewayApiClient;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.util.UUID;
import org.instancio.Assign;
import org.instancio.Instancio;
import org.instancio.Model;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@Tag(MUTATION_API_TAG)
@Epic("API")
@Feature("rococo-gateway")
@Story("Create a museum")
class CreateMuseumTest {

  private final GatewayApiClient client = new GatewayApiClient();
  private final Model<CreateMuseumInfoRequest> model = MuseumProvider.getCreateMuseumInfoRequestModel();

  @Test
  @Authentication
  @DisplayName("Create museum")
  void positiveCreateMuseumTest() {
    var request = Instancio.create(model);
    var response = client.createMuseum(request);

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    var body = response.body();
    RestValidation.checkResponse(request, body);
    RestValidation.checkEntity(request, body.id());
  }

  @Test
  @DisplayName("Attempt to create museum without auth")
  void negativeCreateMuseumWithoutAuthTest() {
    var request = Instancio.create(model);
    var response = client.createMuseum(request);

    RestValidation.responseMustHaveUnauthorizedStatus(response);
    RestValidation.bodyMustBeEmpty(response);
  }

  @Test
  @Authentication
  @NewMuseum
  @DisplayName("Attempt to create museum with duplicate title")
  void negativeCreateMuseumWithDuplicateTitleTest(MuseumInfoResponse existedMuseum) {
    var request = Instancio.of(model)
        .set(Select.field(CreateMuseumInfoRequest::title), existedMuseum.title())
        .set(Select.field(GeoInfoRequest::city), existedMuseum.geo().city())
        .create();
    var response = client.createMuseum(request);

    RestValidation.responseMustHaveConflictStatus(response);
    String error = "Museum title already taken: %s".formatted(existedMuseum.title());
    RestValidation.bodyMustHaveTheError(response, error);
  }

  @ParameterizedTest
  @Authentication
  @NullAndEmptySource
  @DisplayName("Attempt to create museum with empty title")
  void negativeCreateMuseumWithEmptyTitleTest(String title) {
    var request = Instancio.of(model)
        .set(Select.field(CreateMuseumInfoRequest::title), title)
        .create();
    var response = client.createMuseum(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @ParameterizedTest
  @Authentication
  @ValueSource(ints = {MIN_TITLE_LENGTH - 1, MAX_TITLE_LENGTH + 1})
  @DisplayName("Attempt to create museum with title length that is out of range")
  void negativeCreateMuseumWithTitleLengthThatIsOutOfRangeTest(int length) {
    var request = Instancio.of(model)
        .assign(Assign.valueOf(CreateMuseumInfoRequest::title)
            .generate(gen -> gen.string().length(length)))
        .create();
    var response = client.createMuseum(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @ParameterizedTest
  @Authentication
  @NullAndEmptySource
  @DisplayName("Attempt to create museum with empty description")
  void negativeCreateMuseumWithEmptyDescriptionTest(String description) {
    var request = Instancio.of(model)
        .set(Select.field(CreateMuseumInfoRequest::description), description)
        .create();
    var response = client.createMuseum(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @ParameterizedTest
  @Authentication
  @ValueSource(ints = {MIN_DESCRIPTION_LENGTH - 1, MAX_DESCRIPTION_LENGTH + 1})
  @DisplayName("Attempt to create museum with description length that is out of range")
  void negativeCreateMuseumWithDescriptionLengthThatIsOutOfRangeTest(int length) {
    var request = Instancio.of(model)
        .assign(Assign.valueOf(CreateMuseumInfoRequest::description)
            .generate(gen -> gen.string().length(length)))
        .create();
    var response = client.createMuseum(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @ParameterizedTest
  @Authentication
  @NullAndEmptySource
  @DisplayName("Attempt to create museum with empty photo")
  void negativeCreateMuseumWithEmptyPhotoTest(String photo) {
    var request = Instancio.of(model)
        .set(Select.field(CreateMuseumInfoRequest::photo), photo)
        .create();
    var response = client.createMuseum(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @Test
  @Authentication
  @DisplayName("Attempt to create museum with photo exceeding the limit size")
  void negativeCreateMuseumWithPhotoExceedingLimitSizeTest() {
    var request = Instancio.of(model)
        .assign(Assign.valueOf(CreateMuseumInfoRequest::photo)
            .generate(gen -> gen.string().length(MAX_PHOTO_SIZE + 1)))
        .create();
    var response = client.createMuseum(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHavePhotoExceedLimitError(response);
  }

  @Test
  @Authentication
  @DisplayName("Attempt to create museum without geo info")
  void negativeCreateMuseumWithoutGeoInfoTest() {
    var request = Instancio.of(model)
        .ignore(Select.field(CreateMuseumInfoRequest::geo))
        .create();
    var response = client.createMuseum(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @Authentication
  @DisplayName("Attempt to create museum with empty city")
  void negativeCreateMuseumWithEmptyCityTest(String city) {
    var request = Instancio.of(model)
        .set(Select.field(GeoInfoRequest::city), city)
        .create();
    var response = client.createMuseum(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @Test
  @Authentication
  @DisplayName("Attempt to create museum without country info")
  void negativeCreateMuseumWithoutCountryInfoTest() {
    var request = Instancio.of(model)
        .ignore(Select.field(GeoInfoRequest::country))
        .create();
    var response = client.createMuseum(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @Test
  @Authentication
  @DisplayName("Attempt to create museum without country id")
  void negativeCreateMuseumWithoutCountryIdTest() {
    var request = Instancio.of(model)
        .ignore(Select.field(CountryInfoRequest::id))
        .create();
    var response = client.createMuseum(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @Test
  @Authentication
  @DisplayName("Attempt to create museum non-existent country id")
  void negativeCreateMuseumWitNonExistentCountryIdTest() {
    var request = Instancio.of(model)
        .set(Select.field(CountryInfoRequest::id), UUID.randomUUID())
        .create();
    var response = client.createMuseum(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    String error = "Country not found: %s".formatted(request.geo().country().id());
    RestValidation.bodyMustHaveTheError(response, error);
  }
}
