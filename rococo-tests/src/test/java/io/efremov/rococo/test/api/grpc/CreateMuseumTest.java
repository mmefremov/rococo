package io.efremov.rococo.test.api.grpc;

import static io.efremov.rococo.config.Constants.MAX_DESCRIPTION_LENGTH;
import static io.efremov.rococo.config.Constants.MAX_PHOTO_SIZE;
import static io.efremov.rococo.config.Constants.MAX_TITLE_LENGTH;
import static io.efremov.rococo.config.Constants.MIN_DESCRIPTION_LENGTH;
import static io.efremov.rococo.config.Constants.MIN_TITLE_LENGTH;
import static io.efremov.rococo.config.Constants.MUTATION_API_TAG;
import static io.efremov.rococo.util.RandomDataUtils.GEN;

import io.efremov.rococo.api.validation.GrpcValidation;
import io.efremov.rococo.grpc.CreateMuseumRequest;
import io.efremov.rococo.jupiter.annotation.NewMuseum;
import io.efremov.rococo.model.MuseumInfoResponse;
import io.efremov.rococo.provider.MuseumProvider;
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
@Feature("rococo-museum")
@Story("Create a museum")
class CreateMuseumTest extends BaseGrpcTest {

  private final CreateMuseumRequest prototype = MuseumProvider.getCreateMuseumRequest();

  @Test
  @DisplayName("Create museum")
  void positiveCreateMuseumTest() {
    var response = MUSEUM_BLOCKING_STUB.createMuseum(prototype);

    GrpcValidation.checkResponse(prototype, response);
    GrpcValidation.checkEntity(prototype, response.getId());
  }

