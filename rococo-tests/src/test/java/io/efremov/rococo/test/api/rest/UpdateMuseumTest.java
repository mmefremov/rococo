package io.efremov.rococo.test.api.rest;

import static io.efremov.rococo.config.Constants.MAX_DESCRIPTION_LENGTH;
import static io.efremov.rococo.config.Constants.MAX_PHOTO_SIZE;
import static io.efremov.rococo.config.Constants.MAX_TITLE_LENGTH;
import static io.efremov.rococo.config.Constants.MIN_DESCRIPTION_LENGTH;
import static io.efremov.rococo.config.Constants.MIN_TITLE_LENGTH;
import static io.efremov.rococo.config.Constants.MUTATION_API_TAG;

import io.efremov.rococo.api.validation.RestValidation;
import io.efremov.rococo.data.repository.CountryRepository;
import io.efremov.rococo.jupiter.annotation.Authentication;
import io.efremov.rococo.jupiter.annotation.NewMuseum;
import io.efremov.rococo.model.CountryInfoRequest;
import io.efremov.rococo.model.GeoInfoRequest;
import io.efremov.rococo.model.MuseumInfoResponse;
import io.efremov.rococo.model.UpdateMuseumInfoRequest;
import io.efremov.rococo.provider.MuseumProvider;
import io.efremov.rococo.service.GatewayApiClient;
import io.efremov.rococo.util.RandomDataUtils;
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
@Story("Update a museum")
class UpdateMuseumTest {

  private final GatewayApiClient client = new GatewayApiClient();
  private final Model<UpdateMuseumInfoRequest> model = MuseumProvider.getUpdateMuseumInfoRequestModel();

  @Test
  @Authentication
  @NewMuseum
  @DisplayName("Update museum title")
  void positiveUpdateMuseumWithTitleTest(MuseumInfoResponse museum) {
    var request = MuseumProvider.getUpdateMuseumInfoRequestApi(museum)
        .set(Select.field(UpdateMuseumInfoRequest::title), RandomDataUtils.randomTitle())
        .create();
    var response = client.updateMuseum(request);

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    var body = response.body();
    RestValidation.checkResponse(request, body);
    RestValidation.checkEntity(request, body.id());
  }

  @Test
  @Authentication
  @NewMuseum
  @DisplayName("Update museum description")
  void positiveUpdateMuseumWithDescriptionTest(MuseumInfoResponse museum) {
    var request = MuseumProvider.getUpdateMuseumInfoRequestApi(museum)
        .set(Select.field(UpdateMuseumInfoRequest::description), RandomDataUtils.randomParagraph())
        .create();
    var response = client.updateMuseum(request);

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    var body = response.body();
    RestValidation.checkResponse(request, body);
    RestValidation.checkEntity(request, body.id());
  }

  @Test
  @Authentication
  @NewMuseum
  @DisplayName("Update museum photo")
  void positiveUpdateMuseumWithPhotoTest(MuseumInfoResponse museum) {
    var request = MuseumProvider.getUpdateMuseumInfoRequestApi(museum)
        .set(Select.field(UpdateMuseumInfoRequest::photo), RandomDataUtils.randomPhoto())
        .create();
    var response = client.updateMuseum(request);

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    var body = response.body();
    RestValidation.checkResponse(request, body);
  }

  @Test
  @Authentication
  @NewMuseum
  @DisplayName("Update museum city")
  void positiveUpdateMuseumWithCityTest(MuseumInfoResponse museum) {
    var request = MuseumProvider.getUpdateMuseumInfoRequestApi(museum)
        .set(Select.field(GeoInfoRequest::city), RandomDataUtils.randomCity())
        .create();
    var response = client.updateMuseum(request);

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    var body = response.body();
    RestValidation.checkResponse(request, body);
  }

  @Test
  @Authentication
  @NewMuseum
  @DisplayName("Update museum country id")
  void positiveUpdateMuseumWithCountryIdTest(MuseumInfoResponse museum) {
    var request = MuseumProvider.getUpdateMuseumInfoRequestApi(museum)
        .set(Select.field(CountryInfoRequest::id), new CountryRepository().findAny().getId())
        .create();
    var response = client.updateMuseum(request);

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    var body = response.body();
    RestValidation.checkResponse(request, body);
  }

  @Test
  @DisplayName("Attempt to update museum without auth")
  void negativeUpdateMuseumWithoutAuthTest() {
    var request = Instancio.create(model);
    var response = client.updateMuseum(request);

    RestValidation.responseMustHaveUnauthorizedStatus(response);
    RestValidation.bodyMustBeEmpty(response);
  }

  @Test
  @Authentication
  @DisplayName("Attempt to update museum with non-existing id")
  void negativeUpdateMuseumWithNonExistentIdTest() {
    var request = Instancio.of(model)
        .set(Select.field(UpdateMuseumInfoRequest::id), UUID.randomUUID())
        .create();
    var response = client.updateMuseum(request);

    RestValidation.responseMustHaveNotFoundStatus(response);
    RestValidation.bodyMustBeEmpty(response);
  }

