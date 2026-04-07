package io.efremov.rococo.api.core;

import io.efremov.rococo.config.Config;
import java.io.IOException;
import java.util.Objects;
import okhttp3.Interceptor;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;

public class CodeInterceptor implements Interceptor {

  private static final String DOCKER_FRONT_URL = "http://rococo-client";
  private static final ThreadLocal<String> threadCode = new ThreadLocal<>();

  public static String getCode() {
    String code = threadCode.get();
    threadCode.remove();
    return code;
  }

  @Override
  public @NonNull Response intercept(Chain chain) throws IOException {
    Response response = chain.proceed(chain.request());
    if (response.isRedirect()) {
      String location = Objects.requireNonNull(response.header("Location"));
      if ("hybrid".equals(System.getProperty("test.env")) && location.startsWith(DOCKER_FRONT_URL)) {
        String frontUrl = Config.getInstance().frontUrl();
        String rewritten = location.replace(DOCKER_FRONT_URL, frontUrl);
        response = response.newBuilder()
            .header("Location", rewritten)
            .build();
        location = rewritten;
      }
      if (location.contains("login?error")) {
        throw new RuntimeException("Login error");
      }
      if (location.contains("code=")) {
        String code = StringUtils.substringAfter(location, "code=");
        threadCode.set(code);
        return response.newBuilder()
            .removeHeader("Location")
            .code(200)
            .build();
      }
    }
    return response;
  }
}
