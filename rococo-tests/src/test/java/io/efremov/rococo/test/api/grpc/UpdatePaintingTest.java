package io.efremov.rococo.test.api.grpc;

import static io.efremov.rococo.config.Constants.MAX_DESCRIPTION_LENGTH;
import static io.efremov.rococo.config.Constants.MAX_PHOTO_SIZE;
import static io.efremov.rococo.config.Constants.MAX_TITLE_LENGTH;
import static io.efremov.rococo.config.Constants.MIN_DESCRIPTION_LENGTH;
import static io.efremov.rococo.config.Constants.MIN_TITLE_LENGTH;
import static io.efremov.rococo.config.Constants.MUTATION_API_TAG;

import io.efremov.rococo.api.validation.GrpcValidation;
import io.efremov.rococo.jupiter.annotation.AnyArtist;
import io.efremov.rococo.jupiter.annotation.AnyPainting;
import io.efremov.rococo.jupiter.annotation.NewMuseum;
import io.efremov.rococo.jupiter.annotation.NewPainting;
import io.efremov.rococo.model.ArtistInfoResponse;
import io.efremov.rococo.model.MuseumInfoResponse;
import io.efremov.rococo.model.PaintingInfoResponse;
import io.efremov.rococo.provider.PaintingProvider;
import io.efremov.rococo.util.RandomDataUtils;
import io.grpc.Status;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Tag(MUTATION_API_TAG)
@Epic("API")
@Feature("rococo-painting")
@Story("Update a painting")
class UpdatePaintingTest extends BaseGrpcTest {

  private static final PaintingInfoResponse prototype = PaintingProvider.getAnyPainting();

  @Test
  @NewPainting
  @DisplayName("Update painting title")
  void positiveUpdatePaintingWithTitleTest(PaintingInfoResponse painting) {
    var request = PaintingProvider.getUpdatePaintingRequest(painting).toBuilder()
        .setTitle(RandomDataUtils.randomTitle())
        .build();
    var response = PAINTING_BLOCKING_STUB.updatePainting(request);

    GrpcValidation.checkResponse(request, response);
    GrpcValidation.checkEntity(request, response.getId());
  }

  @Test
  @NewPainting
  @DisplayName("Update painting description")
  void positiveUpdatePaintingWithDescriptionTest(PaintingInfoResponse painting) {
    var request = PaintingProvider.getUpdatePaintingRequest(painting).toBuilder()
        .setDescription(RandomDataUtils.randomParagraph())
        .build();
    var response = PAINTING_BLOCKING_STUB.updatePainting(request);

    GrpcValidation.checkResponse(request, response);
    GrpcValidation.checkEntity(request, response.getId());
  }

  @Test
  @NewPainting
  @DisplayName("Update painting content")
  void positiveUpdatePaintingWithContentTest(PaintingInfoResponse painting) {
    var request = PaintingProvider.getUpdatePaintingRequest(painting).toBuilder()
        .setContent(RandomDataUtils.randomPhoto())
        .build();
    var response = PAINTING_BLOCKING_STUB.updatePainting(request);

    GrpcValidation.checkResponse(request, response);
    GrpcValidation.checkEntity(request, response.getId());
  }

  @Test
  @NewPainting
  @AnyArtist
  @DisplayName("Update painting artist id")
  void positiveUpdatePaintingWithArtistIdTest(PaintingInfoResponse painting, ArtistInfoResponse artist) {
    var request = PaintingProvider.getUpdatePaintingRequest(painting).toBuilder()
        .setArtistId(artist.id().toString())
        .build();
    var response = PAINTING_BLOCKING_STUB.updatePainting(request);

    GrpcValidation.checkResponse(request, response);
    GrpcValidation.checkEntity(request, response.getId());
  }

  @Test
  @NewPainting
  @NewMuseum
  @DisplayName("Update painting museum id")
  void positiveUpdatePaintingWithMuseumIdTest(PaintingInfoResponse painting, MuseumInfoResponse museum) {
    var request = PaintingProvider.getUpdatePaintingRequest(painting).toBuilder()
        .setMuseumId(museum.id().toString())
        .build();
    var response = PAINTING_BLOCKING_STUB.updatePainting(request);

    GrpcValidation.checkResponse(request, response);
    GrpcValidation.checkEntity(request, response.getId());
  }

