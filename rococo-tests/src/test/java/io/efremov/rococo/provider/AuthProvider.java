package io.efremov.rococo.provider;

import static io.efremov.rococo.config.Constants.DEFAULT_PASSWORD;

import com.fasterxml.jackson.databind.JsonNode;
import io.efremov.rococo.api.validation.RestValidation;
import io.efremov.rococo.config.Config;
import io.efremov.rococo.service.AuthApiClient;
import io.efremov.rococo.util.OauthUtils;
import io.efremov.rococo.util.RandomDataUtils;
import java.util.Objects;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class AuthProvider {

  public static String getNewUserAuthToken() {
    AuthApiClient authApiClient = new AuthApiClient();
    String username = RandomDataUtils.randomUsername();
    String password = DEFAULT_PASSWORD;
    Response<ResponseBody> formResponse = authApiClient.getRegisterForm();
    RestValidation.responseMustHaveSuccessfulStatus(formResponse);
    String csrf = OauthUtils.findCsrfValue(Objects.requireNonNull(formResponse.body()));

    Response<Void> registerResponse = authApiClient.register(username, password, csrf);
    RestValidation.responseMustHaveCreatedStatus(registerResponse);

    String codeVerifier = OauthUtils.generateCodeVerifier();
    String codeChallenge = OauthUtils.generateCodeChallenge(codeVerifier);
    String redirectUri = Config.getInstance().frontUrl() + "authorized";
    String clientId = "client";

    Response<ResponseBody> authorizeResponse = authApiClient.authorize(clientId, redirectUri, codeChallenge);
    RestValidation.responseMustHaveSuccessfulStatus(authorizeResponse);

    Response<Void> loginResponse = authApiClient.login(username, password, csrf);
    RestValidation.responseMustHaveSuccessfulStatus(loginResponse);

    Response<JsonNode> tokenResponse = authApiClient.token(clientId, redirectUri, codeVerifier);
    RestValidation.responseMustHaveSuccessfulStatus(tokenResponse);
    JsonNode tokenBody = Objects.requireNonNull(tokenResponse.body());
    return "%s %s".formatted(
        tokenBody.get("token_type").asText(),
        tokenBody.get("id_token").asText());
  }

  public static String getRegisteredUserAuthToken(String username, String password) {
    AuthApiClient authApiClient = new AuthApiClient();
    String codeVerifier = OauthUtils.generateCodeVerifier();
    String codeChallenge = OauthUtils.generateCodeChallenge(codeVerifier);
    String redirectUri = Config.getInstance().frontUrl() + "authorized";
    String clientId = "client";

    Response<ResponseBody> authorizeResponse = authApiClient.authorize(clientId, redirectUri, codeChallenge);
    RestValidation.responseMustHaveSuccessfulStatus(authorizeResponse);
    String csrf = OauthUtils.findCsrfValue(Objects.requireNonNull(authorizeResponse.body()));

    Response<Void> loginResponse = authApiClient.login(username, password, csrf);
    RestValidation.responseMustHaveSuccessfulStatus(loginResponse);

    Response<JsonNode> tokenResponse = authApiClient.token(clientId, redirectUri, codeVerifier);
    RestValidation.responseMustHaveSuccessfulStatus(tokenResponse);
    JsonNode tokenBody = Objects.requireNonNull(tokenResponse.body());
    return "%s %s".formatted(
        tokenBody.get("token_type").asText(),
        tokenBody.get("id_token").asText());
  }
}
