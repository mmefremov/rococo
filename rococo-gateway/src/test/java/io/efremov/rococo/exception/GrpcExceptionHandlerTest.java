package io.efremov.rococo.exception;

import static org.assertj.core.api.Assertions.assertThat;

import io.grpc.Status;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class GrpcExceptionHandlerTest {

  private final GrpcExceptionHandler handler = new GrpcExceptionHandler();

  @ParameterizedTest
  @CsvSource({"NOT_FOUND,404", "ALREADY_EXISTS,409", "INVALID_ARGUMENT,400", "UNAUTHENTICATED,401",
      "PERMISSION_DENIED,403", "DEADLINE_EXCEEDED,504", "INTERNAL,500", "UNAVAILABLE,500", "UNKNOWN,500"})
  void handleGrpcException_mapsStatusAndPreservesDescription(Status.Code code, int expectedStatus) {
    var result = handler.handleGrpcException(
        Status.fromCode(code).withDescription("Error details").asRuntimeException());

    assertThat(result.getStatusCode().value()).as("HTTP status").isEqualTo(expectedStatus);
    assertThat(result.getBody()).as("error body").isEqualTo(Map.of("error", "Error details"));
  }

  @ParameterizedTest
  @EnumSource(Status.Code.class)
  void handleGrpcException_withoutDescription_usesCodeName(Status.Code code) {
    var result = handler.handleGrpcException(Status.fromCode(code).asRuntimeException());

    assertThat(result.getBody()).as("fallback error").isEqualTo(Map.of("error", code.name()));
  }

  @Test
  void handleBadRequest_withMessage_returnsBadRequest() {
    var result = handler.handleBadRequest(new IllegalArgumentException("Invalid UUID"));

    assertThat(result.getStatusCode().value()).as("HTTP status").isEqualTo(400);
    assertThat(result.getBody()).as("error body").isEqualTo(Map.of("error", "Invalid UUID"));
  }

  @Test
  void handleBadRequest_withoutMessage_usesFallback() {
    var result = handler.handleBadRequest(new IllegalArgumentException());

    assertThat(result.getStatusCode().value()).as("HTTP status").isEqualTo(400);
    assertThat(result.getBody()).as("error body").isEqualTo(Map.of("error", "Bad request"));
  }
}
