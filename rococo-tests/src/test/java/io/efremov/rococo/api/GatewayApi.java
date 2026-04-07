package io.efremov.rococo.api;

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
import java.util.UUID;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GatewayApi {

  @GET("api/session")
  Call<SessionResponse> getSession();

  @GET("api/user")
  Call<UserInfoResponse> getUser();

  @PATCH("api/user")
  Call<UserInfoResponse> updateUser(@Body UpdateUserInfoRequest request);

  @GET("api/artist")
  Call<PageResponse<ArtistInfoResponse>> getAllArtists(
      @Query("page") int page,
      @Query("size") int size,
      @Query("name") String name);

  @GET("api/artist/{id}")
  Call<ArtistInfoResponse> getArtistById(@Path("id") UUID id);

  @POST("api/artist")
  Call<ArtistInfoResponse> createArtist(@Body CreateArtistInfoRequest request);

  @PATCH("api/artist")
  Call<ArtistInfoResponse> updateArtist(@Body UpdateArtistInfoRequest request);

  @GET("api/museum")
  Call<PageResponse<MuseumInfoResponse>> getAllMuseums(
      @Query("page") int page,
      @Query("size") int size,
      @Query("title") String title);

  @GET("api/museum/{id}")
  Call<MuseumInfoResponse> getMuseumById(@Path("id") UUID id);

  @POST("api/museum")
  Call<MuseumInfoResponse> createMuseum(@Body CreateMuseumInfoRequest request);

  @PATCH("api/museum")
  Call<MuseumInfoResponse> updateMuseum(@Body UpdateMuseumInfoRequest request);

  @GET("api/painting")
  Call<PageResponse<PaintingInfoResponse>> getAllPaintings(
      @Query("page") int page,
      @Query("size") int size,
      @Query("title") String title);

  @GET("api/painting/{id}")
  Call<PaintingInfoResponse> getPaintingById(@Path("id") UUID id);

  @GET("api/painting/author/{artistId}")
  Call<PageResponse<PaintingInfoResponse>> getPaintingsByArtist(
      @Path("artistId") UUID artistId,
      @Query("page") int page,
      @Query("size") int size);

  @POST("api/painting")
  Call<PaintingInfoResponse> createPainting(@Body CreatePaintingInfoRequest request);

  @PATCH("api/painting")
  Call<PaintingInfoResponse> updatePainting(@Body UpdatePaintingInfoRequest request);

  @GET("api/country")
  Call<PageResponse<CountryInfoResponse>> getAllCountries(
      @Query("page") int page,
      @Query("size") int size);
}
