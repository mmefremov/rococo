package io.efremov.rococo.api.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.efremov.rococo.data.entity.ArtistEntity;
import io.efremov.rococo.data.entity.CountryEntity;
import io.efremov.rococo.data.entity.MuseumEntity;
import io.efremov.rococo.data.entity.PaintingEntity;
import io.efremov.rococo.data.repository.ArtistRepository;
import io.efremov.rococo.data.repository.CountryRepository;
import io.efremov.rococo.data.repository.MuseumRepository;
import io.efremov.rococo.data.repository.PaintingRepository;
import io.efremov.rococo.data.repository.UserRepository;
import io.efremov.rococo.model.ArtistInfoResponse;
import io.efremov.rococo.model.CountryInfoResponse;
import io.efremov.rococo.model.CreateArtistInfoRequest;
import io.efremov.rococo.model.CreateMuseumInfoRequest;
import io.efremov.rococo.model.CreatePaintingInfoRequest;
import io.efremov.rococo.model.ErrorBody;
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
import java.net.HttpURLConnection;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import okhttp3.HttpUrl;
import okhttp3.ResponseBody;
import org.jspecify.annotations.NonNull;
import retrofit2.Response;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RestValidation {

  @Step("Check that the response has a successful status")
  public static void responseMustHaveSuccessfulStatus(Response<?> response) {
    assertThat(response.code()).as("status code").isEqualTo(HttpURLConnection.HTTP_OK);
  }

  @Step("Check that the response has 'created' status")
  public static void responseMustHaveCreatedStatus(Response<?> response) {
    assertThat(response.code()).as("status code").isEqualTo(HttpURLConnection.HTTP_CREATED);
  }

  @Step("Check that the response has 'bad request' status")
  public static void responseMustHaveBadRequestStatus(Response<?> response) {
    assertThat(response.code()).as("status code").isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
  }

  @Step("Check that the response has 'authorized' status")
  public static void responseMustHaveUnauthorizedStatus(Response<?> response) {
    assertThat(response.code()).as("status code").isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
  }

  @Step("Check that the response has 'not found' status")
  public static void responseMustHaveNotFoundStatus(Response<?> response) {
    assertThat(response.code()).as("status code").isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
  }

  @Step("Check that the response has 'conflict' status")
  public static void responseMustHaveConflictStatus(Response<?> response) {
    assertThat(response.code()).as("status code").isEqualTo(HttpURLConnection.HTTP_CONFLICT);
  }

  @Step("Check that the response body is empty")
  public static void bodyMustBeEmpty(Response<?> response) {
    assertThat(response.body()).as("body").isNull();
  }

  @Step("Check that the response body has the error")
  public static void bodyMustHaveTheError(Response<?> response, String expectedError) {
    assertThat(response.body()).as("body").isNull();
    ResponseBody responseBody = response.errorBody();
    assertThat(responseBody).as("error body").isNotNull();
    String actualError = ErrorBody.getErrorFromResponseBody(responseBody);
    assertThat(actualError).as("error").isEqualTo(expectedError);
  }

  public static void bodyMustHaveBadRequestError(Response<?> response) {
    bodyMustHaveTheError(response, "Bad Request");
  }

  public static void bodyMustHavePhotoExceedLimitError(Response<?> response) {
    bodyMustHaveTheError(response, "photo: must not exceed 1 MB");
  }

  public static void bodyMustHaveContentExceedLimitError(Response<?> response) {
    bodyMustHaveTheError(response, "content: must not exceed 1 MB");
  }

  public static void bodyMustHavePageIndexLessThanZeroError(Response<?> response) {
    bodyMustHaveTheError(response, "Page index must not be less than zero");
  }

  public static void bodyMustHavePageSizeLessThanOneError(Response<?> response) {
    bodyMustHaveTheError(response, "Page size must not be less than one");
  }

  @Step("Check that the response body is present")
  public static void bodyMustBePresent(Response<?> response) {
    assertThat(response.body()).as("body").isNotNull();
  }

  @Step("Check the data of sent input and the received response")
  public static void checkResponse(@NonNull CreateArtistInfoRequest request, @NonNull ArtistInfoResponse info) {
    assertSoftly(softly -> {
      softly.assertThat(info.id()).as("id").isNotNull();
      softly.assertThat(info.name()).as("name").isEqualTo(request.name());
      softly.assertThat(info.biography()).as("biography").isEqualTo(request.biography());
      softly.assertThat(info.photo()).as("photo").isEqualTo(request.photo());
    });
  }

  @Step("Check if all the data was saved correctly in the database")
  public static void checkEntity(@NonNull CreateArtistInfoRequest request, @NonNull UUID id) {
    var entity = new ArtistRepository().findById(id);
    assertThat(entity).as("entity").isNotNull();
    assertSoftly(softly -> {
      softly.assertThat(entity.getName()).as("name").isEqualTo(request.name());
      softly.assertThat(entity.getBiography()).as("biography").isEqualTo(request.biography());
      softly.assertThat(entity.getPhoto()).asString().as("photo").isEqualTo(request.photo());
    });
  }

  @Step("Check the data of sent input and the received response")
  public static void checkResponse(@NonNull UpdateArtistInfoRequest request, @NonNull ArtistInfoResponse info) {
    assertSoftly(softly -> {
      softly.assertThat(info.id()).as("id").isEqualTo(request.id());
      softly.assertThat(info.name()).as("name").isEqualTo(request.name());
      softly.assertThat(info.biography()).as("biography").isEqualTo(request.biography());
      softly.assertThat(info.photo()).as("photo").isEqualTo(request.photo());
    });
  }

  @Step("Check if all the data was saved correctly in the database")
  public static void checkEntity(@NonNull UpdateArtistInfoRequest request, @NonNull UUID id) {
    var entity = new ArtistRepository().findById(id);
    assertThat(entity).as("entity").isNotNull();
    assertSoftly(softly -> {
      softly.assertThat(entity.getName()).as("name").isEqualTo(request.name());
      softly.assertThat(entity.getBiography()).as("biography").isEqualTo(request.biography());
      softly.assertThat(entity.getPhoto()).asString().as("photo").isEqualTo(request.photo());
    });
  }

  @Step("Check the data of sent input and the received response")
  public static void checkResponse(@NonNull CreateMuseumInfoRequest request, @NonNull MuseumInfoResponse info) {
    assertThat(info.geo()).as("geo").isNotNull();
    assertThat(info.geo().country()).as("country").isNotNull();
    assertSoftly(softly -> {
      softly.assertThat(info.id()).as("id").isNotNull();
      softly.assertThat(info.title()).as("title").isEqualTo(request.title());
      softly.assertThat(info.description()).as("description").isEqualTo(request.description());
      softly.assertThat(info.photo()).as("photo").isEqualTo(request.photo());
      softly.assertThat(info.geo().city()).as("city").isEqualTo(request.geo().city());
      softly.assertThat(info.geo().country().id()).as("country id").isEqualTo(request.geo().country().id());
    });
  }

  @Step("Check if all the sent data was saved correctly in the database")
  public static void checkEntity(@NonNull CreateMuseumInfoRequest request, @NonNull UUID id) {
    var entity = new MuseumRepository().findById(id);
    assertThat(entity).as("entity").isNotNull();
    assertSoftly(softly -> {
      softly.assertThat(entity.getTitle()).as("title").isEqualTo(request.title());
      softly.assertThat(entity.getDescription()).as("description").isEqualTo(request.description());
      softly.assertThat(entity.getCity()).as("city").isEqualTo(request.geo().city());
      softly.assertThat(entity.getPhoto()).asString().as("photo").isEqualTo(request.photo());
      softly.assertThat(entity.getCountryId()).as("country id").isEqualTo(request.geo().country().id());
    });
  }

  @Step("Check the data of sent input and the received response")
  public static void checkResponse(@NonNull UpdateMuseumInfoRequest request, @NonNull MuseumInfoResponse info) {
    assertThat(info.geo()).as("geo").isNotNull();
    assertThat(info.geo().country()).as("country").isNotNull();
    assertSoftly(softly -> {
      softly.assertThat(info.id()).as("id").isNotNull();
      softly.assertThat(info.title()).as("title").isEqualTo(request.title());
      softly.assertThat(info.description()).as("description").isEqualTo(request.description());
      softly.assertThat(info.photo()).as("photo").isEqualTo(request.photo());
      softly.assertThat(info.geo().city()).as("city").isEqualTo(request.geo().city());
      softly.assertThat(info.geo().country().id()).as("country id").isEqualTo(request.geo().country().id());
    });
  }

  @Step("Check if all the data was saved correctly in the database")
  public static void checkEntity(@NonNull UpdateMuseumInfoRequest request, @NonNull UUID id) {
    var entity = new MuseumRepository().findById(id);
    assertThat(entity).as("entity").isNotNull();
    assertSoftly(softly -> {
      softly.assertThat(entity.getTitle()).as("title").isEqualTo(request.title());
      softly.assertThat(entity.getDescription()).as("description").isEqualTo(request.description());
      softly.assertThat(entity.getCity()).as("city").isEqualTo(request.geo().city());
      softly.assertThat(entity.getPhoto()).asString().as("photo").isEqualTo(request.photo());
      softly.assertThat(entity.getCountryId()).as("country id").isEqualTo(request.geo().country().id());
    });
  }

  @Step("Check the data of sent input and the received response")
  public static void checkResponse(@NonNull CreatePaintingInfoRequest request, @NonNull PaintingInfoResponse info) {
    assertSoftly(softly -> {
      softly.assertThat(info.id()).as("id").isNotNull();
      softly.assertThat(info.title()).as("title").isEqualTo(request.title());
      softly.assertThat(info.content()).as("content").isEqualTo(request.content());
      softly.assertThat(info.description()).as("description").isEqualTo(request.description());
      softly.assertThat(info.artist().id()).as("artist id").isEqualTo(request.artist().id());
      softly.assertThat(info.museum().id()).as("museum id").isEqualTo(request.museum().id());
    });
  }

  @Step("Check if all the sent data was saved correctly in the database")
  public static void checkEntity(@NonNull CreatePaintingInfoRequest request, @NonNull UUID id) {
    var entity = new PaintingRepository().findById(id);
    assertThat(entity).as("entity").isNotNull();
    assertSoftly(softly -> {
      softly.assertThat(entity.getTitle()).as("title").isEqualTo(request.title());
      softly.assertThat(entity.getDescription()).as("description").isEqualTo(request.description());
      softly.assertThat(entity.getContent()).asString().as("content").isEqualTo(request.content());
      softly.assertThat(entity.getArtistId()).as("artist id").isEqualTo(request.artist().id());
      softly.assertThat(entity.getMuseumId()).as("museum id").isEqualTo(request.museum().id());
    });
  }

  @Step("Check the data of sent input and the received response")
  public static void checkResponse(@NonNull UpdatePaintingInfoRequest request, @NonNull PaintingInfoResponse info) {
    assertSoftly(softly -> {
      softly.assertThat(info.id()).as("id").isNotNull();
      softly.assertThat(info.title()).as("title").isEqualTo(request.title());
      softly.assertThat(info.content()).as("content").isEqualTo(request.content());
      softly.assertThat(info.description()).as("description").isEqualTo(request.description());
      softly.assertThat(info.artist().id()).as("artist id").isEqualTo(request.artist().id());
      softly.assertThat(info.museum().id()).as("museum id").isEqualTo(request.museum().id());
    });
  }

  @Step("Check if all the data was saved correctly in the database")
  public static void checkEntity(@NonNull UpdatePaintingInfoRequest request, @NonNull UUID id) {
    var entity = new PaintingRepository().findById(id);
    assertThat(entity).as("entity").isNotNull();
    assertSoftly(softly -> {
      softly.assertThat(entity.getTitle()).as("title").isEqualTo(request.title());
      softly.assertThat(entity.getDescription()).as("description").isEqualTo(request.description());
      softly.assertThat(entity.getContent()).asString().as("content").isEqualTo(request.content());
      softly.assertThat(entity.getArtistId()).as("artist id").isEqualTo(request.artist().id());
      softly.assertThat(entity.getMuseumId()).as("museum id").isEqualTo(request.museum().id());
    });
  }

  @Step("Check the data of the received response matches the data from the database")
  public static void checkResponse(ArtistInfoResponse info, UUID id) {
    var entity = new ArtistRepository().findById(id);
    assertThat(entity).as("entity").isNotNull();
    assertSoftly(softly -> {
      softly.assertThat(info.id()).as("name").isEqualTo(id);
      softly.assertThat(info.name()).as("name").isEqualTo(entity.getName());
      softly.assertThat(info.biography()).as("biography").isEqualTo(entity.getBiography());
      softly.assertThat(info.photo()).as("photo").isEqualTo(new String(entity.getPhoto()));
    });
  }

  @Step("Check the data of the received response matches the data from the database")
  public static void checkResponse(MuseumInfoResponse info, UUID id) {
    var entity = new MuseumRepository().findById(id);
    assertThat(entity).as("entity").isNotNull();
    assertSoftly(softly -> {
      softly.assertThat(info.id()).as("name").isEqualTo(id);
      softly.assertThat(info.title()).as("title").isEqualTo(entity.getTitle());
      softly.assertThat(info.description()).as("description").isEqualTo(entity.getDescription());
      softly.assertThat(info.photo()).as("photo").isEqualTo(new String(entity.getPhoto()));
      softly.assertThat(info.geo().city()).as("city").isEqualTo(entity.getCity());
      softly.assertThat(info.geo().country().id()).as("country id").isEqualTo(entity.getCountryId());
    });
    checkEntity(info.geo().country(), info.geo().country().id());
  }

  @Step("Check the data of the received response matches the data from the database")
  public static void checkResponse(PaintingInfoResponse info, UUID id) {
    var entity = new PaintingRepository().findById(id);
    assertThat(entity).as("entity").isNotNull();
    assertSoftly(softly -> {
      softly.assertThat(info.id()).as("name").isEqualTo(id);
      softly.assertThat(info.title()).as("title").isEqualTo(entity.getTitle());
      softly.assertThat(info.description()).as("description").isEqualTo(entity.getDescription());
      softly.assertThat(info.content()).as("content").isEqualTo(new String(entity.getContent()));
      softly.assertThat(info.artist().id()).as("artist id").isEqualTo(entity.getArtistId());
      softly.assertThat(info.museum().id()).as("museum id").isEqualTo(entity.getMuseumId());
    });
    checkResponse(info.museum(), info.museum().id());
    checkResponse(info.artist(), info.artist().id());
  }

  @Step("Check the data of the received response matches the data from the database")
  public static void checkEntity(CountryInfoResponse info, UUID id) {
    var entity = new CountryRepository().findById(id);
    assertThat(entity).as("entity").isNotNull();
    assertSoftly(softly -> {
      softly.assertThat(info.id()).as("name").isEqualTo(id);
      softly.assertThat(info.name()).as("title").isEqualTo(entity.getName());
    });
  }

  @Step("Check the session data")
  public static void checkSessionResponseWithAuth(SessionResponse session) {
    assertSoftly(softly -> {
      assertThat(session.username()).as("username").isNotNull();
      assertThat(session.issuedAt()).as("issuedAt").isNotNull().isBefore(OffsetDateTime.now());
      assertThat(session.expiresAt()).as("expiresAt").isNotNull().isAfter(session.issuedAt());
      assertThat(Duration.between(session.issuedAt(), session.expiresAt()))
          .as("duration").isEqualTo(Duration.ofMinutes(30));
    });
  }

  @Step("Check the session data")
  public static void checkSessionResponseWithoutAuth(SessionResponse session) {
    assertSoftly(softly -> {
      assertThat(session.username()).as("username").isNull();
      assertThat(session.issuedAt()).as("username").isNull();
      assertThat(session.expiresAt()).as("username").isNull();
    });
  }

  @Step("Check the data of the received response matches the data from the database")
  public static void checkResponse(UserInfoResponse info) {
    assertThat(info.username()).as("username").isNotNull();
    var entity = new UserRepository().findByUsername(info.username());
    assertSoftly(softly -> {
      softly.assertThat(info.id()).as("id").isEqualTo(entity.getId());
      softly.assertThat(info.firstname()).as("firstname").isEqualTo(entity.getFirstName());
      softly.assertThat(info.lastname()).as("lastname").isEqualTo(entity.getLastName());
      if (entity.getAvatar() == null) {
        softly.assertThat(info.avatar()).as("avatar").isNull();
      } else {
        softly.assertThat(info.avatar()).as("avatar").isEqualTo(new String(entity.getAvatar()));
      }
    });
  }

  @Step("Check the data of the received response matches the data from the database")
  public static void checkEntity(UpdateUserInfoRequest request, UserInfoResponse info) {
    var entity = new UserRepository().findByUsername(info.username());
    assertSoftly(softly -> {
      if (request.firstname() != null) {
        softly.assertThat(entity.getFirstName()).as("firstname").isEqualTo(request.firstname());
      }
      if (request.lastname() != null) {
        softly.assertThat(entity.getLastName()).as("lastname").isEqualTo(request.lastname());
      }
      if (request.avatar() != null) {
        softly.assertThat(entity.getAvatar()).asString().as("avatar").isEqualTo(request.avatar());
      }
    });
  }

  @Step("Check the data of the received response matches the data from the database")
  public static void checkArtistsPageResponse(Response<PageResponse<ArtistInfoResponse>> response) {
    HttpUrl url = response.raw().request().url();
    int page = Integer.parseInt(Objects.requireNonNull(url.queryParameter("page")));
    int size = Integer.parseInt(Objects.requireNonNull(url.queryParameter("size")));
    String name = url.queryParameter("name");
    PageResponse<ArtistInfoResponse> infoPage = Objects.requireNonNull(response.body());
    long count = new ArtistRepository().countAllByName(name);
    checkPageResponse(infoPage, count, size, page);
    List<ArtistEntity> expectedArtistList = new ArtistRepository().finAllByName(page, size, name);
    List<ArtistInfoResponse> actualArtistList = infoPage.content();
    assertThat(actualArtistList).as("size").hasSameSizeAs(expectedArtistList);

    for (int i = 0; i < expectedArtistList.size(); i++) {
      ArtistInfoResponse actualArtist = actualArtistList.get(i);
      ArtistEntity expectedArtist = expectedArtistList.get(i);
      assertThat(actualArtist.id()).as("id").isEqualTo(expectedArtist.getId());
      assertSoftly(softly -> {
        softly.assertThat(actualArtist.name()).as("name").isEqualTo(expectedArtist.getName());
        softly.assertThat(actualArtist.biography()).as("biography").isEqualTo(expectedArtist.getBiography());
        softly.assertThat(actualArtist.photo()).as("photo").isEqualTo(new String(expectedArtist.getPhoto()));
      });
    }
  }

  private static void checkPageResponse(PageResponse<?> infoPage, long count, int size, int page) {
    assertSoftly(softly -> {
      softly.assertThat(infoPage.totalElements()).as("totalElements").isEqualTo(count);
      long expectedTotalPages = Math.ceilDiv(count, size);
      softly.assertThat(infoPage.totalPages()).as("totalPages").isEqualTo(expectedTotalPages);
      softly.assertThat(infoPage.pageNumber()).as("pageNumber").isEqualTo(page);
      softly.assertThat(infoPage.pageSize()).as("pageSize").isEqualTo(size);
      long expectedNumberOfElements = Math.min((count - (page * size)), size);
      softly.assertThat(infoPage.numberOfElements()).as("numberOfElements")
          .isEqualTo(expectedNumberOfElements);
      softly.assertThat(infoPage.first()).as("first").isEqualTo(page == 0);
      softly.assertThat(infoPage.last()).as("last").isEqualTo(count <= ((page + 1) * size));
    });
  }

  @Step("Check the data of the received response matches the data from the database")
  public static void checkMuseumsPageResponse(Response<PageResponse<MuseumInfoResponse>> response) {
    HttpUrl url = response.raw().request().url();
    int page = Integer.parseInt(Objects.requireNonNull(url.queryParameter("page")));
    int size = Integer.parseInt(Objects.requireNonNull(url.queryParameter("size")));
    String title = url.queryParameter("title");
    PageResponse<MuseumInfoResponse> infoPage = Objects.requireNonNull(response.body());
    long count = new MuseumRepository().countAllByTitle(title);
    checkPageResponse(infoPage, count, size, page);
    List<MuseumEntity> expectedMuseumList = new MuseumRepository().finAllByTitle(page, size, title);
    List<MuseumInfoResponse> actualMuseumList = infoPage.content();
    assertThat(actualMuseumList).as("size").hasSameSizeAs(expectedMuseumList);

    for (int i = 0; i < expectedMuseumList.size(); i++) {
      MuseumInfoResponse actualMuseum = actualMuseumList.get(i);
      MuseumEntity expectedMuseum = expectedMuseumList.get(i);
      assertThat(actualMuseum.id()).as("id").isEqualTo(expectedMuseum.getId());
      assertSoftly(softly -> {
        softly.assertThat(actualMuseum.title()).as("title").isEqualTo(expectedMuseum.getTitle());
        softly.assertThat(actualMuseum.description()).as("description").isEqualTo(expectedMuseum.getDescription());
        softly.assertThat(actualMuseum.photo()).as("photo").isEqualTo(new String(expectedMuseum.getPhoto()));
        softly.assertThat(actualMuseum.geo().city()).as("city").isEqualTo(expectedMuseum.getCity());
        softly.assertThat(actualMuseum.geo().country().id()).as("country id").isEqualTo(expectedMuseum.getCountryId());
      });
      checkEntity(actualMuseum.geo().country(), actualMuseum.geo().country().id());
    }
  }

  @Step("Check the data of the received response matches the data from the database")
  public static void checkPaintingsPageResponse(Response<PageResponse<PaintingInfoResponse>> response) {
    HttpUrl url = response.raw().request().url();
    int page = Integer.parseInt(Objects.requireNonNull(url.queryParameter("page")));
    int size = Integer.parseInt(Objects.requireNonNull(url.queryParameter("size")));
    PageResponse<PaintingInfoResponse> infoPage = Objects.requireNonNull(response.body());
    long count = getExpectedPaintingsCount(url);
    checkPageResponse(infoPage, count, size, page);
    List<PaintingEntity> expectedPaintingList = getExpectedPaintingsList(url, page, size);
    List<PaintingInfoResponse> actualPaintingList = infoPage.content();
    assertThat(actualPaintingList).as("size").hasSameSizeAs(expectedPaintingList);

    for (int i = 0; i < expectedPaintingList.size(); i++) {
      PaintingInfoResponse actualPainting = actualPaintingList.get(i);
      PaintingEntity expectedPainting = expectedPaintingList.get(i);
      assertThat(actualPainting.id()).as("id").isEqualTo(expectedPainting.getId());
      assertSoftly(softly -> {
        softly.assertThat(actualPainting.title()).as("title").isEqualTo(expectedPainting.getTitle());
        softly.assertThat(actualPainting.description()).as("description").isEqualTo(expectedPainting.getDescription());
        softly.assertThat(actualPainting.content()).as("content").isEqualTo(new String(expectedPainting.getContent()));
        softly.assertThat(actualPainting.artist().id()).as("artist id").isEqualTo(expectedPainting.getArtistId());
        softly.assertThat(actualPainting.museum().id()).as("museum id").isEqualTo(expectedPainting.getMuseumId());
      });
      checkResponse(actualPainting.museum(), actualPainting.museum().id());
      checkResponse(actualPainting.artist(), actualPainting.artist().id());
    }
  }

  private static long getExpectedPaintingsCount(HttpUrl url) {
    if (url.pathSegments().size() == 4) {
      String artistId = url.pathSegments().getLast();
      return new PaintingRepository().countAllByArtistId(UUID.fromString(artistId));
    } else {
      String title = url.queryParameter("title");
      return new PaintingRepository().countAllByTitle(title);
    }
  }

  private static List<PaintingEntity> getExpectedPaintingsList(HttpUrl url, int page, int size) {
    if (url.pathSegments().size() == 4) {
      String artistId = url.pathSegments().getLast();
      return new PaintingRepository().finAllByArtistId(page, size, UUID.fromString(artistId));
    } else {
      String title = url.queryParameter("title");
      return new PaintingRepository().finAllByTitle(page, size, title);
    }
  }

  @Step("Check the data of the received response matches the data from the database")
  public static void checkCountriesPageResponse(Response<PageResponse<CountryInfoResponse>> response) {
    HttpUrl url = response.raw().request().url();
    int page = Integer.parseInt(Objects.requireNonNull(url.queryParameter("page")));
    int size = Integer.parseInt(Objects.requireNonNull(url.queryParameter("size")));
    PageResponse<CountryInfoResponse> infoPage = Objects.requireNonNull(response.body());
    long count = new CountryRepository().countAll();
    checkPageResponse(infoPage, count, size, page);
    List<CountryEntity> expectedCountryList = new CountryRepository().finAll(page, size);
    List<CountryInfoResponse> actualCountryList = infoPage.content();
    assertThat(actualCountryList).as("size").hasSameSizeAs(expectedCountryList);

    assertSoftly(softly -> {
      for (int i = 0; i < expectedCountryList.size(); i++) {
        CountryInfoResponse actualCountry = actualCountryList.get(i);
        CountryEntity expectedCountry = expectedCountryList.get(i);
        softly.assertThat(actualCountry.id()).as("id").isEqualTo(expectedCountry.getId());
        softly.assertThat(actualCountry.name()).as("name").isEqualTo(expectedCountry.getName());
      }
    });
  }
}
