package io.efremov.rococo.service;

import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;

import io.efremov.rococo.api.core.RetrofitLoggingInterceptor;
import io.efremov.rococo.api.core.ThreadSafeCookieStore;
import io.efremov.rococo.config.Config;
import io.efremov.rococo.util.JsonUtils;
import io.qameta.allure.okhttp3.AllureOkHttp3;
import java.net.CookieManager;
import java.net.CookiePolicy;
import okhttp3.Interceptor;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public abstract class RestClient {

  protected static final Config CFG = Config.getInstance();

  private final Retrofit retrofit;

  public RestClient(String baseUrl, boolean followRedirect, @Nullable Interceptor... interceptors) {
    OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
        .followRedirects(followRedirect);

    if (isNotEmpty(interceptors)) {
      for (Interceptor interceptor : interceptors) {
        clientBuilder.addNetworkInterceptor(interceptor);
      }
    }
    OkHttpClient client = clientBuilder
        .addNetworkInterceptor(new AllureOkHttp3()
            .setRequestTemplate("request-attachment.ftl")
            .setResponseTemplate("response-attachment.ftl"))
        .addNetworkInterceptor(new RetrofitLoggingInterceptor())
        .cookieJar(new JavaNetCookieJar(
            new CookieManager(
                ThreadSafeCookieStore.INSTANCE,
                CookiePolicy.ACCEPT_ALL)))
        .build();

    this.retrofit = new Retrofit.Builder()
        .client(client)
        .baseUrl(baseUrl)
        .addConverterFactory(JacksonConverterFactory.create(JsonUtils.getMapper()))
        .build();
  }

  @NonNull
  protected <T> T create(final Class<T> service) {
    return this.retrofit.create(service);
  }
}
