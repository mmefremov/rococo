package io.efremov.rococo.test.api.grpc;

import static io.efremov.rococo.config.Constants.MAX_PARAGRAPH_LENGTH;
import static io.efremov.rococo.config.Constants.MAX_PHOTO_SIZE;
import static io.efremov.rococo.config.Constants.MAX_TITLE_LENGTH;
import static io.efremov.rococo.config.Constants.MIN_PARAGRAPH_LENGTH;
import static io.efremov.rococo.config.Constants.MIN_TITLE_LENGTH;
import static io.efremov.rococo.config.Constants.MUTATION_API_TAG;

import io.efremov.rococo.api.validation.GrpcValidation;
import io.efremov.rococo.jupiter.annotation.AnyArtist;
import io.efremov.rococo.jupiter.annotation.NewArtist;
import io.efremov.rococo.model.ArtistInfoResponse;
import io.efremov.rococo.provider.ArtistProvider;
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
@Feature("rococo-artist")
@Story("Update an artist")
class UpdateArtistTest extends BaseGrpcTest {

  private static final ArtistInfoResponse prototype = ArtistProvider.getAnyArtist();

  @Test
  @NewArtist
  @DisplayName("Update artist name")
  void positiveUpdateArtistWithNameTest(ArtistInfoResponse artist) {
    var request = ArtistProvider.getUpdateArtistRequest(artist).toBuilder()
        .setName(RandomDataUtils.randomFullName())
        .build();
    var response = ARTIST_BLOCKING_STUB.updateArtist(request);

    GrpcValidation.checkResponse(request, response);
    GrpcValidation.checkEntity(request, response.getId());
  }

  @Test
  @NewArtist
  @DisplayName("Update artist biography")
  void positiveUpdateArtistWithBiographyTest(ArtistInfoResponse artist) {
    var request = ArtistProvider.getUpdateArtistRequest(artist).toBuilder()
        .setBiography(RandomDataUtils.randomParagraph())
        .build();
    var response = ARTIST_BLOCKING_STUB.updateArtist(request);

    GrpcValidation.checkResponse(request, response);
    GrpcValidation.checkEntity(request, response.getId());
  }

  @Test
  @NewArtist
  @DisplayName("Update artist photo")
  void positiveUpdateArtistWithPhotoTest(ArtistInfoResponse artist) {
    var request = ArtistProvider.getUpdateArtistRequest(artist).toBuilder()
        .setPhoto(RandomDataUtils.randomPhoto())
        .build();
    var response = ARTIST_BLOCKING_STUB.updateArtist(request);

    GrpcValidation.checkResponse(request, response);
  }

