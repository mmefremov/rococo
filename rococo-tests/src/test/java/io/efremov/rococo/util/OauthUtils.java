package io.efremov.rococo.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import okhttp3.ResponseBody;
import org.jspecify.annotations.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OauthUtils {

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();
  private static final Base64.Encoder BASE_64_ENCODER = Base64.getUrlEncoder().withoutPadding();
  private static final Pattern CSRF_PATTERN = Pattern.compile("<input[^>]+name=\"_csrf\"[^>]+value=\"([^\"]+)\"");


  @NonNull
  public static String findCsrfValue(ResponseBody responseBody) {
    try {
      String html = responseBody.string();
      Matcher matcher = CSRF_PATTERN.matcher(html);
      if (!matcher.find()) {
        throw new IllegalStateException("CSRF token not found");
      }
      return matcher.group(1);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Nonnull
  public static String generateCodeVerifier() {
    byte[] codeVerifier = new byte[32];
    SECURE_RANDOM.nextBytes(codeVerifier);
    return BASE_64_ENCODER.encodeToString(codeVerifier);
  }

  @Nonnull
  public static String generateCodeChallenge(String codeVerifier) {
    byte[] bytes = codeVerifier.getBytes(StandardCharsets.US_ASCII);
    MessageDigest messageDigest = null;
    try {
      messageDigest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
    byte[] digest = messageDigest.digest(bytes);
    return BASE_64_ENCODER.encodeToString(digest);
  }
}
