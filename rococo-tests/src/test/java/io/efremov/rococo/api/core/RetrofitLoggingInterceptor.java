package io.efremov.rococo.api.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.efremov.rococo.util.JsonUtils;
import java.io.IOException;
import java.net.HttpCookie;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class RetrofitLoggingInterceptor implements Interceptor {

  private static final String SEPARATOR = "\n";
  private static final String TAB = "\t";
  private static final String NONE = "<none>";
  private static final int MAX_FIELD_LENGTH = 1_000;
  private static final int TRUNCATED_PREFIX_LENGTH = 15;
  private static final Set<String> IGNORED_HEADERS = Set.of(
      "host", "connection", "accept-encoding", "user-agent", "cookie"
  );

  @NotNull
  @Override
  public Response intercept(@NotNull Chain chain) throws IOException {
    Request request = chain.request();
    logRequest(request);
    Response response = chain.proceed(request);
    response = logResponse(response);
    return response;
  }

  private void logRequest(Request request) {
    StringBuilder sb = new StringBuilder();
    sb.append("Request method:").append(TAB).append(request.method()).append(SEPARATOR);
    sb.append("Request URI:").append(TAB).append(request.url()).append(SEPARATOR);

    Map<String, String> queryParams = parseQueryParams(request);
    sb.append("Query params:").append(TAB).append(prettyMap(queryParams)).append(SEPARATOR);

    Map<String, String> formParams = parseFormParams(request);
    sb.append("Form params:").append(TAB).append(prettyMap(formParams)).append(SEPARATOR);

    Map<String, String> headersMap = toHeadersMap(request.headers());
    sb.append("Headers:").append(TAB).append(prettyMap(headersMap)).append(SEPARATOR);

    Map<String, String> cookiesMap = toCookiesMap(request.url().toString());
    sb.append("Cookies:").append(TAB).append(prettyMap(cookiesMap)).append(SEPARATOR);

    // Body
    RequestBody body = request.body();
    if (body != null) {
      try {
        Buffer buffer = new Buffer();
        body.writeTo(buffer);
        String bodyContent = buffer.readString(StandardCharsets.UTF_8);
        sb.append("Body:").append(TAB);
        sb.append(prettyBody(bodyContent, body.contentType())).append(SEPARATOR);
      } catch (IOException e) {
        sb.append("Body:").append(TAB).append("[failed to read body]").append(SEPARATOR);
      }
    } else {
      sb.append("Body:").append(TAB).append(NONE).append(SEPARATOR);
    }
    log.info("\n{}", sb);
  }

  private Response logResponse(Response response) {
    StringBuilder sb = new StringBuilder();
    sb.append(response.protocol().toString().toUpperCase()).append(" ")
        .append(response.code());
    sb.append(" ").append(response.message()).append(SEPARATOR);
    Map<String, String> headersMap = toHeadersMap(response.headers());
    sb.append("Headers:").append(TAB).append(prettyMap(headersMap)).append(SEPARATOR);
    sb.append("Body:").append(TAB);
    ResponseBody body = response.body();

    try {
      MediaType contentType = body.contentType();
      String bodyContent = body.string();
      sb.append(prettyBody(bodyContent, contentType)).append(SEPARATOR);
      log.info("\n{}", sb);

      return response.newBuilder()
          .body(ResponseBody.create(bodyContent, contentType))
          .build();
    } catch (IOException e) {
      sb.append("[failed to read body]").append(SEPARATOR);
      log.info("\n{}", sb);
      return response;
    }
  }

  private Map<String, String> parseQueryParams(Request request) {
    Map<String, String> params = new LinkedHashMap<>();
    String query = StringUtils.substringAfter(request.url().toString(), "?");
    if (StringUtils.isBlank(query)) {
      return params;
    }
    for (String pair : query.split("&")) {
      String param = StringUtils.substringBefore(pair, "=");
      String value = StringUtils.substringAfter(pair, "=");
      params.put(param, value);
    }
    return params;
  }

  private Map<String, String> parseFormParams(Request request) {
    Map<String, String> params = new LinkedHashMap<>();
    RequestBody body = request.body();
    if (body instanceof FormBody formBody) {
      for (int i = 0; i < formBody.size(); i++) {
        params.put(formBody.encodedName(i), formBody.encodedValue(i));
      }
    }
    return params;
  }

  private Map<String, String> toHeadersMap(Headers headers) {
    Map<String, String> map = new LinkedHashMap<>();
    for (int i = 0; i < headers.size(); i++) {
      String name = headers.name(i);
      if (!IGNORED_HEADERS.contains(name.toLowerCase())) {
        map.put(name, headers.value(i));
      }
    }
    return map;
  }

  private Map<String, String> toCookiesMap(String url) {
    Map<String, String> cookies = new LinkedHashMap<>();
    try {
      URI uri = URI.create(url);
      List<HttpCookie> httpCookies = ThreadSafeCookieStore.INSTANCE.get(uri);
      if (httpCookies.isEmpty()) {
        httpCookies = ThreadSafeCookieStore.INSTANCE.getCookies();
      }
      for (HttpCookie cookie : httpCookies) {
        cookies.put(cookie.getName(), cookie.getValue());
      }
    } catch (IllegalArgumentException e) {
      // ignore
    }
    return cookies;
  }

  private String prettyMap(Map<String, String> map) {
    if (map.isEmpty()) {
      return NONE;
    }
    return JsonUtils.writeValueAsString(map);
  }

  private String prettyBody(String bodyContent, MediaType contentType) {
    if (bodyContent == null || bodyContent.trim().isEmpty()) {
      return NONE;
    }
    if (contentType != null && contentType.subtype().contains("json")) {
      JsonNode node = JsonUtils.readTree(bodyContent);
      JsonNode truncated = truncateLongFields(node);
      return JsonUtils.writeValueAsString(truncated);
    }
    return truncatePlain(bodyContent);
  }

  public static JsonNode truncateLongFields(JsonNode node) {
    if (node.isObject()) {
      truncateObjectFields((ObjectNode) node);
    } else if (node.isArray()) {
      truncateArrayElements(node);
    }
    return node;
  }

  private static void truncateObjectFields(ObjectNode objectNode) {
    objectNode.properties().forEach(entry ->
        truncateNode(entry.getKey(), entry.getValue(), objectNode));
  }

  private static void truncateArrayElements(JsonNode arrayNode) {
    for (int i = 0; i < arrayNode.size(); i++) {
      JsonNode element = arrayNode.get(i);
      if (element.isObject()) {
        truncateObjectFields((ObjectNode) element);
      } else if (element.isArray()) {
        truncateArrayElements(element);
      }
    }
  }

  private static void truncateNode(String fieldName, JsonNode value, ObjectNode objectNode) {
    if (value.isTextual()) {
      truncateTextFieldIfNeeded(fieldName, value.asText(), objectNode);
    } else if (value.isObject()) {
      truncateObjectFields((ObjectNode) value);
    } else if (value.isArray()) {
      truncateArrayElements(value);
    }
  }

  private static void truncateTextFieldIfNeeded(String fieldName, String text, ObjectNode objectNode) {
    if (text.length() > MAX_FIELD_LENGTH) {
      String truncated = text.substring(0, TRUNCATED_PREFIX_LENGTH)
          + "... [truncated, original size: " + text.length() + " chars]";
      objectNode.put(fieldName, truncated);
    }
  }

  private String truncatePlain(String content) {
    if (content.length() > MAX_FIELD_LENGTH) {
      return content.substring(0, TRUNCATED_PREFIX_LENGTH)
          + "... [truncated, original size: " + content.length() + " chars]";
    }
    return content;
  }
}