  @Test
  @Authentication
  @NewMuseum
  @DisplayName("Attempt to update museum with duplicate title")
  void negativeUpdateMuseumWithDuplicateTitleTest(MuseumInfoResponse existedMuseum) {
    var request = Instancio.of(model)
        .set(Select.field(UpdateMuseumInfoRequest::title), existedMuseum.title())
        .set(Select.field(GeoInfoRequest::city), existedMuseum.geo().city())
        .create();
    var response = client.updateMuseum(request);

    RestValidation.responseMustHaveConflictStatus(response);
    String error = "Museum title already taken: %s".formatted(existedMuseum.title());
    RestValidation.bodyMustHaveTheError(response, error);
  }

  @ParameterizedTest
  @Authentication
  @NullAndEmptySource
  @DisplayName("Attempt to update museum with empty title")
  void negativeUpdateMuseumWithEmptyTitleTest(String title) {
    var request = Instancio.of(model)
        .set(Select.field(UpdateMuseumInfoRequest::title), title)
        .create();
    var response = client.updateMuseum(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @ParameterizedTest
  @Authentication
  @ValueSource(ints = {MIN_TITLE_LENGTH - 1, MAX_TITLE_LENGTH + 1})
  @DisplayName("Attempt to update museum with title length that is out of range")
  void negativeUpdateMuseumWithTitleLengthThatIsOutOfRangeTest(int length) {
    var request = Instancio.of(model)
        .assign(Assign.valueOf(UpdateMuseumInfoRequest::title)
            .generate(gen -> gen.string().length(length)))
        .create();
    var response = client.updateMuseum(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @ParameterizedTest
  @Authentication
  @NullAndEmptySource
  @DisplayName("Attempt to update museum with empty description")
  void negativeUpdateMuseumWithEmptyDescriptionTest(String description) {
    var request = Instancio.of(model)
        .set(Select.field(UpdateMuseumInfoRequest::description), description)
        .create();
    var response = client.updateMuseum(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @ParameterizedTest
  @Authentication
  @ValueSource(ints = {MIN_DESCRIPTION_LENGTH - 1, MAX_DESCRIPTION_LENGTH + 1})
  @DisplayName("Attempt to update museum with description length that is out of range")
  void negativeUpdateMuseumWithDescriptionLengthThatIsOutOfRangeTest(int length) {
    var request = Instancio.of(model)
        .assign(Assign.valueOf(UpdateMuseumInfoRequest::description)
            .generate(gen -> gen.string().length(length)))
        .create();
    var response = client.updateMuseum(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @ParameterizedTest
  @Authentication
  @NullAndEmptySource
  @DisplayName("Attempt to update museum with empty photo")
  void negativeUpdateMuseumWithEmptyPhotoTest(String photo) {
    var request = Instancio.of(model)
        .set(Select.field(UpdateMuseumInfoRequest::photo), photo)
        .create();
    var response = client.updateMuseum(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @Test
  @Authentication
  @DisplayName("Attempt to update museum with photo exceeding the limit size")
  void negativeUpdateMuseumWithPhotoExceedingLimitSizeTest() {
    var request = Instancio.of(model)
        .assign(Assign.valueOf(UpdateMuseumInfoRequest::photo)
            .generate(gen -> gen.string().length(MAX_PHOTO_SIZE + 1)))
        .create();
    var response = client.updateMuseum(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHavePhotoExceedLimitError(response);
  }

  @Test
  @Authentication
  @DisplayName("Attempt to update museum without geo info")
  void negativeUpdateMuseumWithoutGeoInfoTest() {
    var request = Instancio.of(model)
        .ignore(Select.field(UpdateMuseumInfoRequest::geo))
        .create();
    var response = client.updateMuseum(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @ParameterizedTest
  @Authentication
  @NullAndEmptySource
  @DisplayName("Attempt to update museum with empty city")
  void negativeUpdateMuseumWithEmptyCityTest(String city) {
    var request = Instancio.of(model)
        .set(Select.field(GeoInfoRequest::city), city)
        .create();
    var response = client.updateMuseum(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @ParameterizedTest
  @Authentication
  @ValueSource(ints = {MIN_TITLE_LENGTH - 1, MAX_TITLE_LENGTH + 1})
  @DisplayName("Attempt to update museum with city length that is out of range")
  void negativeUpdateMuseumWithCityLengthThatIsOutOfRangeTest(int length) {
    var request = Instancio.of(model)
        .assign(Assign.valueOf(GeoInfoRequest::city)
            .generate(gen -> gen.string().length(length)))
        .create();
    var response = client.updateMuseum(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @Test
  @Authentication
  @DisplayName("Attempt to update museum without country info")
  void negativeUpdateMuseumWithoutCountryInfoTest() {
    var request = Instancio.of(model)
        .ignore(Select.field(GeoInfoRequest::country))
        .create();
    var response = client.updateMuseum(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @Test
  @Authentication
  @DisplayName("Attempt to update museum without country id")
  void negativeUpdateMuseumWithoutCountryIdTest() {
    var request = Instancio.of(model)
        .ignore(Select.field(CountryInfoRequest::id))
        .create();
    var response = client.updateMuseum(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @Test
  @Authentication
  @DisplayName("Attempt to update museum with non-existent country id")
  void negativeUpdateMuseumWithNonExistentCountryIdTest() {
    var request = Instancio.of(model)
        .set(Select.field(CountryInfoRequest::id), UUID.randomUUID())
        .create();
    var response = client.updateMuseum(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    String error = "Country not found: %s".formatted(request.geo().country().id());
    RestValidation.bodyMustHaveTheError(response, error);
  }
}
