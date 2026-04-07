package io.efremov.rococo.provider;

import io.efremov.rococo.data.entity.CountryEntity;
import io.efremov.rococo.data.entity.MuseumEntity;
import io.efremov.rococo.data.repository.CountryRepository;
import io.efremov.rococo.data.repository.MuseumRepository;
import io.efremov.rococo.grpc.CreateMuseumRequest;
import io.efremov.rococo.grpc.UpdateMuseumRequest;
import io.efremov.rococo.model.CountryInfoRequest;
import io.efremov.rococo.model.CreateMuseumInfoRequest;
import io.efremov.rococo.model.GeoInfoRequest;
import io.efremov.rococo.model.MuseumInfoResponse;
import io.efremov.rococo.model.UpdateMuseumInfoRequest;
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
public class MuseumProvider {

  public static MuseumInfoResponse getAnyMuseum() {
    MuseumEntity entity = new MuseumRepository().findAny();
    if (entity != null) {
      UUID id = entity.getId();
      return new GatewayApiClient().getMuseumById(id).body();
    }
    return getNewMuseum();
  }

  public static MuseumInfoResponse getNewMuseum() {
    return new GatewayApiClient().createMuseum(getCreateMuseumInfoRequest())
        .body();
  }

  public static CreateMuseumInfoRequest getCreateMuseumInfoRequest() {
    return Instancio.create(getCreateMuseumInfoRequestModel());
  }

  public static @NonNull Model<CreateMuseumInfoRequest> getCreateMuseumInfoRequestModel() {
    return Instancio.of(CreateMuseumInfoRequest.class)
        .set(Select.field(CreateMuseumInfoRequest::title), RandomDataUtils.randomTitle())
        .set(Select.field(CreateMuseumInfoRequest::description), RandomDataUtils.randomParagraph())
        .set(Select.field(CreateMuseumInfoRequest::photo), RandomDataUtils.randomPhoto())
        .setModel(Select.field(CreateMuseumInfoRequest::geo), getGeoInfoRequestModel())
        .toModel();
  }

  public static @NonNull Model<GeoInfoRequest> getGeoInfoRequestModel() {
    return Instancio.of(GeoInfoRequest.class)
        .set(Select.field(GeoInfoRequest::city), RandomDataUtils.randomCity())
        .setModel(Select.field(GeoInfoRequest::country), getCountryInfoRequestModel())
        .toModel();
  }

  public static @NonNull Model<CountryInfoRequest> getCountryInfoRequestModel() {
    CountryEntity country = new CountryRepository().findAny();
    return Instancio.of(CountryInfoRequest.class)
        .set(Select.field(CountryInfoRequest::id), country.getId())
        .toModel();
  }

  public static @NonNull CreateMuseumRequest getCreateMuseumRequest() {
    CountryEntity country = new CountryRepository().findAny();
    return CreateMuseumRequest.newBuilder()
        .setTitle(RandomDataUtils.randomTitle())
        .setDescription(RandomDataUtils.randomParagraph())
        .setPhoto(RandomDataUtils.randomPhoto())
        .setCity(RandomDataUtils.randomCity())
        .setCountryId(country.getId().toString())
        .build();
  }

  public static @NonNull UpdateMuseumRequest getUpdateMuseumRequest(MuseumInfoResponse museum) {
    return UpdateMuseumRequest.newBuilder()
        .setId(museum.id().toString())
        .setTitle(museum.title())
        .setDescription(museum.description())
        .setPhoto(museum.photo())
        .setCity(museum.geo().city())
        .setCountryId(museum.geo().country().id().toString())
        .build();
  }

  public static @NonNull InstancioApi<UpdateMuseumInfoRequest> getUpdateMuseumInfoRequestApi(
      MuseumInfoResponse museum) {
    return Instancio.of(UpdateMuseumInfoRequest.class)
        .set(Select.field(UpdateMuseumInfoRequest::id), museum.id())
        .set(Select.field(UpdateMuseumInfoRequest::title), museum.title())
        .set(Select.field(UpdateMuseumInfoRequest::description), museum.description())
        .set(Select.field(UpdateMuseumInfoRequest::photo), museum.photo())
        .set(Select.field(GeoInfoRequest::city), museum.geo().city())
        .set(Select.field(CountryInfoRequest::id), museum.geo().country().id());
  }

  public static @NonNull Model<UpdateMuseumInfoRequest> getUpdateMuseumInfoRequestModel() {
    MuseumInfoResponse museum = getNewMuseum();
    return Instancio.of(UpdateMuseumInfoRequest.class)
        .set(Select.field(UpdateMuseumInfoRequest::id), museum.id())
        .set(Select.field(UpdateMuseumInfoRequest::title), museum.title())
        .set(Select.field(UpdateMuseumInfoRequest::description), museum.description())
        .set(Select.field(UpdateMuseumInfoRequest::photo), museum.photo())
        .set(Select.field(GeoInfoRequest::city), museum.geo().city())
        .set(Select.field(CountryInfoRequest::id), museum.geo().country().id())
        .toModel();
  }
}
