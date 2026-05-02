package io.efremov.rococo.service;

import com.fasterxml.jackson.databind.JsonNode;
import io.efremov.rococo.api.AuthApi;
import io.efremov.rococo.api.core.CodeInterceptor;
import io.qameta.allure.Step;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import retrofit2.Response;

@Slf4j
public final class AuthApiClient extends RestClient {

  private final AuthApi authApi;

  public AuthApiClient() {
    super(CFG.authUrl(), true, new CodeInterceptor());
    this.authApi = create(AuthApi.class);
  }

  @Step("Get register form")
  public Response<ResponseBody> getRegisterForm() {
    try {
      return authApi.getRegisterForm().execute();
    } catch (IOException e) {
      throw new RuntimeException("Failed to get register form", e);
    }
  }

  @Step("Register user")
  public Response<Void> register(String username, String password, String csrf) {
    try {
      return authApi.register(username, password, password, csrf).execute();
    } catch (IOException e) {
      throw new RuntimeException("Failed to register user", e);
    }
  }

  @Step("Authorize user")
  public Response<ResponseBody> authorize(String clientId, String redirectUri, String codeChallenge) {
    try {
      return authApi.authorize("code", clientId, "openid", redirectUri, codeChallenge, "S256").execute();
    } catch (IOException e) {
      throw new RuntimeException("Failed to authorize user", e);
    }
  }

  @Step("Login user")
  public Response<Void> login(String username, String password, String csrf) {
    try {
      return authApi.login(username, password, csrf).execute();
    } catch (IOException e) {
      throw new RuntimeException("Failed to login user", e);
    }
  }

  @Step("Get access token")
  public Response<JsonNode> token(String clientId, String redirectUri, String codeVerifier) {
    try {
      return authApi.token(
              clientId,
              redirectUri,
              "authorization_code",
              CodeInterceptor.getCode(),
              codeVerifier)
          .execute();
    } catch (IOException e) {
      throw new RuntimeException("Failed to get access token", e);
    }
  }
}
