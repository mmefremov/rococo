package io.efremov.rococo.test.api.rest;

import static io.efremov.rococo.config.Constants.MAX_PARAGRAPH_LENGTH;
import static io.efremov.rococo.config.Constants.MAX_PHOTO_SIZE;
import static io.efremov.rococo.config.Constants.MAX_TITLE_LENGTH;
import static io.efremov.rococo.config.Constants.MIN_PARAGRAPH_LENGTH;
import static io.efremov.rococo.config.Constants.MIN_TITLE_LENGTH;
import static io.efremov.rococo.config.Constants.MUTATION_API_TAG;

import io.efremov.rococo.api.validation.RestValidation;
import io.efremov.rococo.jupiter.annotation.Authentication;
import io.efremov.rococo.jupiter.annotation.NewArtist;
import io.efremov.rococo.model.ArtistInfoResponse;
import io.efremov.rococo.model.CreateArtistInfoRequest;
import io.efremov.rococo.provider.ArtistProvider;
import io.efremov.rococo.service.GatewayApiClient;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
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
@Story("Create an artist")
class CreateArtistTest {

  private final GatewayApiClient client = new GatewayApiClient();
  private final Model<CreateArtistInfoRequest> model = ArtistProvider.getArtistInfoRequestModel();

  @Test
  @Authentication
  @DisplayName("Create artist")
  void positiveCreateArtistTest() {
    var request = Instancio.create(model);
    var response = client.createArtist(request);

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    var body = response.body();
    RestValidation.checkResponse(request, body);
    RestValidation.checkEntity(request, body.id());
  }

  @Test
  @DisplayName("Attempt to create artist without auth")
  void negativeCreateArtistWithoutAuthTest() {
    var request = Instancio.create(model);
    var response = client.createArtist(request);

    RestValidation.responseMustHaveUnauthorizedStatus(response);
    RestValidation.bodyMustBeEmpty(response);
  }

  @Test
  @Authentication
  @NewArtist
  @DisplayName("Attempt to create artist with duplicate name")
  void negativeCreateArtistWithDuplicateNameTest(ArtistInfoResponse existedArtist) {
    var request = Instancio.of(model)
        .set(Select.field(CreateArtistInfoRequest::name), existedArtist.name())
        .create();
    var response = client.createArtist(request);

    RestValidation.responseMustHaveConflictStatus(response);
    String error = "Artist already exists: %s".formatted(existedArtist.name());
    RestValidation.bodyMustHaveTheError(response, error);
  }

  @ParameterizedTest
  @Authentication
  @NullAndEmptySource
  @DisplayName("Attempt to create artist with empty name")
  void negativeCreateArtistWithEmptyNameTest(String name) {
    var request = Instancio.of(model)
        .set(Select.field(CreateArtistInfoRequest::name), name)
        .create();
    var response = client.createArtist(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @ParameterizedTest
  @Authentication
  @ValueSource(ints = {MIN_TITLE_LENGTH - 1, MAX_TITLE_LENGTH + 1})
  @DisplayName("Attempt to create artist with name length that is out of range")
  void negativeCreateArtistWithNameLengthThatIsOutOfRangeTest(int length) {
    var request = Instancio.of(model)
        .assign(Assign.valueOf(CreateArtistInfoRequest::name)
            .generate(gen -> gen.string().length(length)))
        .create();
    var response = client.createArtist(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @ParameterizedTest
  @Authentication
  @NullAndEmptySource
  @DisplayName("Attempt to create artist with empty biography")
  void negativeCreateArtistWithEmptyBiographyTest(String biography) {
    var request = Instancio.of(model)
        .set(Select.field(CreateArtistInfoRequest::biography), biography)
        .create();
    var response = client.createArtist(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @ParameterizedTest
  @Authentication
  @ValueSource(ints = {MIN_PARAGRAPH_LENGTH - 1, MAX_PARAGRAPH_LENGTH + 1})
  @DisplayName("Attempt to create artist with biography length that is out of range")
  void negativeCreateArtistWithBiographyLengthThatIsOutOfRangeTest(int length) {
    var request = Instancio.of(model)
        .assign(Assign.valueOf(CreateArtistInfoRequest::biography)
            .generate(gen -> gen.string().length(length)))
        .create();
    var response = client.createArtist(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @ParameterizedTest
  @Authentication
  @NullAndEmptySource
  @DisplayName("Attempt to create artist with empty photo")
  void negativeCreateArtistWithEmptyPhotoTest(String photo) {
    var request = Instancio.of(model)
        .set(Select.field(CreateArtistInfoRequest::photo), photo)
        .create();
    var response = client.createArtist(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @Test
  @Authentication
  @DisplayName("Attempt to create artist with photo exceeding the limit size")
  void negativeCreateArtistWithPhotoExceedingLimitSizeTest() {
    var request = Instancio.of(model)
        .assign(Assign.valueOf(CreateArtistInfoRequest::photo)
            .generate(gen -> gen.string().length(MAX_PHOTO_SIZE + 1)))
        .create();
    var response = client.createArtist(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHavePhotoExceedLimitError(response);
  }
}
