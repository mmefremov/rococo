package io.efremov.rococo.test.api.rest;

import static io.efremov.rococo.config.Constants.MAX_DESCRIPTION_LENGTH;
import static io.efremov.rococo.config.Constants.MAX_PHOTO_SIZE;
import static io.efremov.rococo.config.Constants.MIN_DESCRIPTION_LENGTH;
import static io.efremov.rococo.config.Constants.MUTATION_API_TAG;

import io.efremov.rococo.api.validation.RestValidation;
import io.efremov.rococo.jupiter.annotation.Authentication;
import io.efremov.rococo.jupiter.annotation.NewPainting;
import io.efremov.rococo.model.ArtistInfoRequest;
import io.efremov.rococo.model.MuseumInfoRequest;
import io.efremov.rococo.model.PaintingInfoResponse;
import io.efremov.rococo.model.UpdatePaintingInfoRequest;
import io.efremov.rococo.provider.ArtistProvider;
import io.efremov.rococo.provider.MuseumProvider;
import io.efremov.rococo.provider.PaintingProvider;
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
@Story("Update a painting")
class UpdatePaintingTest {

  private final GatewayApiClient client = new GatewayApiClient();
  private final Model<UpdatePaintingInfoRequest> model = PaintingProvider.getUpdatePaintingInfoRequestModel();

  @Test
  @Authentication
  @NewPainting
  @DisplayName("Update painting title")
  void positiveUpdatePaintingWithTitleTest(PaintingInfoResponse painting) {
    var request = PaintingProvider.getUpdatePaintingInfoRequestApi(painting)
        .set(Select.field(UpdatePaintingInfoRequest::title), RandomDataUtils.randomTitle())
        .create();
    var response = client.updatePainting(request);

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    var body = response.body();
    RestValidation.checkResponse(request, body);
    RestValidation.checkEntity(request, body.id());
  }

  @Test
  @Authentication
  @NewPainting
  @DisplayName("Update painting description")
  void positiveUpdatePaintingWithDescriptionTest(PaintingInfoResponse painting) {
    var request = PaintingProvider.getUpdatePaintingInfoRequestApi(painting)
        .set(Select.field(UpdatePaintingInfoRequest::description), RandomDataUtils.randomParagraph())
        .create();
    var response = client.updatePainting(request);

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    var body = response.body();
    RestValidation.checkResponse(request, body);
    RestValidation.checkEntity(request, body.id());
  }

  @Test
  @Authentication
  @NewPainting
  @DisplayName("Update painting content")
  void positiveUpdatePaintingWithContentTest(PaintingInfoResponse painting) {
    var request = PaintingProvider.getUpdatePaintingInfoRequestApi(painting)
        .set(Select.field(UpdatePaintingInfoRequest::content), RandomDataUtils.randomPhoto())
        .create();
    var response = client.updatePainting(request);

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    var body = response.body();
    RestValidation.checkResponse(request, body);
    RestValidation.checkEntity(request, body.id());
  }

  @Test
  @Authentication
  @NewPainting
  @DisplayName("Update painting artist id")
  void positiveUpdatePaintingWithArtistIdTest(PaintingInfoResponse painting) {
    var newArtist = ArtistProvider.getNewArtist();
    var request = PaintingProvider.getUpdatePaintingInfoRequestApi(painting)
        .set(Select.field(ArtistInfoRequest::id), newArtist.id())
        .create();
    var response = client.updatePainting(request);

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    var body = response.body();
    RestValidation.checkResponse(request, body);
    RestValidation.checkEntity(request, body.id());
  }

  @Test
  @Authentication
  @NewPainting
  @DisplayName("Update painting museum id")
  void positiveUpdatePaintingWithMuseumIdTest(PaintingInfoResponse painting) {
    var newMuseum = MuseumProvider.getNewMuseum();
    var request = PaintingProvider.getUpdatePaintingInfoRequestApi(painting)
        .set(Select.field(MuseumInfoRequest::id), newMuseum.id())
        .create();
    var response = client.updatePainting(request);

    RestValidation.responseMustHaveSuccessfulStatus(response);
    RestValidation.bodyMustBePresent(response);
    var body = response.body();
    RestValidation.checkResponse(request, body);
    RestValidation.checkEntity(request, body.id());
  }

  @Test
  @DisplayName("Attempt to update painting without auth")
  void negativeUpdatePaintingWithoutAuthTest() {
    var request = Instancio.create(model);
    var response = client.updatePainting(request);

    RestValidation.responseMustHaveUnauthorizedStatus(response);
    RestValidation.bodyMustBeEmpty(response);
  }

  @Test
  @Authentication
  @DisplayName("Attempt to update painting with non-existing id")
  void negativeUpdatePaintingWithNonExistentIdTest() {
    var request = Instancio.of(model)
        .set(Select.field(UpdatePaintingInfoRequest::id), UUID.randomUUID())
        .create();
    var response = client.updatePainting(request);

    RestValidation.responseMustHaveNotFoundStatus(response);
    RestValidation.bodyMustBeEmpty(response);
  }

