package io.efremov.rococo.api.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.efremov.rococo.data.entity.ArtistEntity;
import io.efremov.rococo.data.entity.CountryEntity;
import io.efremov.rococo.data.entity.MuseumEntity;
import io.efremov.rococo.data.entity.PaintingEntity;
import io.efremov.rococo.data.repository.ArtistRepository;
import io.efremov.rococo.data.repository.CountryRepository;
import io.efremov.rococo.data.repository.MuseumRepository;
import io.efremov.rococo.data.repository.PaintingRepository;
import io.efremov.rococo.grpc.ArtistByIdRequest;
import io.efremov.rococo.grpc.ArtistPageRequest;
import io.efremov.rococo.grpc.ArtistPageResponse;
import io.efremov.rococo.grpc.ArtistResponse;
import io.efremov.rococo.grpc.ArtistsResponse;
import io.efremov.rococo.grpc.CountryPageRequest;
import io.efremov.rococo.grpc.CountryPageResponse;
import io.efremov.rococo.grpc.CountryResponse;
import io.efremov.rococo.grpc.CreateArtistRequest;
import io.efremov.rococo.grpc.CreateMuseumRequest;
import io.efremov.rococo.grpc.CreatePaintingRequest;
import io.efremov.rococo.grpc.MuseumByIdRequest;
import io.efremov.rococo.grpc.MuseumPageRequest;
import io.efremov.rococo.grpc.MuseumPageResponse;
import io.efremov.rococo.grpc.MuseumResponse;
import io.efremov.rococo.grpc.PaintingByArtistRequest;
import io.efremov.rococo.grpc.PaintingByIdRequest;
import io.efremov.rococo.grpc.PaintingPageRequest;
import io.efremov.rococo.grpc.PaintingPageResponse;
import io.efremov.rococo.grpc.PaintingResponse;
import io.efremov.rococo.grpc.UpdateArtistRequest;
import io.efremov.rococo.grpc.UpdateMuseumRequest;
import io.efremov.rococo.grpc.UpdatePaintingRequest;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.qameta.allure.Step;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.jspecify.annotations.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GrpcValidation {

  @Step("Check the data of sent input and the received response")
  public static void checkResponse(@NonNull CreateMuseumRequest request, @NonNull MuseumResponse response) {
    assertSoftly(softly -> {
      softly.assertThat(response.getId()).as("id").isNotNull();
      softly.assertThat(response.getTitle()).as("title").isEqualTo(request.getTitle());
      softly.assertThat(response.getDescription()).as("description").isEqualTo(request.getDescription());
      softly.assertThat(response.getPhoto()).as("photo").isEqualTo(request.getPhoto());
      softly.assertThat(response.getCity()).as("city").isEqualTo(request.getCity());
      softly.assertThat(response.getCountryId()).as("country id").isEqualTo(request.getCountryId());
    });
  }

  @Step("Check if all the data was saved correctly in the database")
  public static void checkEntity(@NonNull CreateMuseumRequest request, @NonNull String id) {
    var entity = new MuseumRepository().findById(UUID.fromString(id));
    assertThat(entity).as("entity").isNotNull();
    assertSoftly(softly -> {
      softly.assertThat(entity.getTitle()).as("title").isEqualTo(request.getTitle());
      softly.assertThat(entity.getDescription()).as("description").isEqualTo(request.getDescription());
      softly.assertThat(entity.getCity()).as("city").isEqualTo(request.getCity());
      softly.assertThat(entity.getPhoto()).asString().as("photo").isEqualTo(request.getPhoto());
      softly.assertThat(entity.getCountryId()).as("country id")
          .isEqualTo(UUID.fromString(request.getCountryId()));
    });
  }

  @Step("Check the data of sent input and the received response")
  public static void checkResponse(@NonNull CreateArtistRequest request, @NonNull ArtistResponse response) {
    assertSoftly(softly -> {
      softly.assertThat(response.getId()).as("id").isNotNull();
      softly.assertThat(response.getName()).as("name").isEqualTo(request.getName());
      softly.assertThat(response.getBiography()).as("biography").isEqualTo(request.getBiography());
      softly.assertThat(response.getPhoto()).as("photo").isEqualTo(request.getPhoto());
    });
  }

  @Step("Check if all the data was saved correctly in the database")
  public static void checkEntity(@NonNull CreateArtistRequest request, @NonNull String id) {
    var entity = new ArtistRepository().findById(UUID.fromString(id));
    assertThat(entity).as("entity").isNotNull();
    assertSoftly(softly -> {
      softly.assertThat(entity.getName()).as("name").isEqualTo(request.getName());
      softly.assertThat(entity.getBiography()).as("biography").isEqualTo(request.getBiography());
      softly.assertThat(entity.getPhoto()).asString().as("photo").isEqualTo(request.getPhoto());
    });
  }

  @Step("Check the data of the received response matches the data from the database")
  public static void checkEntity(@NonNull ArtistByIdRequest request, @NonNull ArtistsResponse response) {
    assertThat(request.getIdList()).as("request ids").hasSize(1);
    assertThat(response.getArtistsList()).as("response artists").hasSize(1);
    String id = request.getId(0);
    var entity = new ArtistRepository().findById(UUID.fromString(id));
    assertThat(entity).as("entity").isNotNull();
    ArtistResponse actual = response.getArtists(0);
    assertSoftly(softly -> {
      softly.assertThat(actual.getId()).as("id").isEqualTo(id);
      softly.assertThat(actual.getName()).as("name").isEqualTo(entity.getName());
      softly.assertThat(actual.getBiography()).as("biography").isEqualTo(entity.getBiography());
      softly.assertThat(actual.getPhoto()).as("photo").isEqualTo(new String(entity.getPhoto()));
    });
  }

  @Step("Check the data of sent input and the received response")
  public static void checkResponse(@NonNull CreatePaintingRequest request, @NonNull PaintingResponse response) {
    assertSoftly(softly -> {
      softly.assertThat(response.getId()).as("id").isNotNull();
      softly.assertThat(response.getTitle()).as("title").isEqualTo(request.getTitle());
      softly.assertThat(response.getDescription()).as("description").isEqualTo(request.getDescription());
      softly.assertThat(response.getContent()).as("content").isEqualTo(request.getContent());
      softly.assertThat(response.getArtistId()).as("artistId").isEqualTo(request.getArtistId());
      if (request.hasMuseumId() && !request.getMuseumId().isEmpty()) {
        softly.assertThat(response.getMuseumId()).as("museumId").isEqualTo(request.getMuseumId());
      }
    });
  }

  @Step("Check if all the data was saved correctly in the database")
  public static void checkEntity(@NonNull CreatePaintingRequest request, @NonNull String id) {
    var entity = new PaintingRepository().findById(UUID.fromString(id));
    assertThat(entity).as("entity").isNotNull();
    assertSoftly(softly -> {
      softly.assertThat(entity.getTitle()).as("title").isEqualTo(request.getTitle());
      softly.assertThat(entity.getDescription()).as("description").isEqualTo(request.getDescription());
      softly.assertThat(entity.getContent()).asString().as("content").isEqualTo(request.getContent());
      softly.assertThat(entity.getArtistId()).as("artistId")
          .isEqualTo(UUID.fromString(request.getArtistId()));
      if (request.hasMuseumId() && !request.getMuseumId().isEmpty()) {
        softly.assertThat(entity.getMuseumId()).as("museumId")
            .isEqualTo(UUID.fromString(request.getMuseumId()));
      }
    });
  }

  @Step("Check the page response")
  public static void checkPageResponse(ArtistPageRequest request, @NonNull ArtistPageResponse response) {
    int page = request.getPage();
    int size = request.getSize();
    String name = request.getName();
    long count = new ArtistRepository().countAllByName(name);
    checkPageResponse(response, count, size, page);
    List<ArtistEntity> expectedArtistList = new ArtistRepository().finAllByName(page, size, name);
    List<ArtistResponse> actualArtistList = response.getContentList();
    assertThat(actualArtistList).as("size").hasSameSizeAs(expectedArtistList);

    for (int i = 0; i < expectedArtistList.size(); i++) {
      ArtistResponse actualArtist = actualArtistList.get(i);
      ArtistEntity expectedArtist = expectedArtistList.get(i);
      assertThat(actualArtist.getId()).as("id").isEqualTo(expectedArtist.getId().toString());
      assertSoftly(softly -> {
        softly.assertThat(actualArtist.getName()).as("name").isEqualTo(expectedArtist.getName());
        softly.assertThat(actualArtist.getBiography()).as("biography").isEqualTo(expectedArtist.getBiography());
        softly.assertThat(actualArtist.getPhoto()).as("photo").isEqualTo(new String(expectedArtist.getPhoto()));
      });
    }
  }

  private static void checkPageResponse(@NonNull ArtistPageResponse response, long count, int size, int page) {
    assertSoftly(softly -> {
      softly.assertThat(response.getTotalElements()).as("totalElements").isEqualTo(count);
      long expectedTotalPages = Math.ceilDiv(count, size);
      softly.assertThat(response.getTotalPages()).as("totalPages").isEqualTo(expectedTotalPages);
      softly.assertThat(response.getPageNumber()).as("pageNumber").isEqualTo(page);
      softly.assertThat(response.getPageSize()).as("pageSize").isEqualTo(size);
      long expectedNumberOfElements = Math.min((count - (page * size)), size);
      softly.assertThat(response.getNumberOfElements()).as("numberOfElements")
          .isEqualTo(expectedNumberOfElements);
      softly.assertThat(response.getFirst()).as("first").isEqualTo(page == 0);
      softly.assertThat(response.getLast()).as("last").isEqualTo(count <= ((page + 1) * size));
    });
  }

  @Step("Check the page response")
  public static void checkPageResponse(MuseumPageRequest request, @NonNull MuseumPageResponse response) {
    int page = request.getPage();
    int size = request.getSize();
    String title = request.getTitle();
    long count = new MuseumRepository().countAllByTitle(title);
    checkPageResponse(response, count, size, page);
    List<MuseumEntity> expectedMuseumList = new MuseumRepository().finAllByTitle(page, size, title);
    List<MuseumResponse> actualMuseumList = response.getContentList();
    assertThat(actualMuseumList).as("size").hasSameSizeAs(expectedMuseumList);

    for (int i = 0; i < expectedMuseumList.size(); i++) {
      MuseumResponse actualMuseum = actualMuseumList.get(i);
      MuseumEntity expectedMuseum = expectedMuseumList.get(i);
      assertThat(actualMuseum.getId()).as("id").isEqualTo(expectedMuseum.getId().toString());
      assertSoftly(softly -> {
        softly.assertThat(actualMuseum.getTitle()).as("title").isEqualTo(expectedMuseum.getTitle());
        softly.assertThat(actualMuseum.getDescription()).as("description").isEqualTo(expectedMuseum.getDescription());
        softly.assertThat(actualMuseum.getPhoto()).as("photo").isEqualTo(new String(expectedMuseum.getPhoto()));
        softly.assertThat(actualMuseum.getCity()).as("city").isEqualTo(expectedMuseum.getCity());
        softly.assertThat(actualMuseum.getCountryId()).as("country id")
            .isEqualTo(expectedMuseum.getCountryId().toString());
      });
    }
  }

  private static void checkPageResponse(@NonNull MuseumPageResponse response, long count, int size, int page) {
    assertSoftly(softly -> {
      softly.assertThat(response.getTotalElements()).as("totalElements").isEqualTo(count);
      long expectedTotalPages = Math.ceilDiv(count, size);
      softly.assertThat(response.getTotalPages()).as("totalPages").isEqualTo(expectedTotalPages);
      softly.assertThat(response.getPageNumber()).as("pageNumber").isEqualTo(page);
      softly.assertThat(response.getPageSize()).as("pageSize").isEqualTo(size);
      long expectedNumberOfElements = Math.min((count - (page * size)), size);
      softly.assertThat(response.getNumberOfElements()).as("numberOfElements")
          .isEqualTo(expectedNumberOfElements);
      softly.assertThat(response.getFirst()).as("first").isEqualTo(page == 0);
      softly.assertThat(response.getLast()).as("last").isEqualTo(count <= ((page + 1) * size));
    });
  }

  @Step("Check the data of the received response matches the data from the database")
  public static void checkEntity(@NonNull CountryResponse response, @NonNull String id) {
    var entity = new CountryRepository().findById(UUID.fromString(id));
    assertThat(entity).as("entity").isNotNull();
    assertSoftly(softly -> {
      softly.assertThat(response.getId()).as("name").isEqualTo(id);
      softly.assertThat(response.getName()).as("title").isEqualTo(entity.getName());
    });
  }

  @Step("Check the page response")
  public static void checkPageResponse(CountryPageRequest request, @NonNull CountryPageResponse response) {
    int page = request.getPage();
    int size = request.getSize();
    long count = new CountryRepository().countAll();
    checkPageResponse(response, count, size, page);
    List<CountryEntity> expectedCountryList = new CountryRepository().finAll(page, size);
    List<CountryResponse> actualCountryList = response.getContentList();
    assertThat(actualCountryList).as("size").hasSameSizeAs(expectedCountryList);

    assertSoftly(softly -> {
      for (int i = 0; i < expectedCountryList.size(); i++) {
        CountryResponse actualCountry = actualCountryList.get(i);
        CountryEntity expectedCountry = expectedCountryList.get(i);
        softly.assertThat(actualCountry.getId()).as("id").isEqualTo(expectedCountry.getId().toString());
        softly.assertThat(actualCountry.getName()).as("name").isEqualTo(expectedCountry.getName());
      }
    });
  }

  private static void checkPageResponse(@NonNull CountryPageResponse response, long count, int size, int page) {
    assertSoftly(softly -> {
      softly.assertThat(response.getTotalElements()).as("totalElements").isEqualTo(count);
      long expectedTotalPages = Math.ceilDiv(count, size);
      softly.assertThat(response.getTotalPages()).as("totalPages").isEqualTo(expectedTotalPages);
      softly.assertThat(response.getPageNumber()).as("pageNumber").isEqualTo(page);
      softly.assertThat(response.getPageSize()).as("pageSize").isEqualTo(size);
      long expectedNumberOfElements = Math.min((count - (page * size)), size);
      softly.assertThat(response.getNumberOfElements()).as("numberOfElements")
          .isEqualTo(expectedNumberOfElements);
      softly.assertThat(response.getFirst()).as("first").isEqualTo(page == 0);
      softly.assertThat(response.getLast()).as("last").isEqualTo(count <= ((page + 1) * size));
    });
  }

  @Step("Check the data of the received response matches the data from the database")
  public static void checkEntity(@NonNull MuseumByIdRequest request, @NonNull MuseumResponse response) {
    UUID id = UUID.fromString(request.getId());
    var entity = new MuseumRepository().findById(id);
    assertThat(entity).as("entity").isNotNull();
    assertSoftly(softly -> {
      softly.assertThat(response.getId()).as("id").isEqualTo(request.getId());
      softly.assertThat(response.getTitle()).as("title").isEqualTo(entity.getTitle());
      softly.assertThat(response.getDescription()).as("description").isEqualTo(entity.getDescription());
      softly.assertThat(response.getPhoto()).as("photo").isEqualTo(new String(entity.getPhoto()));
      softly.assertThat(response.getCity()).as("city").isEqualTo(entity.getCity());
      softly.assertThat(response.getCountryId()).as("country id").isEqualTo(entity.getCountryId().toString());
    });
  }

  @Step("Check the data of the received response matches the data from the database")
  public static void checkEntity(@NonNull PaintingByIdRequest request, @NonNull PaintingResponse response) {
    UUID id = UUID.fromString(request.getId());
    var entity = new PaintingRepository().findById(id);
    assertThat(entity).as("entity").isNotNull();
    assertSoftly(softly -> {
      softly.assertThat(response.getId()).as("id").isEqualTo(request.getId());
      softly.assertThat(response.getTitle()).as("title").isEqualTo(entity.getTitle());
      softly.assertThat(response.getDescription()).as("description").isEqualTo(entity.getDescription());
      softly.assertThat(response.getContent()).as("content").isEqualTo(new String(entity.getContent()));
      softly.assertThat(response.getArtistId()).as("artist id").isEqualTo(entity.getArtistId().toString());
      if (entity.getMuseumId() != null) {
        softly.assertThat(response.getMuseumId()).as("museum id").isEqualTo(entity.getMuseumId().toString());
      }
    });
  }

  @Step("Check the page response")
  public static void checkPageResponse(PaintingPageRequest request, @NonNull PaintingPageResponse response) {
    int page = request.getPage();
    int size = request.getSize();
    String title = request.hasTitle() ? request.getTitle() : null;
    long count = new PaintingRepository().countAllByTitle(title);
    checkPageResponse(response, count, size, page);
    List<PaintingEntity> expectedList = new PaintingRepository().finAllByTitle(page, size, title);
    List<PaintingResponse> actualList = response.getContentList();
    assertThat(actualList).as("size").hasSameSizeAs(expectedList);
    for (int i = 0; i < expectedList.size(); i++) {
      PaintingResponse actual = actualList.get(i);
      PaintingEntity expected = expectedList.get(i);
      assertThat(actual.getId()).as("id").isEqualTo(expected.getId().toString());
      assertSoftly(softly -> {
        softly.assertThat(actual.getTitle()).as("title").isEqualTo(expected.getTitle());
        softly.assertThat(actual.getDescription()).as("description").isEqualTo(expected.getDescription());
        softly.assertThat(actual.getContent()).as("content").isEqualTo(new String(expected.getContent()));
        softly.assertThat(actual.getArtistId()).as("artist id").isEqualTo(expected.getArtistId().toString());
        if (expected.getMuseumId() != null) {
          softly.assertThat(actual.getMuseumId()).as("museum id").isEqualTo(expected.getMuseumId().toString());
        }
      });
    }
  }

  private static void checkPageResponse(@NonNull PaintingPageResponse response, long count, int size, int page) {
    assertSoftly(softly -> {
      softly.assertThat(response.getTotalElements()).as("totalElements").isEqualTo(count);
      long expectedTotalPages = Math.ceilDiv(count, size);
      softly.assertThat(response.getTotalPages()).as("totalPages").isEqualTo(expectedTotalPages);
      softly.assertThat(response.getPageNumber()).as("pageNumber").isEqualTo(page);
      softly.assertThat(response.getPageSize()).as("pageSize").isEqualTo(size);
      long expectedNumberOfElements = Math.min((count - (page * size)), size);
      softly.assertThat(response.getNumberOfElements()).as("numberOfElements")
          .isEqualTo(expectedNumberOfElements);
      softly.assertThat(response.getFirst()).as("first").isEqualTo(page == 0);
      softly.assertThat(response.getLast()).as("last").isEqualTo(count <= ((page + 1) * size));
    });
  }

  @Step("Check the page response")
  public static void checkPageResponse(PaintingByArtistRequest request, @NonNull PaintingPageResponse response) {
    int page = request.getPage();
    int size = request.getSize();
    UUID artistId = UUID.fromString(request.getArtistId());
    long count = new PaintingRepository().countAllByArtistId(artistId);
    checkPageResponse(response, count, size, page);
    List<PaintingEntity> expectedList = new PaintingRepository().finAllByArtistId(page, size, artistId);
    List<PaintingResponse> actualList = response.getContentList();
    assertThat(actualList).as("size").hasSameSizeAs(expectedList);
    for (int i = 0; i < expectedList.size(); i++) {
      PaintingResponse actual = actualList.get(i);
      PaintingEntity expected = expectedList.get(i);
      assertThat(actual.getId()).as("id").isEqualTo(expected.getId().toString());
      assertSoftly(softly -> {
        softly.assertThat(actual.getTitle()).as("title").isEqualTo(expected.getTitle());
        softly.assertThat(actual.getDescription()).as("description").isEqualTo(expected.getDescription());
        softly.assertThat(actual.getContent()).as("content").isEqualTo(new String(expected.getContent()));
        softly.assertThat(actual.getArtistId()).as("artist id").isEqualTo(expected.getArtistId().toString());
      });
    }
  }

  @Step("Check the data of sent input and the received response")
  public static void checkResponse(@NonNull UpdateArtistRequest request, @NonNull ArtistResponse response) {
    assertSoftly(softly -> {
      softly.assertThat(response.getId()).as("id").isEqualTo(request.getId());
      softly.assertThat(response.getName()).as("name").isEqualTo(request.getName());
      softly.assertThat(response.getBiography()).as("biography").isEqualTo(request.getBiography());
      softly.assertThat(response.getPhoto()).as("photo").isEqualTo(request.getPhoto());
    });
  }

  @Step("Check if all the data was saved correctly in the database")
  public static void checkEntity(@NonNull UpdateArtistRequest request, @NonNull String id) {
    var entity = new ArtistRepository().findById(UUID.fromString(id));
    assertThat(entity).as("entity").isNotNull();
    assertSoftly(softly -> {
      softly.assertThat(entity.getName()).as("name").isEqualTo(request.getName());
      softly.assertThat(entity.getBiography()).as("biography").isEqualTo(request.getBiography());
      softly.assertThat(entity.getPhoto()).asString().as("photo").isEqualTo(request.getPhoto());
    });
  }

  @Step("Check the data of sent input and the received response")
  public static void checkResponse(@NonNull UpdateMuseumRequest request, @NonNull MuseumResponse response) {
    assertSoftly(softly -> {
      softly.assertThat(response.getId()).as("id").isEqualTo(request.getId());
      softly.assertThat(response.getTitle()).as("title").isEqualTo(request.getTitle());
      softly.assertThat(response.getDescription()).as("description").isEqualTo(request.getDescription());
      softly.assertThat(response.getPhoto()).as("photo").isEqualTo(request.getPhoto());
      softly.assertThat(response.getCity()).as("city").isEqualTo(request.getCity());
      softly.assertThat(response.getCountryId()).as("country id").isEqualTo(request.getCountryId());
    });
  }

  @Step("Check if all the data was saved correctly in the database")
  public static void checkEntity(@NonNull UpdateMuseumRequest request, @NonNull String id) {
    var entity = new MuseumRepository().findById(UUID.fromString(id));
    assertThat(entity).as("entity").isNotNull();
    assertSoftly(softly -> {
      softly.assertThat(entity.getTitle()).as("title").isEqualTo(request.getTitle());
      softly.assertThat(entity.getDescription()).as("description").isEqualTo(request.getDescription());
      softly.assertThat(entity.getCity()).as("city").isEqualTo(request.getCity());
      softly.assertThat(entity.getPhoto()).asString().as("photo").isEqualTo(request.getPhoto());
      softly.assertThat(entity.getCountryId()).as("country id")
          .isEqualTo(UUID.fromString(request.getCountryId()));
    });
  }

  @Step("Check the data of sent input and the received response")
  public static void checkResponse(@NonNull UpdatePaintingRequest request, @NonNull PaintingResponse response) {
    assertSoftly(softly -> {
      softly.assertThat(response.getId()).as("id").isEqualTo(request.getId());
      softly.assertThat(response.getTitle()).as("title").isEqualTo(request.getTitle());
      softly.assertThat(response.getDescription()).as("description").isEqualTo(request.getDescription());
      softly.assertThat(response.getContent()).as("content").isEqualTo(request.getContent());
      softly.assertThat(response.getArtistId()).as("artist id").isEqualTo(request.getArtistId());
      if (request.hasMuseumId() && !request.getMuseumId().isEmpty()) {
        softly.assertThat(response.getMuseumId()).as("museum id").isEqualTo(request.getMuseumId());
      }
    });
  }

  @Step("Check if all the data was saved correctly in the database")
  public static void checkEntity(@NonNull UpdatePaintingRequest request, @NonNull String id) {
    var entity = new PaintingRepository().findById(UUID.fromString(id));
    assertThat(entity).as("entity").isNotNull();
    assertSoftly(softly -> {
      softly.assertThat(entity.getTitle()).as("title").isEqualTo(request.getTitle());
      softly.assertThat(entity.getDescription()).as("description").isEqualTo(request.getDescription());
      softly.assertThat(entity.getContent()).asString().as("content").isEqualTo(request.getContent());
      softly.assertThat(entity.getArtistId()).as("artist id")
          .isEqualTo(UUID.fromString(request.getArtistId()));
      if (request.hasMuseumId() && !request.getMuseumId().isEmpty()) {
        softly.assertThat(entity.getMuseumId()).as("museum id")
            .isEqualTo(UUID.fromString(request.getMuseumId()));
      }
    });
  }

  @Step("Check status runtime exception")
  public static void checkStatusRuntimeException(ThrowingCallable call, Status.Code code, String description) {
    assertThatThrownBy(call)
        .isInstanceOf(StatusRuntimeException.class)
        .satisfies(e -> {
          Status status = ((StatusRuntimeException) e).getStatus();
          assertThat(status.getCode()).as("code").isEqualTo(code);
          assertThat(status.getDescription()).as("description").isEqualTo(description);
        });
  }
}
