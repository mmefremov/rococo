package io.efremov.rococo.provider;

import io.efremov.rococo.data.entity.PaintingEntity;
import io.efremov.rococo.data.repository.PaintingRepository;
import io.efremov.rococo.grpc.CreatePaintingRequest;
import io.efremov.rococo.grpc.UpdatePaintingRequest;
import io.efremov.rococo.grpc.UpdatePaintingRequest.Builder;
import io.efremov.rococo.model.ArtistInfoRequest;
import io.efremov.rococo.model.CreatePaintingInfoRequest;
import io.efremov.rococo.model.MuseumInfoRequest;
import io.efremov.rococo.model.PaintingInfoResponse;
import io.efremov.rococo.model.UpdatePaintingInfoRequest;
import io.efremov.rococo.service.GatewayApiClient;
import io.efremov.rococo.util.RandomDataUtils;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.instancio.Instancio;
import org.instancio.InstancioApi;
import org.instancio.Model;
import org.instancio.Select;
import org.jspecify.annotations.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaintingProvider {

  public static PaintingInfoResponse getAnyPainting() {
    PaintingEntity entity = new PaintingRepository().findAny();
    if (entity != null) {
      UUID id = entity.getId();
      return new GatewayApiClient().getPaintingById(id)
          .body();
    }
    return getNewPainting();
  }

  public static PaintingInfoResponse getNewPainting() {
    return new GatewayApiClient().createPainting(getCreatePaintingInfoRequest())
        .body();
  }

  public static @NonNull CreatePaintingInfoRequest getCreatePaintingInfoRequest() {
    return Instancio.create(getCreatePaintingInfoRequestModel());
  }

  public static @NonNull Model<CreatePaintingInfoRequest> getCreatePaintingInfoRequestModel() {
    var artist = ArtistProvider.getAnyArtist();
    var museum = MuseumProvider.getAnyMuseum();

    return Instancio.of(CreatePaintingInfoRequest.class)
        .set(Select.field(CreatePaintingInfoRequest::title), RandomDataUtils.randomTitle())
        .set(Select.field(CreatePaintingInfoRequest::description), RandomDataUtils.randomParagraph())
        .set(Select.field(CreatePaintingInfoRequest::content), RandomDataUtils.randomPhoto())
        .set(Select.field(ArtistInfoRequest::id), Objects.requireNonNull(artist).id())
        .set(Select.field(MuseumInfoRequest::id), Objects.requireNonNull(museum).id())
        .toModel();
  }

  public static @NonNull CreatePaintingRequest getCreatePaintingRequest() {
    var artist = ArtistProvider.getAnyArtist();
    var museum = MuseumProvider.getAnyMuseum();
    return CreatePaintingRequest.newBuilder()
        .setTitle(RandomDataUtils.randomTitle())
        .setDescription(RandomDataUtils.randomParagraph())
        .setContent(RandomDataUtils.randomPhoto())
        .setArtistId(Objects.requireNonNull(artist).id().toString())
        .setMuseumId(Objects.requireNonNull(museum).id().toString())
        .build();
  }

  public static @NonNull UpdatePaintingRequest getUpdatePaintingRequest(PaintingInfoResponse painting) {
    Builder builder = UpdatePaintingRequest.newBuilder()
        .setId(painting.id().toString())
        .setTitle(painting.title())
        .setDescription(painting.description())
        .setContent(painting.content())
        .setArtistId(painting.artist().id().toString());
    if (painting.museum().id() != null) {
      builder.setMuseumId(painting.museum().id().toString());
    }
    return builder.build();
  }

  public static @NonNull InstancioApi<UpdatePaintingInfoRequest> getUpdatePaintingInfoRequestApi(
      PaintingInfoResponse painting) {
    return Instancio.of(UpdatePaintingInfoRequest.class)
        .set(Select.field(UpdatePaintingInfoRequest::id), painting.id())
        .set(Select.field(UpdatePaintingInfoRequest::title), RandomDataUtils.randomTitle())
        .set(Select.field(UpdatePaintingInfoRequest::description), painting.description())
        .set(Select.field(UpdatePaintingInfoRequest::content), painting.content())
        .set(Select.field(ArtistInfoRequest::id), painting.artist().id())
        .set(Select.field(MuseumInfoRequest::id), painting.museum().id());
  }

  public static @NonNull Model<UpdatePaintingInfoRequest> getUpdatePaintingInfoRequestModel() {
    var painting = getNewPainting();
    return Instancio.of(UpdatePaintingInfoRequest.class)
        .set(Select.field(UpdatePaintingInfoRequest::id), painting.id())
        .set(Select.field(UpdatePaintingInfoRequest::title), painting.title())
        .set(Select.field(UpdatePaintingInfoRequest::description), painting.description())
        .set(Select.field(UpdatePaintingInfoRequest::content), painting.content())
        .set(Select.field(ArtistInfoRequest::id), painting.artist().id())
        .set(Select.field(MuseumInfoRequest::id), painting.museum().id())
        .toModel();
  }
}