  @Test
  @Authentication
  @NewPainting
  @DisplayName("Attempt to update painting with duplicate title")
  void negativeUpdatePaintingWithDuplicateTitleTest(PaintingInfoResponse existedPainting) {
    var request = Instancio.of(model)
        .set(Select.field(UpdatePaintingInfoRequest::title), existedPainting.title())
        .set(Select.field(ArtistInfoRequest::id), existedPainting.artist().id())
        .create();
    var response = client.updatePainting(request);

    RestValidation.responseMustHaveConflictStatus(response);
    String error = "Painting already exists: %s".formatted(existedPainting.title());
    RestValidation.bodyMustHaveTheError(response, error);
  }

  @ParameterizedTest
  @Authentication
  @NullAndEmptySource
  @DisplayName("Attempt to update painting with empty title")
  void negativeUpdatePaintingWithEmptyTitleTest(String title) {
    var request = Instancio.of(model)
        .set(Select.field(UpdatePaintingInfoRequest::title), title)
        .create();
    var response = client.updatePainting(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @ParameterizedTest
  @Authentication
  @ValueSource(ints = {3 - 1, 255 + 1})
  @DisplayName("Attempt to update painting with title length that is out of range")
  void negativeUpdatePaintingWithTitleLengthThatIsOutOfRangeTest(int length) {
    var request = Instancio.of(model)
        .assign(Assign.valueOf(UpdatePaintingInfoRequest::title)
            .generate(gen -> gen.string().length(256)))
        .create();
    var response = client.updatePainting(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @ParameterizedTest
  @Authentication
  @NullAndEmptySource
  @DisplayName("Attempt to update painting with empty description")
  void negativeUpdatePaintingWithEmptyDescriptionTest(String description) {
    var request = Instancio.of(model)
        .set(Select.field(UpdatePaintingInfoRequest::description), description)
        .create();
    var response = client.updatePainting(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @ParameterizedTest
  @Authentication
  @ValueSource(ints = {MIN_DESCRIPTION_LENGTH - 1, MAX_DESCRIPTION_LENGTH + 1})
  @DisplayName("Attempt to update painting with description length that is out of range")
  void negativeUpdatePaintingWithDescriptionLengthThatIsOutOfRangeTest(int length) {
    var request = Instancio.of(model)
        .assign(Assign.valueOf(UpdatePaintingInfoRequest::description)
            .generate(gen -> gen.string().length(length)))
        .create();
    var response = client.updatePainting(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @ParameterizedTest
  @Authentication
  @NullAndEmptySource
  @DisplayName("Attempt to update painting with empty content")
  void negativeUpdatePaintingWithEmptyContentTest(String content) {
    var request = Instancio.of(model)
        .set(Select.field(UpdatePaintingInfoRequest::content), content)
        .create();
    var response = client.updatePainting(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @Test
  @Authentication
  @DisplayName("Attempt to update painting with content exceeding the limit size")
  void negativeUpdatePaintingWithContentExceedingLimitSizeTest() {
    var request = Instancio.of(model)
        .assign(Assign.valueOf(UpdatePaintingInfoRequest::content)
            .generate(gen -> gen.string().length(MAX_PHOTO_SIZE + 1)))
        .create();
    var response = client.updatePainting(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveContentExceedLimitError(response);
  }

  @Test
  @Authentication
  @DisplayName("Attempt to update painting without artist info")
  void negativeUpdatePaintingWithoutArtistInfoTest() {
    var request = Instancio.of(model)
        .ignore(Select.field(UpdatePaintingInfoRequest::artist))
        .create();
    var response = client.updatePainting(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @Test
  @Authentication
  @DisplayName("Attempt to update painting with non-existent artist id")
  void negativeUpdatePaintingWithNonExistentArtistIdTest() {
    var request = Instancio.of(model)
        .set(Select.field(ArtistInfoRequest::id), UUID.randomUUID())
        .create();
    var response = client.updatePainting(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    String error = "Artist not found: %s".formatted(request.artist().id());
    RestValidation.bodyMustHaveTheError(response, error);
  }

  @Test
  @Authentication
  @DisplayName("Attempt to update painting without museum info")
  void negativeUpdatePaintingWithoutMuseumInfoTest() {
    var request = Instancio.of(model)
        .ignore(Select.field(UpdatePaintingInfoRequest::museum))
        .create();
    var response = client.updatePainting(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    RestValidation.bodyMustHaveBadRequestError(response);
  }

  @Test
  @Authentication
  @DisplayName("Attempt to update painting with non-existent museum id")
  void negativeUpdatePaintingWithNonExistentMuseumIdTest() {
    var request = Instancio.of(model)
        .set(Select.field(MuseumInfoRequest::id), UUID.randomUUID())
        .create();
    var response = client.updatePainting(request);

    RestValidation.responseMustHaveBadRequestStatus(response);
    String error = "Museum not found: %s".formatted(request.museum().id());
    RestValidation.bodyMustHaveTheError(response, error);
  }
}
