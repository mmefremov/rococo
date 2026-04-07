package io.efremov.rococo.provider;

import io.efremov.rococo.data.entity.ArtistEntity;
import io.efremov.rococo.data.repository.ArtistRepository;
import io.efremov.rococo.grpc.CreateArtistRequest;
import io.efremov.rococo.grpc.UpdateArtistRequest;
import io.efremov.rococo.model.ArtistInfoResponse;
import io.efremov.rococo.model.CreateArtistInfoRequest;
import io.efremov.rococo.model.UpdateArtistInfoRequest;
import io.efremov.rococo.service.GatewayApiClient;
import io.efremov.rococo.util.RandomDataUtils;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.instancio.Instancio;
import org.instancio.InstancioApi;
import org.instancio.Model;
import org.instancio.Select;
import org.jspecify.annotations.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ArtistProvider {

  public static ArtistInfoResponse getAnyArtist() {
    ArtistEntity entity = new ArtistRepository().findAny();
    if (entity != null) {
      UUID id = entity.getId();
      return new GatewayApiClient().getArtistById(id)
          .body();
    }
    return getNewArtist();
  }

  public static ArtistInfoResponse getNewArtist() {
    return new GatewayApiClient().createArtist(getArtistInfoRequest())
        .body();
  }

  public static @NonNull CreateArtistInfoRequest getArtistInfoRequest() {
    return Instancio.create(getArtistInfoRequestModel());
  }

  public static @NonNull Model<CreateArtistInfoRequest> getArtistInfoRequestModel() {
    return Instancio.of(CreateArtistInfoRequest.class)
        .set(Select.field(CreateArtistInfoRequest::name), RandomDataUtils.randomFullName())
        .set(Select.field(CreateArtistInfoRequest::biography), RandomDataUtils.randomParagraph())
        .set(Select.field(CreateArtistInfoRequest::photo), RandomDataUtils.randomPhoto())
        .toModel();
  }

  public static @NonNull CreateArtistRequest getCreateArtistRequest() {
    return CreateArtistRequest.newBuilder()
        .setName(RandomDataUtils.randomFullName())
        .setBiography(RandomDataUtils.randomParagraph())
        .setPhoto(RandomDataUtils.randomPhoto())
        .build();
  }

  public static @NonNull UpdateArtistRequest getUpdateArtistRequest(ArtistInfoResponse artist) {
    return UpdateArtistRequest.newBuilder()
        .setId(artist.id().toString())
        .setName(artist.name())
        .setBiography(artist.biography())
        .setPhoto(artist.photo())
        .build();
  }

  public static @NonNull InstancioApi<UpdateArtistInfoRequest> getUpdateArtistInfoRequestApi(
      ArtistInfoResponse artist) {
    return Instancio.of(UpdateArtistInfoRequest.class)
        .set(Select.field(UpdateArtistInfoRequest::id), artist.id())
        .set(Select.field(UpdateArtistInfoRequest::name), artist.name())
        .set(Select.field(UpdateArtistInfoRequest::biography), artist.biography())
        .set(Select.field(UpdateArtistInfoRequest::photo), artist.photo());
  }

  public static @NonNull Model<UpdateArtistInfoRequest> getUpdateArtistInfoRequestModel() {
    ArtistInfoResponse artist = getNewArtist();
    return Instancio.of(UpdateArtistInfoRequest.class)
        .set(Select.field(UpdateArtistInfoRequest::id), artist.id())
        .set(Select.field(UpdateArtistInfoRequest::name), artist.name())
        .set(Select.field(UpdateArtistInfoRequest::biography), artist.biography())
        .set(Select.field(UpdateArtistInfoRequest::photo), artist.photo())
        .toModel();
  }
}
