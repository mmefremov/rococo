package io.efremov.rococo.test.api.grpc;

import static io.efremov.rococo.config.Constants.MAX_PARAGRAPH_LENGTH;
import static io.efremov.rococo.config.Constants.MAX_PHOTO_SIZE;
import static io.efremov.rococo.config.Constants.MAX_TITLE_LENGTH;
import static io.efremov.rococo.config.Constants.MIN_PARAGRAPH_LENGTH;
import static io.efremov.rococo.config.Constants.MIN_TITLE_LENGTH;
import static io.efremov.rococo.config.Constants.MUTATION_API_TAG;
import static io.efremov.rococo.util.RandomDataUtils.GEN;

import io.efremov.rococo.api.validation.GrpcValidation;
import io.efremov.rococo.grpc.CreateArtistRequest;
import io.efremov.rococo.jupiter.annotation.NewArtist;
import io.efremov.rococo.model.ArtistInfoResponse;
import io.efremov.rococo.provider.ArtistProvider;
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
@Feature("rococo-artist")
@Story("Create an artist")
class CreateArtistTest extends BaseGrpcTest {

  private final CreateArtistRequest prototype = ArtistProvider.getCreateArtistRequest();

  @Test
  @DisplayName("Create artist")
  void positiveCreateArtistTest() {
    var response = ARTIST_BLOCKING_STUB.createArtist(prototype);

    GrpcValidation.checkResponse(prototype, response);
    GrpcValidation.checkEntity(prototype, response.getId());
  }

  @Test
  @NewArtist
  @DisplayName("Attempt to create artist with duplicate name")
  void negativeCreateArtistWithDuplicateNameTest(ArtistInfoResponse existedArtist) {
    var request = CreateArtistRequest.newBuilder(prototype)
        .setName(existedArtist.name())
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> ARTIST_BLOCKING_STUB.createArtist(request),
        Status.Code.ALREADY_EXISTS,
        "Artist already exists: %s".formatted(existedArtist.name())
    );
  }

  @Test
  @DisplayName("Attempt to create artist without name")
  void negativeCreateArtistWithoutNameTest() {
    var request = CreateArtistRequest.newBuilder(prototype).clearName().build();
    GrpcValidation.checkStatusRuntimeException(
        () -> ARTIST_BLOCKING_STUB.createArtist(request),
        Status.Code.INVALID_ARGUMENT,
        "name: must not be blank"
    );
  }

  @Test
  @DisplayName("Attempt to create artist with empty name")
  void negativeCreateArtistWithEmptyNameTest() {
    var request = CreateArtistRequest.newBuilder(prototype)
        .setName(StringUtils.EMPTY)
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> ARTIST_BLOCKING_STUB.createArtist(request),
        Status.Code.INVALID_ARGUMENT,
        "name: must not be blank"
    );
  }

  @ParameterizedTest
  @ValueSource(ints = {MIN_TITLE_LENGTH - 1, MAX_TITLE_LENGTH + 1})
  @DisplayName("Attempt to create artist with name length that is out of range")
  void negativeCreateArtistWithNameLengthThatIsOutOfRangeTest(int length) {
    var request = CreateArtistRequest.newBuilder(prototype)
        .setName(GEN.string().length(length).get())
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> ARTIST_BLOCKING_STUB.createArtist(request),
        Status.Code.INVALID_ARGUMENT,
        "name: must be from 3 to 255 characters"
    );
  }

  @Test
  @DisplayName("Attempt to create artist without biography")
  void negativeCreateArtistWithoutBiographyTest() {
    var request = CreateArtistRequest.newBuilder(prototype).clearBiography().build();
    GrpcValidation.checkStatusRuntimeException(
        () -> ARTIST_BLOCKING_STUB.createArtist(request),
        Status.Code.INVALID_ARGUMENT,
        "biography: must not be blank"
    );
  }

  @Test
  @DisplayName("Attempt to create artist with empty biography")
  void negativeCreateArtistWithEmptyBiographyTest() {
    var request = CreateArtistRequest.newBuilder(prototype)
        .setBiography(StringUtils.EMPTY)
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> ARTIST_BLOCKING_STUB.createArtist(request),
        Status.Code.INVALID_ARGUMENT,
        "biography: must not be blank"
    );
  }

  @ParameterizedTest
  @ValueSource(ints = {MIN_PARAGRAPH_LENGTH - 1, MAX_PARAGRAPH_LENGTH + 1})
  @DisplayName("Attempt to create artist with biography length that is out of range")
  void negativeCreateArtistWithBiographyLengthThatIsOutOfRangeTest(int length) {
    var request = CreateArtistRequest.newBuilder(prototype)
        .setBiography(GEN.string().length(length).get())
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> ARTIST_BLOCKING_STUB.createArtist(request),
        Status.Code.INVALID_ARGUMENT,
        "biography: must be from 10 to 2000 characters"
    );
  }

  @Test
  @DisplayName("Attempt to create artist without photo")
  void negativeCreateArtistWithoutPhotoTest() {
    var request = CreateArtistRequest.newBuilder(prototype).clearPhoto().build();
    GrpcValidation.checkStatusRuntimeException(
        () -> ARTIST_BLOCKING_STUB.createArtist(request),
        Status.Code.INVALID_ARGUMENT,
        "photo: must not be blank"
    );
  }

  @Test
  @DisplayName("Attempt to create artist with empty photo")
  void negativeCreateArtistWithEmptyPhotoTest() {
    var request = CreateArtistRequest.newBuilder(prototype)
        .setPhoto(StringUtils.EMPTY)
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> ARTIST_BLOCKING_STUB.createArtist(request),
        Status.Code.INVALID_ARGUMENT,
        "photo: must not be blank"
    );
  }

  @Test
  @DisplayName("Attempt to create artist with photo exceeding the limit size")
  void negativeCreateArtistWithPhotoExceedingLimitSizeTest() {
    var request = CreateArtistRequest.newBuilder(prototype)
        .setPhoto(GEN.string().length(MAX_PHOTO_SIZE + 1).get())
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> ARTIST_BLOCKING_STUB.createArtist(request),
        Status.Code.INVALID_ARGUMENT,
        "photo: must not exceed 1 MB"
    );
  }
}