  @Test
  @DisplayName("Attempt to update artist with non-existent id")
  void negativeUpdateArtistWithNonExistentIdTest() {
    String nonExistentId = UUID.randomUUID().toString();
    var request = ArtistProvider.getUpdateArtistRequest(prototype).toBuilder()
        .setId(nonExistentId)
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> ARTIST_BLOCKING_STUB.updateArtist(request),
        Status.Code.NOT_FOUND,
        "Artist not found: %s".formatted(nonExistentId)
    );
  }

  @Test
  @AnyArtist
  @DisplayName("Attempt to update artist with duplicate name")
  void negativeUpdateArtistWithDuplicateNameTest(ArtistInfoResponse existedArtist) {
    var request = ArtistProvider.getUpdateArtistRequest(prototype).toBuilder()
        .setName(existedArtist.name())
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> ARTIST_BLOCKING_STUB.updateArtist(request),
        Status.Code.ALREADY_EXISTS,
        "Artist name already taken: %s".formatted(request.getName())
    );
  }

  @Test
  @DisplayName("Attempt to update artist without name")
  void negativeUpdateArtistWithoutNameTest() {
    var request = ArtistProvider.getUpdateArtistRequest(prototype).toBuilder()
        .clearName()
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> ARTIST_BLOCKING_STUB.updateArtist(request),
        Status.Code.INVALID_ARGUMENT,
        "name: must not be blank"
    );
  }

  @Test
  @DisplayName("Attempt to update artist with empty name")
  void negativeUpdateArtistWithEmptyNameTest() {
    var request = ArtistProvider.getUpdateArtistRequest(prototype).toBuilder()
        .setName("")
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> ARTIST_BLOCKING_STUB.updateArtist(request),
        Status.Code.INVALID_ARGUMENT,
        "name: must not be blank"
    );
  }

  @ParameterizedTest
  @ValueSource(ints = {MIN_TITLE_LENGTH - 1, MAX_TITLE_LENGTH + 1})
  @DisplayName("Attempt to update artist with name length that is out of range")
  void negativeUpdateArtistWithNameLengthThatIsOutOfRangeTest(int length) {
    var request = ArtistProvider.getUpdateArtistRequest(prototype).toBuilder()
        .setName(RandomDataUtils.GEN.string().length(length).get())
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> ARTIST_BLOCKING_STUB.updateArtist(request),
        Status.Code.INVALID_ARGUMENT,
        "name: must be from %d to %d characters".formatted(MIN_TITLE_LENGTH, MAX_TITLE_LENGTH)
    );
  }

  @Test
  @DisplayName("Attempt to update artist without biography")
  void negativeUpdateArtistWithoutBiographyTest() {
    var request = ArtistProvider.getUpdateArtistRequest(prototype).toBuilder()
        .clearBiography()
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> ARTIST_BLOCKING_STUB.updateArtist(request),
        Status.Code.INVALID_ARGUMENT,
        "biography: must not be blank"
    );
  }

  @Test
  @DisplayName("Attempt to update artist with empty biography")
  void negativeUpdateArtistWithEmptyBiographyTest() {
    var request = ArtistProvider.getUpdateArtistRequest(prototype).toBuilder()
        .setBiography("")
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> ARTIST_BLOCKING_STUB.updateArtist(request),
        Status.Code.INVALID_ARGUMENT,
        "biography: must not be blank"
    );
  }

  @ParameterizedTest
  @ValueSource(ints = {MIN_PARAGRAPH_LENGTH - 1, MAX_PARAGRAPH_LENGTH + 1})
  @DisplayName("Attempt to update artist with biography length that is out of range")
  void negativeUpdateArtistWithBiographyLengthThatIsOutOfRangeTest(int length) {
    var request = ArtistProvider.getUpdateArtistRequest(prototype).toBuilder()
        .setBiography(RandomDataUtils.GEN.string().length(length).get())
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> ARTIST_BLOCKING_STUB.updateArtist(request),
        Status.Code.INVALID_ARGUMENT,
        "biography: must be from %d to %d characters".formatted(MIN_PARAGRAPH_LENGTH, MAX_PARAGRAPH_LENGTH)
    );
  }

  @Test
  @DisplayName("Attempt to update artist without photo")
  void negativeUpdateArtistWithoutPhotoTest() {
    var request = ArtistProvider.getUpdateArtistRequest(prototype).toBuilder()
        .clearPhoto()
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> ARTIST_BLOCKING_STUB.updateArtist(request),
        Status.Code.INVALID_ARGUMENT,
        "photo: must not be blank"
    );
  }

  @Test
  @DisplayName("Attempt to update artist with empty photo")
  void negativeUpdateArtistWithEmptyPhotoTest() {
    var request = ArtistProvider.getUpdateArtistRequest(prototype).toBuilder()
        .setPhoto("")
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> ARTIST_BLOCKING_STUB.updateArtist(request),
        Status.Code.INVALID_ARGUMENT,
        "photo: must not be blank"
    );
  }

  @Test
  @DisplayName("Attempt to update artist with photo exceeding the limit size")
  void negativeUpdateArtistWithPhotoExceedingLimitSizeTest() {
    var request = ArtistProvider.getUpdateArtistRequest(prototype).toBuilder()
        .setPhoto(RandomDataUtils.GEN.string().length(MAX_PHOTO_SIZE + 1).get())
        .build();
    GrpcValidation.checkStatusRuntimeException(
        () -> ARTIST_BLOCKING_STUB.updateArtist(request),
        Status.Code.INVALID_ARGUMENT,
        "photo: must not exceed 1 MB"
    );
  }
}
