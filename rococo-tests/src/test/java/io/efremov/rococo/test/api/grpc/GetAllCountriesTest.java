package io.efremov.rococo.test.api.grpc;

import static io.efremov.rococo.config.Constants.INFO_API_TAG;

import io.efremov.rococo.api.validation.GrpcValidation;
import io.efremov.rococo.data.repository.CountryRepository;
import io.efremov.rococo.grpc.CountryPageRequest;
import io.grpc.Status;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(INFO_API_TAG)
@Epic("API")
@Feature("rococo-geo")
@Story("Get all countries")
class GetAllCountriesTest extends BaseGrpcTest {

  private static final int DEFAULT_SIZE = 20;
  private static final CountryPageRequest prototype = CountryPageRequest.newBuilder()
      .setSize(DEFAULT_SIZE)
      .build();

  @Test
  @DisplayName("Get all countries")
  void positiveGetAllCountriesTest() {
    var response = GEO_BLOCKING_STUB.getAllCountries(prototype);

    GrpcValidation.checkPageResponse(prototype, response);
  }

  @Test
  @DisplayName("Get all countries with last page")
  void positiveGetAllCountriesWithLastPageTest() {
    int count = Math.toIntExact(new CountryRepository().countAll());
    var request = CountryPageRequest.newBuilder(prototype)
        .setPage(count / DEFAULT_SIZE)
        .build();
    var response = GEO_BLOCKING_STUB.getAllCountries(request);

    GrpcValidation.checkPageResponse(request, response);
  }

  @Test
  @DisplayName("Attempt to get countries with negative page")
  void negativeGetAllCountriesWithNegativePageTest() {
    var request = CountryPageRequest.newBuilder(prototype)
        .setPage(-1)
        .build();

    GrpcValidation.checkStatusRuntimeException(
        () -> GEO_BLOCKING_STUB.getAllCountries(request),
        Status.Code.INVALID_ARGUMENT,
        "Page index must not be less than zero"
    );
  }

  @Test
  @DisplayName("Attempt to get countries with zero size")
  void negativeGetAllCountriesWithZeroSizeTest() {
    var request = CountryPageRequest.newBuilder(prototype)
        .setSize(0)
        .build();

    GrpcValidation.checkStatusRuntimeException(
        () -> GEO_BLOCKING_STUB.getAllCountries(request),
        Status.Code.INVALID_ARGUMENT,
        "Page size must not be less than one"
    );
  }
}
