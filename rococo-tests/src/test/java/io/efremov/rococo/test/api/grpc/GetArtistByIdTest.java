package io.efremov.rococo.test.api.grpc;

import static io.efremov.rococo.config.Constants.INFO_API_TAG;

import io.efremov.rococo.api.ValidationGrpcSteps;
import io.efremov.rococo.grpc.ArtistByIdRequest;
import io.efremov.rococo.grpc.ArtistsResponse;
import io.efremov.rococo.jupiter.annotation.NewArtist;
import io.efremov.rococo.model.ArtistInfoResponse;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(INFO_API_TAG)
@Epic("API")
@Feature("rococo-artist")
@Story("Get artist by id")
class GetArtistByIdTest extends BaseGrpcTest {

  @Test
  @NewArtist
  @DisplayName("Get artist by id")
  void positiveGetArtistByIdTest(ArtistInfoResponse existedArtist) {
    var request = ArtistByIdRequest.newBuilder()
        .addId(existedArtist.id().toString())
        .build();
    var response = ARTIST_BLOCKING_STUB.getArtistById(request);

    ValidationGrpcSteps.checkEntity(request, response);
  }

  @Test
  @DisplayName("Get artist by non-existent id returns empty list")
  void negativeGetArtistByIdWithNonExistentIdTest() {
    var request = ArtistByIdRequest.newBuilder()
        .addId(UUID.randomUUID().toString())
        .build();
    ArtistsResponse response = ARTIST_BLOCKING_STUB.getArtistById(request);

    Assertions.assertThat(response.getArtistsList()).as("artists").isEmpty();
  }
}
