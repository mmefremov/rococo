package io.efremov.rococo.test.api.grpc;

import static io.efremov.rococo.config.Constants.MAX_DESCRIPTION_LENGTH;
import static io.efremov.rococo.config.Constants.MAX_PHOTO_SIZE;
import static io.efremov.rococo.config.Constants.MAX_TITLE_LENGTH;
import static io.efremov.rococo.config.Constants.MIN_DESCRIPTION_LENGTH;
import static io.efremov.rococo.config.Constants.MIN_TITLE_LENGTH;
import static io.efremov.rococo.config.Constants.MUTATION_API_TAG;

import io.efremov.rococo.api.validation.GrpcValidation;
import io.efremov.rococo.data.repository.CountryRepository;
import io.efremov.rococo.jupiter.annotation.AnyMuseum;
import io.efremov.rococo.jupiter.annotation.NewMuseum;
import io.efremov.rococo.model.MuseumInfoResponse;
import io.efremov.rococo.provider.MuseumProvider;
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
@Feature("rococo-museum")
@Story("Update a museum")
class UpdateMuseumTest extends BaseGrpcTest {

  private static final MuseumInfoResponse prototype = MuseumProvider.getAnyMuseum();


  @Test
  @NewMuseum
  @DisplayName("Update museum title")
  void positiveUpdateMuseumWithTitleTest(MuseumInfoResponse museum) {
    var request = MuseumProvider.getUpdateMuseumRequest(museum).toBuilder()
        .setTitle(RandomDataUtils.randomTitle())
        .build();
    var response = MUSEUM_BLOCKING_STUB.updateMuseum(request);

    GrpcValidation.checkResponse(request, response);
    GrpcValidation.checkEntity(request, response.getId());
  }

  @Test
  @NewMuseum
  @DisplayName("Update museum description")
  void positiveUpdateMuseumWithDescriptionTest(MuseumInfoResponse museum) {
    var request = MuseumProvider.getUpdateMuseumRequest(museum).toBuilder()
        .setDescription(RandomDataUtils.randomParagraph())
        .build();
    var response = MUSEUM_BLOCKING_STUB.updateMuseum(request);

    GrpcValidation.checkResponse(request, response);
    GrpcValidation.checkEntity(request, response.getId());
  }

  @Test
  @NewMuseum
  @DisplayName("Update museum photo")
  void positiveUpdateMuseumWithPhotoTest(MuseumInfoResponse museum) {
    var request = MuseumProvider.getUpdateMuseumRequest(museum).toBuilder()
        .setPhoto(RandomDataUtils.randomPhoto())
        .build();
    var response = MUSEUM_BLOCKING_STUB.updateMuseum(request);

    GrpcValidation.checkResponse(request, response);
    GrpcValidation.checkEntity(request, response.getId());
  }

  @Test
  @NewMuseum
  @DisplayName("Update museum city")
  void positiveUpdateMuseumWithCityTest(MuseumInfoResponse museum) {
    var request = MuseumProvider.getUpdateMuseumRequest(museum).toBuilder()
        .setCity(RandomDataUtils.randomCity())
        .build();
    var response = MUSEUM_BLOCKING_STUB.updateMuseum(request);

    GrpcValidation.checkResponse(request, response);
    GrpcValidation.checkEntity(request, response.getId());
  }

  @Test
  @NewMuseum
  @DisplayName("Update museum country id")
  void positiveUpdateMuseumWithCountryIdTest(MuseumInfoResponse museum) {
    var request = MuseumProvider.getUpdateMuseumRequest(museum).toBuilder()
        .setCountryId(new CountryRepository().findAny().getId().toString())
        .build();
    var response = MUSEUM_BLOCKING_STUB.updateMuseum(request);

    GrpcValidation.checkResponse(request, response);
    GrpcValidation.checkEntity(request, response.getId());
  }