  @Test
  @DisplayName("Attempt to update painting with non-existent id")
  void negativeUpdatePaintingWithNonExistentIdTest() {
    String nonExistentId = UUID.randomUUID().toString();
    var request = PaintingProvider.getUpdatePaintingRequest(prototype).toBuilder()
        .setId(nonExistentId)
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> PAINTING_BLOCKING_STUB.updatePainting(request),
        Status.Code.NOT_FOUND,
        "Painting not found: %s".formatted(nonExistentId)
    );
  }

  @Test
  @AnyPainting
  @DisplayName("Attempt to update painting with duplicate title")
  void negativeUpdatePaintingWithDuplicateTitleTest(PaintingInfoResponse existedPainting) {
    var request = PaintingProvider.getUpdatePaintingRequest(prototype).toBuilder()
        .setTitle(existedPainting.title())
        .setArtistId(existedPainting.artist().id().toString())
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> PAINTING_BLOCKING_STUB.updatePainting(request),
        Status.Code.ALREADY_EXISTS,
        "Painting already exists: %s".formatted(existedPainting.title())
    );
  }

  @Test
  @DisplayName("Attempt to update painting without title")
  void negativeUpdatePaintingWithoutTitleTest() {
    var request = PaintingProvider.getUpdatePaintingRequest(prototype).toBuilder()
        .clearTitle()
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> PAINTING_BLOCKING_STUB.updatePainting(request),
        Status.Code.INVALID_ARGUMENT,
        "title: must not be blank"
    );
  }

  @Test
  @DisplayName("Attempt to update painting with empty title")
  void negativeUpdatePaintingWithEmptyTitleTest() {
    var request = PaintingProvider.getUpdatePaintingRequest(prototype).toBuilder()
        .setTitle("")
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> PAINTING_BLOCKING_STUB.updatePainting(request),
        Status.Code.INVALID_ARGUMENT,
        "title: must not be blank"
    );
  }

  @ParameterizedTest
  @ValueSource(ints = {MIN_TITLE_LENGTH - 1, MAX_TITLE_LENGTH + 1})
  @DisplayName("Attempt to update painting with title length that is out of range")
  void negativeUpdatePaintingWithTitleLengthThatIsOutOfRangeTest(int length) {
    var request = PaintingProvider.getUpdatePaintingRequest(prototype).toBuilder()
        .setTitle(RandomDataUtils.GEN.string().length(length).get())
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> PAINTING_BLOCKING_STUB.updatePainting(request),
        Status.Code.INVALID_ARGUMENT,
        "title: must be from %d to %d characters".formatted(MIN_TITLE_LENGTH, MAX_TITLE_LENGTH)
    );
  }

  @Test
  @DisplayName("Attempt to update painting without description")
  void negativeUpdatePaintingWithoutDescriptionTest() {
    var request = PaintingProvider.getUpdatePaintingRequest(prototype).toBuilder()
        .clearDescription()
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> PAINTING_BLOCKING_STUB.updatePainting(request),
        Status.Code.INVALID_ARGUMENT,
        "description: must not be blank"
    );
  }

  @Test
  @DisplayName("Attempt to update painting with empty description")
  void negativeUpdatePaintingWithEmptyDescriptionTest() {
    var request = PaintingProvider.getUpdatePaintingRequest(prototype).toBuilder()
        .setDescription("")
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> PAINTING_BLOCKING_STUB.updatePainting(request),
        Status.Code.INVALID_ARGUMENT,
        "description: must not be blank"
    );
  }

  @ParameterizedTest
  @ValueSource(ints = {MIN_DESCRIPTION_LENGTH - 1, MAX_DESCRIPTION_LENGTH + 1})
  @DisplayName("Attempt to update painting with description length that is out of range")
  void negativeUpdatePaintingWithDescriptionLengthThatIsOutOfRangeTest(int length) {
    var request = PaintingProvider.getUpdatePaintingRequest(prototype).toBuilder()
        .setDescription(RandomDataUtils.GEN.string().length(length).get())
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> PAINTING_BLOCKING_STUB.updatePainting(request),
        Status.Code.INVALID_ARGUMENT,
        "description: must be from %d to %d characters".formatted(MIN_DESCRIPTION_LENGTH, MAX_DESCRIPTION_LENGTH)
    );
  }

  @Test
  @DisplayName("Attempt to update painting without content")
  void negativeUpdatePaintingWithoutContentTest() {
    var request = PaintingProvider.getUpdatePaintingRequest(prototype).toBuilder()
        .clearContent()
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> PAINTING_BLOCKING_STUB.updatePainting(request),
        Status.Code.INVALID_ARGUMENT,
        "content: must not be blank"
    );
  }

  @Test
  @DisplayName("Attempt to update painting with empty content")
  void negativeUpdatePaintingWithEmptyContentTest() {
    var request = PaintingProvider.getUpdatePaintingRequest(prototype).toBuilder()
        .setContent("")
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> PAINTING_BLOCKING_STUB.updatePainting(request),
        Status.Code.INVALID_ARGUMENT,
        "content: must not be blank"
    );
  }

  @Test
  @DisplayName("Attempt to update painting with content exceeding the limit size")
  void negativeUpdatePaintingWithContentExceedingLimitSizeTest() {
    var request = PaintingProvider.getUpdatePaintingRequest(prototype).toBuilder()
        .setContent(RandomDataUtils.GEN.string().length(MAX_PHOTO_SIZE + 1).get())
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> PAINTING_BLOCKING_STUB.updatePainting(request),
        Status.Code.INVALID_ARGUMENT,
        "content: must not exceed 1 MB"
    );
  }

}
