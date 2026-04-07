package io.efremov.rococo.service;

import io.efremov.rococo.api.GatewayApi;
import io.efremov.rococo.api.core.BearerTokenInterceptor;
import io.efremov.rococo.model.ArtistInfoResponse;
import io.efremov.rococo.model.CountryInfoResponse;
import io.efremov.rococo.model.CreateArtistInfoRequest;
import io.efremov.rococo.model.CreateMuseumInfoRequest;
import io.efremov.rococo.model.CreatePaintingInfoRequest;
import io.efremov.rococo.model.MuseumInfoResponse;
import io.efremov.rococo.model.PageResponse;
import io.efremov.rococo.model.PaintingInfoResponse;
import io.efremov.rococo.model.SessionResponse;
import io.efremov.rococo.model.UpdateArtistInfoRequest;
import io.efremov.rococo.model.UpdateMuseumInfoRequest;
import io.efremov.rococo.model.UpdatePaintingInfoRequest;
import io.efremov.rococo.model.UpdateUserInfoRequest;
import io.efremov.rococo.model.UserInfoResponse;
import io.qameta.allure.Step;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Response;

@Slf4j
public final class GatewayApiClient extends RestClient {

  private final GatewayApi gatewayApi;

  public GatewayApiClient() {
    super(CFG.gatewayUrl(), false, new BearerTokenInterceptor());
    this.gatewayApi = create(GatewayApi.class);
  }

  @Step("Create artist")
  public Response<ArtistInfoResponse> createArtist(CreateArtistInfoRequest request) {
    try {
      return gatewayApi.createArtist(request).execute();
    } catch (IOException e) {
      throw new RuntimeException("Failed to create artist", e);
    }
  }

  @Step("Update artist")
  public Response<ArtistInfoResponse> updateArtist(UpdateArtistInfoRequest request) {
    try {
      return gatewayApi.updateArtist(request).execute();
    } catch (IOException e) {
      throw new RuntimeException("Failed to update artist", e);
    }
  }

  @Step("Get artist by id")
  public Response<ArtistInfoResponse> getArtistById(UUID id) {
    try {
      return gatewayApi.getArtistById(id).execute();
    } catch (IOException e) {
      throw new RuntimeException("Failed to get artist by id: " + id, e);
    }
  }

  @Step("Get all artists")
  public Response<PageResponse<ArtistInfoResponse>> getAllArtists(int page, int size, String name) {
    try {
      return gatewayApi.getAllArtists(page, size, name).execute();
    } catch (IOException e) {
      throw new RuntimeException("Failed to get all artists", e);
    }
  }

  public Response<PageResponse<ArtistInfoResponse>> getAllArtists(int page, int size) {
    return getAllArtists(page, size, null);
  }

  @Step("Create museum")
  public Response<MuseumInfoResponse> createMuseum(CreateMuseumInfoRequest request) {
    try {
      return gatewayApi.createMuseum(request).execute();
    } catch (IOException e) {
      throw new RuntimeException("Failed to create museum", e);
    }
  }

  @Step("Update museum")
  public Response<MuseumInfoResponse> updateMuseum(UpdateMuseumInfoRequest request) {
    try {
      return gatewayApi.updateMuseum(request).execute();
    } catch (IOException e) {
      throw new RuntimeException("Failed to update museum", e);
    }
  }

  @Step("Get museum by id")
  public Response<MuseumInfoResponse> getMuseumById(UUID id) {
    try {
      return gatewayApi.getMuseumById(id).execute();
    } catch (IOException e) {
      throw new RuntimeException("Failed to get museum by id: " + id, e);
    }
  }

  @Step("Get all museums")
  public Response<PageResponse<MuseumInfoResponse>> getAllMuseums(int page, int size, String title) {
    try {
      return gatewayApi.getAllMuseums(page, size, title).execute();
    } catch (IOException e) {
      throw new RuntimeException("Failed to get all museums", e);
    }
  }

  public Response<PageResponse<MuseumInfoResponse>> getAllMuseums(int page, int size) {
    return getAllMuseums(page, size, null);
  }


  @Step("Create painting")
  public Response<PaintingInfoResponse> createPainting(CreatePaintingInfoRequest request) {
    try {
      return gatewayApi.createPainting(request).execute();
    } catch (IOException e) {
      throw new RuntimeException("Failed to create painting", e);
    }
  }

  @Step("Update painting")
  public Response<PaintingInfoResponse> updatePainting(UpdatePaintingInfoRequest request) {
    try {
      return gatewayApi.updatePainting(request).execute();
    } catch (IOException e) {
      throw new RuntimeException("Failed to update painting", e);
    }
  }

  @Step("Get painting by id")
  public Response<PaintingInfoResponse> getPaintingById(UUID id) {
    try {
      return gatewayApi.getPaintingById(id).execute();
    } catch (IOException e) {
      throw new RuntimeException("Failed to get painting by id: " + id, e);
    }
  }

  @Step("Get paintings by artist")
  public Response<PageResponse<PaintingInfoResponse>> getPaintingsByArtist(UUID artistId, int page, int size) {
    try {
      return gatewayApi.getPaintingsByArtist(artistId, page, size).execute();
    } catch (IOException e) {
      throw new RuntimeException("Failed to get paintings by artist: " + artistId, e);
    }
  }

  @Step("Get all paintings")
  public Response<PageResponse<PaintingInfoResponse>> getAllPaintings(int page, int size, String title) {
    try {
      return gatewayApi.getAllPaintings(page, size, title).execute();
    } catch (IOException e) {
      throw new RuntimeException("Failed to get all paintings", e);
    }
  }

  public Response<PageResponse<PaintingInfoResponse>> getAllPaintings(int page, int size) {
    return getAllPaintings(page, size, null);
  }

  @Step("Get session")
  public Response<SessionResponse> getSession() {
    try {
      return gatewayApi.getSession().execute();
    } catch (IOException e) {
      throw new RuntimeException("Failed to get session", e);
    }
  }

  @Step("Get user")
  public Response<UserInfoResponse> getUser() {
    try {
      return gatewayApi.getUser().execute();
    } catch (IOException e) {
      throw new RuntimeException("Failed to get user", e);
    }
  }

  @Step("Update user")
  public Response<UserInfoResponse> updateUser(UpdateUserInfoRequest request) {
    try {
      return gatewayApi.updateUser(request).execute();
    } catch (IOException e) {
      throw new RuntimeException("Failed to update user", e);
    }
  }

  @Step("Get all countries")
  public Response<PageResponse<CountryInfoResponse>> getAllCountries(int page, int size) {
    try {
      return gatewayApi.getAllCountries(page, size).execute();
    } catch (IOException e) {
      throw new RuntimeException("Failed to get all countries", e);
    }
  }
}
