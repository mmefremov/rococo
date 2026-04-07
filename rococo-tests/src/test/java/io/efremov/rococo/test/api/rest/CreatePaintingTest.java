package io.efremov.rococo.test.api.rest;

import static io.efremov.rococo.config.Constants.MAX_DESCRIPTION_LENGTH;
import static io.efremov.rococo.config.Constants.MAX_PHOTO_SIZE;
import static io.efremov.rococo.config.Constants.MAX_TITLE_LENGTH;
import static io.efremov.rococo.config.Constants.MIN_DESCRIPTION_LENGTH;
import static io.efremov.rococo.config.Constants.MIN_TITLE_LENGTH;
import static io.efremov.rococo.config.Constants.MUTATION_API_TAG;

import io.efremov.rococo.api.validation.RestValidation;
import io.efremov.rococo.jupiter.annotation.AnyPainting;
import io.efremov.rococo.jupiter.annotation.Authentication;
import io.efremov.rococo.model.ArtistInfoRequest;
import io.efremov.rococo.model.CreatePaintingInfoRequest;
import io.efremov.rococo.model.MuseumInfoRequest;
import io.efremov.rococo.model.PaintingInfoResponse;
import io.efremov.rococo.provider.PaintingProvider;
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
@Story("Create a painting")
class CreatePaintingTest {

  private final GatewayApiClient client = new GatewayApiClient();
  private final Model<CreatePaintingInfoRequest> model = PaintingProvider.getCreatePaintingInfoRequestModel();

  @Test
  @Authentication
  @DisplayName("Create painting")
  void positiveCreatePaintingTest() {
    var request = Instancio.create(model);
    var response = client.createPainting(request);

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    var body = response.body();
    RestValidation.checkResponse(request, body);
    RestValidation.checkEntity(request, body.id());
  }

  @Test
  @DisplayName("Attempt to create painting without auth")
  void negativeCreatePaintingWithoutAuthTest() {
    var request = Instancio.create(model);
    var response = client.createPainting(request);

    RestValidation.responseMustHaveUnauthorizedStatus(response);
    RestValidation.bodyMustBeEmpty(response);
  }

  @Test
  @Authentication
  @AnyPainting
  @DisplayName("Attempt to create painting with duplicate title")
  void negativeCreatePaintingWithDuplicateTitleTest(PaintingInfoResponse existedPainting) {
    var request = Instancio.of(model)
        .set(Select.field(CreatePaintingInfoRequest::title), existedPainting.title())
        .set(Select.field(ArtistInfoRequest::id), existedPainting.artist().id())
        .create();
    var response = client.createPainting(request);

    RestValidation.responseMustHaveConflictStatus(response);
    String error = "Painting already exists: %s".formatted(existedPainting.title());
    RestValidation.bodyMustHaveTheError(response, error);
  }

  @ParameterizedTest
  @Authentication
  @NullAndEmptySource
  @DisplayName("Attempt to create painting with empty title")
  void negativeCreatePaintingWithEmptyTitleTest(String title) {
    var request = Instancio.of(model)
        .set(Select.field(CreatePaintingInfoRequest::title), title)
        .create();
    var response = client.createPainting(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @ParameterizedTest
  @Authentication
  @ValueSource(ints = {MIN_TITLE_LENGTH - 1, MAX_TITLE_LENGTH + 1})
  @DisplayName("Attempt to create painting with title length that is out of range")
  void negativeCreatePaintingWithTitleLengthThatIsOutOfRangeTest(int length) {
    var request = Instancio.of(model)
        .assign(Assign.valueOf(CreatePaintingInfoRequest::title)
            .generate(gen -> gen.string().length(length)))
        .create();
    var response = client.createPainting(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @ParameterizedTest
  @Authentication
  @NullAndEmptySource
  @DisplayName("Attempt to create painting with empty description")
  void negativeCreatePaintingWithEmptyDescriptionTest(String description) {
    var request = Instancio.of(model)
        .set(Select.field(CreatePaintingInfoRequest::description), description)
        .create();
    var response = client.createPainting(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @ParameterizedTest
  @Authentication
  @ValueSource(ints = {MIN_DESCRIPTION_LENGTH - 1, MAX_DESCRIPTION_LENGTH + 1})
  @DisplayName("Attempt to create painting with description length that is out of range")
  void negativeCreatePaintingWithDescriptionLengthThatIsOutOfRangeTest(int length) {
    var request = Instancio.of(model)
        .assign(Assign.valueOf(CreatePaintingInfoRequest::description)
            .generate(gen -> gen.string().length(length)))
        .create();
    var response = client.createPainting(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @ParameterizedTest
  @Authentication
  @NullAndEmptySource
  @DisplayName("Attempt to create painting with empty content")
  void negativeCreatePaintingWithEmptyContentTest(String photo) {
    var request = Instancio.of(model)
        .set(Select.field(CreatePaintingInfoRequest::content), photo)
        .create();
    var response = client.createPainting(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }


  @Test
  @Authentication
  @DisplayName("Attempt to create painting with content exceeding the limit size")
  void negativeCreatePaintingWithContentExceedingLimitSizeTest() {
    var request = Instancio.of(model)
        .assign(Assign.valueOf(CreatePaintingInfoRequest::content)
            .generate(gen -> gen.string().length(MAX_PHOTO_SIZE + 1)))
        .create();
    var response = client.createPainting(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveContentExceedLimitError(response);
  }

  @Test
  @Authentication
  @DisplayName("Attempt to create painting without artist info")
  void negativeCreatePaintingWithoutArtistInfoTest() {
    var request = Instancio.of(model)
        .ignore(Select.field(CreatePaintingInfoRequest::artist))
        .create();
    var response = client.createPainting(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @Test
  @Authentication
  @DisplayName("Attempt to create painting without artist id")
  void negativeCreatePaintingWithoutArtistIdTest() {
    var request = Instancio.of(model)
        .ignore(Select.field(ArtistInfoRequest::id))
        .create();
    var response = client.createPainting(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @Test
  @Authentication
  @DisplayName("Attempt to create painting with non-existent artist id")
  void negativeCreatePaintingWithNonExistentArtistIdTest() {
    var request = Instancio.of(model)
        .set(Select.field(ArtistInfoRequest::id), UUID.randomUUID())
        .create();
    var response = client.createPainting(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    String error = "Artist not found: %s".formatted(request.artist().id());
    RestValidation.bodyMustHaveTheError(response, error);
  }

  @Test
  @Authentication
  @DisplayName("Attempt to create painting without museum info")
  void negativeCreatePaintingWithoutMuseumInfoTest() {
    var request = Instancio.of(model)
        .ignore(Select.field(CreatePaintingInfoRequest::museum))
        .create();
    var response = client.createPainting(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @Test
  @Authentication
  @DisplayName("Attempt to create painting with non-existent museum id")
  void negativeCreatePaintingWithNonExistentMuseumIdTest() {
    var request = Instancio.of(model)
        .set(Select.field(MuseumInfoRequest::id), UUID.randomUUID())
        .create();
    var response = client.createPainting(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    String error = "Museum not found: %s".formatted(request.museum().id());
    RestValidation.bodyMustHaveTheError(response, error);
  }
}
