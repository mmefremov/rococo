package io.efremov.rococo.test.api.rest;

import static io.efremov.rococo.config.Constants.MAX_PARAGRAPH_LENGTH;
import static io.efremov.rococo.config.Constants.MAX_PHOTO_SIZE;
import static io.efremov.rococo.config.Constants.MAX_TITLE_LENGTH;
import static io.efremov.rococo.config.Constants.MIN_PARAGRAPH_LENGTH;
import static io.efremov.rococo.config.Constants.MIN_TITLE_LENGTH;
import static io.efremov.rococo.config.Constants.MUTATION_API_TAG;

import io.efremov.rococo.api.validation.RestValidation;
import io.efremov.rococo.jupiter.annotation.AnyArtist;
import io.efremov.rococo.jupiter.annotation.Authentication;
import io.efremov.rococo.jupiter.annotation.NewArtist;
import io.efremov.rococo.model.ArtistInfoResponse;
import io.efremov.rococo.model.UpdateArtistInfoRequest;
import io.efremov.rococo.provider.ArtistProvider;
import io.efremov.rococo.service.GatewayApiClient;
import io.efremov.rococo.util.RandomDataUtils;
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
@Story("Update an artist")
class UpdateArtistTest {

  private final GatewayApiClient client = new GatewayApiClient();
  private final Model<UpdateArtistInfoRequest> model = ArtistProvider.getUpdateArtistInfoRequestModel();

  @Test
  @Authentication
  @NewArtist
  @DisplayName("Update artist name")
  void positiveUpdateArtistWithNameTest(ArtistInfoResponse artist) {
    var request = ArtistProvider.getUpdateArtistInfoRequestApi(artist)
        .set(Select.field(UpdateArtistInfoRequest::name), RandomDataUtils.randomFullName())
        .create();
    var response = client.updateArtist(request);

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    var body = response.body();
    RestValidation.checkResponse(request, body);
    RestValidation.checkEntity(request, body.id());
  }

  @Test
  @Authentication
  @NewArtist
  @DisplayName("Update artist biography")
  void positiveUpdateArtistWithBiographyTest(ArtistInfoResponse artist) {
    var request = ArtistProvider.getUpdateArtistInfoRequestApi(artist)
        .set(Select.field(UpdateArtistInfoRequest::biography), RandomDataUtils.randomParagraph())
        .create();
    var response = client.updateArtist(request);

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    var body = response.body();
    RestValidation.checkResponse(request, body);
    RestValidation.checkEntity(request, body.id());
  }

  @Test
  @Authentication
  @NewArtist
  @DisplayName("Update artist photo")
  void positiveUpdateArtistWithPhotoTest(ArtistInfoResponse artist) {
    var request = ArtistProvider.getUpdateArtistInfoRequestApi(artist)
        .set(Select.field(UpdateArtistInfoRequest::photo), RandomDataUtils.randomPhoto())
        .create();
    var response = client.updateArtist(request);

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    var body = response.body();
    RestValidation.checkResponse(request, body);
  }

  @Test
  @DisplayName("Attempt to update artist without auth")
  void negativeUpdateArtistWithoutAuthTest() {
    var request = Instancio.create(model);
    var response = client.updateArtist(request);

    RestValidation.responseMustHaveUnauthorizedStatus(response);
    RestValidation.bodyMustBeEmpty(response);
  }

  @Test
  @Authentication
  @DisplayName("Attempt to update artist with non-existing id")
  void negativeUpdateArtistWithNonExistentIdTest() {
    var request = Instancio.of(model)
        .set(Select.field(UpdateArtistInfoRequest::id), java.util.UUID.randomUUID())
        .create();
    var response = client.updateArtist(request);

    RestValidation.responseMustHaveNotFoundStatus(response);
    RestValidation.bodyMustBeEmpty(response);
  }

  @Test
  @Authentication
  @AnyArtist
  @DisplayName("Attempt to update artist with duplicate name")
  void negativeUpdateArtistWithDuplicateNameTest(ArtistInfoResponse existedArtist) {
    var request = Instancio.of(model)
        .set(Select.field(UpdateArtistInfoRequest::name), existedArtist.name())
        .create();
    var response = client.updateArtist(request);

    RestValidation.responseMustHaveConflictStatus(response);
    String error = "Artist name already taken: %s".formatted(existedArtist.name());
    RestValidation.bodyMustHaveTheError(response, error);
  }

  @ParameterizedTest
  @Authentication
  @NullAndEmptySource
  @DisplayName("Attempt to update artist with empty name")
  void negativeUpdateArtistWithEmptyNameTest(String name) {
    var request = Instancio.of(model)
        .set(Select.field(UpdateArtistInfoRequest::name), name)
        .create();
    var response = client.updateArtist(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }


  @ParameterizedTest
  @Authentication
  @ValueSource(ints = {MIN_TITLE_LENGTH - 1, MAX_TITLE_LENGTH + 1})
  @DisplayName("Attempt to update artist with name length that is out of range")
  void negativeUpdateArtistWithNameLengthThatIsOutOfRangeTest(int length) {
    var request = Instancio.of(model)
        .assign(Assign.valueOf(UpdateArtistInfoRequest::name)
            .generate(gen -> gen.string().length(length)))
        .create();
    var response = client.updateArtist(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }


  @ParameterizedTest
  @Authentication
  @NullAndEmptySource
  @DisplayName("Attempt to update artist with empty biography")
  void negativeUpdateArtistWithEmptyBiographyTest(String biography) {
    var request = Instancio.of(model)
        .set(Select.field(UpdateArtistInfoRequest::biography), biography)
        .create();
    var response = client.updateArtist(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @ParameterizedTest
  @Authentication
  @ValueSource(ints = {MIN_PARAGRAPH_LENGTH - 1, MAX_PARAGRAPH_LENGTH + 1})
  @DisplayName("Attempt to update artist with biography length that is out of range")
  void negativeUpdateArtistWithBiographyLengthThatIsOutOfRangeTest(int length) {
    var request = Instancio.of(model)
        .assign(Assign.valueOf(UpdateArtistInfoRequest::biography)
            .generate(gen -> gen.string().length(length)))
        .create();
    var response = client.updateArtist(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @ParameterizedTest
  @Authentication
  @NullAndEmptySource
  @DisplayName("Attempt to update artist with empty photo")
  void negativeUpdateArtistWithEmptyPhotoTest(String photo) {
    var request = Instancio.of(model)
        .set(Select.field(UpdateArtistInfoRequest::photo), photo)
        .create();
    var response = client.updateArtist(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @Test
  @Authentication
  @DisplayName("Attempt to update artist with photo exceeding the limit size")
  void negativeUpdateArtistWithPhotoExceedingLimitSizeTest() {
    var request = Instancio.of(model)
        .assign(Assign.valueOf(UpdateArtistInfoRequest::photo)
            .generate(gen -> gen.string().length(MAX_PHOTO_SIZE + 1)))
        .create();
    var response = client.updateArtist(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHavePhotoExceedLimitError(response);
  }
}