  @Test
  @DisplayName("Attempt to update museum with non-existent id")
  void negativeUpdateMuseumWithNonExistentIdTest() {
    String nonExistentId = UUID.randomUUID().toString();
    var request = MuseumProvider.getUpdateMuseumRequest(prototype).toBuilder()
        .setId(nonExistentId)
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> MUSEUM_BLOCKING_STUB.updateMuseum(request),
        Status.Code.NOT_FOUND,
        "Museum not found: %s".formatted(nonExistentId)
    );
  }

  @Test
  @AnyMuseum
  @DisplayName("Attempt to update museum with duplicate title")
  void negativeUpdateMuseumWithDuplicateTitleTest(MuseumInfoResponse existedMuseum) {
    var request = MuseumProvider.getUpdateMuseumRequest(prototype).toBuilder()
        .setTitle(existedMuseum.title())
        .setCity(existedMuseum.geo().city())
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> MUSEUM_BLOCKING_STUB.updateMuseum(request),
        Status.Code.ALREADY_EXISTS,
        "Museum title already taken: %s".formatted(existedMuseum.title())
    );
  }

  @Test
  @DisplayName("Attempt to update museum without title")
  void negativeUpdateMuseumWithoutTitleTest() {
    var request = MuseumProvider.getUpdateMuseumRequest(prototype).toBuilder()
        .clearTitle()
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> MUSEUM_BLOCKING_STUB.updateMuseum(request),
        Status.Code.INVALID_ARGUMENT,
        "title: must not be blank"
    );
  }

  @Test
  @DisplayName("Attempt to update museum with empty title")
  void negativeUpdateMuseumWithEmptyTitleTest() {
    var request = MuseumProvider.getUpdateMuseumRequest(prototype).toBuilder()
        .setTitle("")
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> MUSEUM_BLOCKING_STUB.updateMuseum(request),
        Status.Code.INVALID_ARGUMENT,
        "title: must not be blank"
    );
  }

  @ParameterizedTest
  @ValueSource(ints = {MIN_TITLE_LENGTH - 1, MAX_TITLE_LENGTH + 1})
  @DisplayName("Attempt to update museum with title length that is out of range")
  void negativeUpdateMuseumWithTitleLengthThatIsOutOfRangeTest(int length) {
    var request = MuseumProvider.getUpdateMuseumRequest(prototype).toBuilder()
        .setTitle(RandomDataUtils.GEN.string().length(length).get())
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> MUSEUM_BLOCKING_STUB.updateMuseum(request),
        Status.Code.INVALID_ARGUMENT,
        "title: must be from %d to %d characters".formatted(MIN_TITLE_LENGTH, MAX_TITLE_LENGTH)
    );
  }

  @Test
  @DisplayName("Attempt to update museum without description")
  void negativeUpdateMuseumWithoutDescriptionTest() {
    var request = MuseumProvider.getUpdateMuseumRequest(prototype).toBuilder()
        .clearDescription()
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> MUSEUM_BLOCKING_STUB.updateMuseum(request),
        Status.Code.INVALID_ARGUMENT,
        "description: must not be blank"
    );
  }

  @Test
  @DisplayName("Attempt to update museum with empty description")
  void negativeUpdateMuseumWithEmptyDescriptionTest() {
    var created = MUSEUM_BLOCKING_STUB.createMuseum(MuseumProvider.getCreateMuseumRequest());
    var request = MuseumProvider.getUpdateMuseumRequest(prototype).toBuilder()
        .setDescription("")
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> MUSEUM_BLOCKING_STUB.updateMuseum(request),
        Status.Code.INVALID_ARGUMENT,
        "description: must not be blank"
    );
  }

  @ParameterizedTest
  @ValueSource(ints = {MIN_DESCRIPTION_LENGTH - 1, MAX_DESCRIPTION_LENGTH + 1})
  @DisplayName("Attempt to update museum with description length that is out of range")
  void negativeUpdateMuseumWithDescriptionLengthThatIsOutOfRangeTest(int length) {
    var request = MuseumProvider.getUpdateMuseumRequest(prototype).toBuilder()
        .setDescription(RandomDataUtils.GEN.string().length(length).get())
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> MUSEUM_BLOCKING_STUB.updateMuseum(request),
        Status.Code.INVALID_ARGUMENT,
        "description: must be from %d to %d characters".formatted(MIN_DESCRIPTION_LENGTH, MAX_DESCRIPTION_LENGTH)
    );
  }

  @Test
  @DisplayName("Attempt to update museum without city")
  void negativeUpdateMuseumWithoutCityTest() {
    var request = MuseumProvider.getUpdateMuseumRequest(prototype).toBuilder()
        .clearCity()
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> MUSEUM_BLOCKING_STUB.updateMuseum(request),
        Status.Code.INVALID_ARGUMENT,
        "city: must not be blank"
    );
  }

  @Test
  @DisplayName("Attempt to update museum with empty city")
  void negativeUpdateMuseumWithEmptyCityTest() {
    var request = MuseumProvider.getUpdateMuseumRequest(prototype).toBuilder()
        .setCity("")
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> MUSEUM_BLOCKING_STUB.updateMuseum(request),
        Status.Code.INVALID_ARGUMENT,
        "city: must not be blank"
    );
  }

  @ParameterizedTest
  @ValueSource(ints = {MIN_TITLE_LENGTH - 1, MAX_TITLE_LENGTH + 1})
  @DisplayName("Attempt to update museum with city length that is out of range")
  void negativeUpdateMuseumWithCityLengthThatIsOutOfRangeTest(int length) {
    var request = MuseumProvider.getUpdateMuseumRequest(prototype).toBuilder()
        .setCity(RandomDataUtils.GEN.string().length(length).get())
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> MUSEUM_BLOCKING_STUB.updateMuseum(request),
        Status.Code.INVALID_ARGUMENT,
        "city: must be from %d to %d characters".formatted(MIN_TITLE_LENGTH, MAX_TITLE_LENGTH)
    );
  }

  @Test
  @DisplayName("Attempt to update museum without photo")
  void negativeUpdateMuseumWithoutPhotoTest() {
    var request = MuseumProvider.getUpdateMuseumRequest(prototype).toBuilder()
        .clearPhoto()
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> MUSEUM_BLOCKING_STUB.updateMuseum(request),
        Status.Code.INVALID_ARGUMENT,
        "photo: must not be blank"
    );
  }

  @Test
  @DisplayName("Attempt to update museum with empty photo")
  void negativeUpdateMuseumWithEmptyPhotoTest() {
    var request = MuseumProvider.getUpdateMuseumRequest(prototype).toBuilder()
        .setPhoto("")
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> MUSEUM_BLOCKING_STUB.updateMuseum(request),
        Status.Code.INVALID_ARGUMENT,
        "photo: must not be blank"
    );
  }

  @Test
  @DisplayName("Attempt to update museum with photo exceeding the limit size")
  void negativeUpdateMuseumWithPhotoExceedingLimitSizeTest() {
    var request = MuseumProvider.getUpdateMuseumRequest(prototype).toBuilder()
        .setPhoto(RandomDataUtils.GEN.string().length(MAX_PHOTO_SIZE + 1).get())
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> MUSEUM_BLOCKING_STUB.updateMuseum(request),
        Status.Code.INVALID_ARGUMENT,
        "photo: must not exceed 1 MB"
    );
  }

}
