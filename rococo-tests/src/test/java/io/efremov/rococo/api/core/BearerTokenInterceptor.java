package io.efremov.rococo.api.core;

import io.efremov.rococo.jupiter.extension.AuthenticationExtension;
import io.efremov.rococo.jupiter.extension.TestMethodContextExtension;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jspecify.annotations.NonNull;

public class BearerTokenInterceptor implements Interceptor {

  @Override
  public @NonNull Response intercept(@NonNull Chain chain) throws IOException {
    Request request = chain.request();
    String token = null;
    if (TestMethodContextExtension.context() != null) {
      token = AuthenticationExtension.getToken();
    } else if ("POST".equals(request.method())) {
      token = AuthenticationExtension.getGlobalToken();
    }
    if (token != null) {
      request = chain.request().newBuilder()
          .header("Authorization", token)
          .build();
    }
    return chain.proceed(request);
  }
}
