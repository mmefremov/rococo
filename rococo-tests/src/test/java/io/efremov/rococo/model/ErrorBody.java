package io.efremov.rococo.model;

import io.efremov.rococo.util.JsonUtils;
import java.io.IOException;
import java.time.OffsetDateTime;
import okhttp3.ResponseBody;

public record ErrorBody(
    OffsetDateTime timestamp,
    String status,
    String error,
    String path
) {

  public static String getErrorFromResponseBody(ResponseBody responseBody) {
    try {
      return JsonUtils.readValue(responseBody.string(), ErrorBody.class).error();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