  @Test
  @NewMuseum
  @DisplayName("Attempt to create museum with duplicate title")
  void negativeCreateMuseumWithDuplicateTitleTest(MuseumInfoResponse existedMuseum) {
    var request = CreateMuseumRequest.newBuilder(prototype)
        .setTitle(existedMuseum.title())
        .setCity(existedMuseum.geo().city())
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> MUSEUM_BLOCKING_STUB.createMuseum(request),
        Status.Code.ALREADY_EXISTS,
        "Museum title already taken: %s".formatted(existedMuseum.title())
    );
  }

  @Test
  @DisplayName("Attempt to create museum without title")
  void negativeCreateMuseumWithoutTitleTest() {
    var request = CreateMuseumRequest.newBuilder(prototype).clearTitle().build();
    GrpcValidation.checkStatusRuntimeException(
        () -> MUSEUM_BLOCKING_STUB.createMuseum(request),
        Status.Code.INVALID_ARGUMENT,
        "title: must not be blank"
    );
  }

  @Test
  @DisplayName("Attempt to create museum with empty title")
  void negativeCreateMuseumWithEmptyTitleTest() {
    var request = CreateMuseumRequest.newBuilder(prototype)
        .setTitle(StringUtils.EMPTY)
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> MUSEUM_BLOCKING_STUB.createMuseum(request),
        Status.Code.INVALID_ARGUMENT,
        "title: must not be blank"
    );
  }

  @ParameterizedTest
  @ValueSource(ints = {MIN_TITLE_LENGTH - 1, MAX_TITLE_LENGTH + 1})
  @DisplayName("Attempt to create museum with title length that is out of range")
  void negativeCreateMuseumWithTitleLengthThatIsOutOfRangeTest(int length) {
    var request = CreateMuseumRequest.newBuilder(prototype)
        .setTitle(GEN.string().length(length).get())
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> MUSEUM_BLOCKING_STUB.createMuseum(request),
        Status.Code.INVALID_ARGUMENT,
        "title: must be from 3 to 255 characters"
    );
  }

  @Test
  @DisplayName("Attempt to create museum without description")
  void negativeCreateMuseumWithoutDescriptionTest() {
    var request = CreateMuseumRequest.newBuilder(prototype)
        .clearDescription().build();
    GrpcValidation.checkStatusRuntimeException(
        () -> MUSEUM_BLOCKING_STUB.createMuseum(request),
        Status.Code.INVALID_ARGUMENT,
        "description: must not be blank"
    );
  }

  @Test
  @DisplayName("Attempt to create museum with empty description")
  void negativeCreateMuseumWithEmptyDescriptionTest() {
    var request = CreateMuseumRequest.newBuilder(prototype)
        .setDescription(StringUtils.EMPTY)
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> MUSEUM_BLOCKING_STUB.createMuseum(request),
        Status.Code.INVALID_ARGUMENT,
        "description: must not be blank"
    );
  }

  @ParameterizedTest
  @ValueSource(ints = {MIN_DESCRIPTION_LENGTH - 1, MAX_DESCRIPTION_LENGTH + 1})
  @DisplayName("Attempt to create museum with description length that is out of range")
  void negativeCreateMuseumWithDescriptionLengthThatIsOutOfRangeTest(int length) {
    var request = CreateMuseumRequest.newBuilder(prototype)
        .setDescription(GEN.string().length(length).get())
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> MUSEUM_BLOCKING_STUB.createMuseum(request),
        Status.Code.INVALID_ARGUMENT,
        "description: must be from 10 to 1000 characters"
    );
  }

  @Test
  @DisplayName("Attempt to create museum without city")
  void negativeCreateMuseumWithoutCityTest() {
    var request = CreateMuseumRequest.newBuilder(prototype).clearCity().build();
    GrpcValidation.checkStatusRuntimeException(
        () -> MUSEUM_BLOCKING_STUB.createMuseum(request),
        Status.Code.INVALID_ARGUMENT,
        "city: must not be blank"
    );
  }

  @Test
  @DisplayName("Attempt to create museum with empty city")
  void negativeCreateMuseumWithEmptyCityTest() {
    var request = CreateMuseumRequest.newBuilder(prototype)
        .setCity(StringUtils.EMPTY)
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> MUSEUM_BLOCKING_STUB.createMuseum(request),
        Status.Code.INVALID_ARGUMENT,
        "city: must not be blank"
    );
  }

  @ParameterizedTest
  @ValueSource(ints = {MIN_TITLE_LENGTH - 1, MAX_TITLE_LENGTH + 1})
  @DisplayName("Attempt to create museum with city length that is out of range")
  void negativeCreateMuseumWithCityLengthThatIsOutOfRangeTest(int length) {
    var request = CreateMuseumRequest.newBuilder(prototype)
        .setCity(GEN.string().length(length).get())
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> MUSEUM_BLOCKING_STUB.createMuseum(request),
        Status.Code.INVALID_ARGUMENT,
        "city: must be from 3 to 255 characters"
    );
  }

  @Test
  @DisplayName("Attempt to create museum without photo")
  void negativeCreateMuseumWithoutPhotoTest() {
    var request = CreateMuseumRequest.newBuilder(prototype).clearPhoto().build();
    GrpcValidation.checkStatusRuntimeException(
        () -> MUSEUM_BLOCKING_STUB.createMuseum(request),
        Status.Code.INVALID_ARGUMENT,
        "photo: must not be blank"
    );
  }

  @Test
  @DisplayName("Attempt to create museum with empty photo")
  void negativeCreateMuseumWithEmptyPhotoTest() {
    var request = CreateMuseumRequest.newBuilder(prototype)
        .setPhoto(StringUtils.EMPTY)
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> MUSEUM_BLOCKING_STUB.createMuseum(request),
        Status.Code.INVALID_ARGUMENT,
        "photo: must not be blank"
    );
  }

  @Test
  @DisplayName("Attempt to create museum with photo exceeding the limit size")
  void negativeCreateMuseumWithPhotoExceedingLimitSizeTest() {
    var request = CreateMuseumRequest.newBuilder(prototype)
        .setPhoto(GEN.string().length(MAX_PHOTO_SIZE + 1).get())
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> MUSEUM_BLOCKING_STUB.createMuseum(request),
        Status.Code.INVALID_ARGUMENT,
        "photo: must not exceed 1 MB"
    );
  }

}
