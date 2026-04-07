package io.efremov.rococo.test.api.grpc;

import static io.efremov.rococo.config.Constants.MAX_DESCRIPTION_LENGTH;
import static io.efremov.rococo.config.Constants.MAX_PHOTO_SIZE;
import static io.efremov.rococo.config.Constants.MAX_TITLE_LENGTH;
import static io.efremov.rococo.config.Constants.MIN_DESCRIPTION_LENGTH;
import static io.efremov.rococo.config.Constants.MIN_TITLE_LENGTH;
import static io.efremov.rococo.config.Constants.MUTATION_API_TAG;
import static io.efremov.rococo.util.RandomDataUtils.GEN;

import io.efremov.rococo.api.validation.GrpcValidation;
import io.efremov.rococo.grpc.CreatePaintingRequest;
import io.efremov.rococo.jupiter.annotation.NewPainting;
import io.efremov.rococo.model.PaintingInfoResponse;
import io.efremov.rococo.provider.PaintingProvider;
import io.grpc.Status;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Tag(MUTATION_API_TAG)
@Epic("API")
@Feature("rococo-painting")
@Story("Create a painting")
class CreatePaintingTest extends BaseGrpcTest {

  private final CreatePaintingRequest prototype = PaintingProvider.getCreatePaintingRequest();

  @Test
  @DisplayName("Create painting")
  void positiveCreatePaintingTest() {
    var response = PAINTING_BLOCKING_STUB.createPainting(prototype);

    GrpcValidation.checkResponse(prototype, response);
    GrpcValidation.checkEntity(prototype, response.getId());
  }

  @Test
  @NewPainting
  @DisplayName("Attempt to create painting with duplicate title")
  void negativeCreatePaintingWithDuplicateTitleTest(PaintingInfoResponse existedPainting) {
    var request = CreatePaintingRequest.newBuilder(prototype)
        .setTitle(existedPainting.title())
        .setArtistId(existedPainting.artist().id().toString())
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> PAINTING_BLOCKING_STUB.createPainting(request),
        Status.Code.ALREADY_EXISTS,
        "Painting already exists: %s".formatted(existedPainting.title())
    );
  }

  @Test
  @DisplayName("Attempt to create painting without title")
  void negativeCreatePaintingWithoutTitleTest() {
    var request = CreatePaintingRequest.newBuilder(prototype)
        .clearTitle()
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> PAINTING_BLOCKING_STUB.createPainting(request),
        Status.Code.INVALID_ARGUMENT,
        "title: must not be blank"
    );
  }

  @Test
  @DisplayName("Attempt to create painting with empty title")
  void negativeCreatePaintingWithEmptyTitleTest() {
    var request = CreatePaintingRequest.newBuilder(prototype)
        .setTitle(StringUtils.EMPTY)
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> PAINTING_BLOCKING_STUB.createPainting(request),
        Status.Code.INVALID_ARGUMENT,
        "title: must not be blank"
    );
  }

  @ParameterizedTest
  @ValueSource(ints = {MIN_TITLE_LENGTH - 1, MAX_TITLE_LENGTH + 1})
  @DisplayName("Attempt to create painting with title length that is out of range")
  void negativeCreatePaintingWithTitleLengthThatIsOutOfRangeTest(int length) {
    var request = CreatePaintingRequest.newBuilder(prototype)
        .setTitle(GEN.string().length(length).get())
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> PAINTING_BLOCKING_STUB.createPainting(request),
        Status.Code.INVALID_ARGUMENT,
        "title: must be from 3 to 255 characters"
    );
  }

  @Test
  @DisplayName("Attempt to create painting without description")
  void negativeCreatePaintingWithoutDescriptionTest() {
    var request = CreatePaintingRequest.newBuilder(prototype)
        .clearDescription()
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> PAINTING_BLOCKING_STUB.createPainting(request),
        Status.Code.INVALID_ARGUMENT,
        "description: must not be blank"
    );
  }

  @Test
  @DisplayName("Attempt to create painting with empty description")
  void negativeCreatePaintingWithEmptyDescriptionTest() {
    var request = CreatePaintingRequest.newBuilder(prototype)
        .setDescription(StringUtils.EMPTY)
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> PAINTING_BLOCKING_STUB.createPainting(request),
        Status.Code.INVALID_ARGUMENT,
        "description: must not be blank"
    );
  }

  @ParameterizedTest
  @ValueSource(ints = {MIN_DESCRIPTION_LENGTH - 1, MAX_DESCRIPTION_LENGTH + 1})
  @DisplayName("Attempt to create painting with description length that is out of range")
  void negativeCreatePaintingWithDescriptionLengthThatIsOutOfRangeTest(int length) {
    var request = CreatePaintingRequest.newBuilder(prototype)
        .setDescription(GEN.string().length(length).get())
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> PAINTING_BLOCKING_STUB.createPainting(request),
        Status.Code.INVALID_ARGUMENT,
        "description: must be from 10 to 1000 characters"
    );
  }

  @Test
  @DisplayName("Attempt to create painting without content")
  void negativeCreatePaintingWithoutContentTest() {
    var request = CreatePaintingRequest.newBuilder(prototype)
        .clearContent()
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> PAINTING_BLOCKING_STUB.createPainting(request),
        Status.Code.INVALID_ARGUMENT,
        "content: must not be blank"
    );
  }

  @Test
  @DisplayName("Attempt to create painting with empty content")
  void negativeCreatePaintingWithEmptyContentTest() {
    var request = CreatePaintingRequest.newBuilder(prototype)
        .setContent(StringUtils.EMPTY)
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> PAINTING_BLOCKING_STUB.createPainting(request),
        Status.Code.INVALID_ARGUMENT,
        "content: must not be blank"
    );
  }

  @Test
  @DisplayName("Attempt to create painting with content exceeding the limit size")
  void negativeCreatePaintingWithContentExceedingLimitSizeTest() {
    var request = CreatePaintingRequest.newBuilder(prototype)
        .setContent(GEN.string().length(MAX_PHOTO_SIZE + 1).get())
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> PAINTING_BLOCKING_STUB.createPainting(request),
        Status.Code.INVALID_ARGUMENT,
        "content: must not exceed 1 MB"
    );
  }

}
