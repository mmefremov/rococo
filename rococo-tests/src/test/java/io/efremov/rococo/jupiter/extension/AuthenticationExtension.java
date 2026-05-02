package io.efremov.rococo.jupiter.extension;


import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import io.efremov.rococo.api.core.ThreadSafeCookieStore;
import io.efremov.rococo.config.Config;
import io.efremov.rococo.jupiter.annotation.Authentication;
import io.efremov.rococo.jupiter.annotation.meta.WebTest;
import io.efremov.rococo.provider.AuthProvider;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.Cookie;

public class AuthenticationExtension implements BeforeEachCallback {

  public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(
      AuthenticationExtension.class);

  private static class TokenHolder {

    static final String TOKEN = AuthProvider.getNewUserAuthToken();
    static final String SESSION_ID = ThreadSafeCookieStore.INSTANCE.sessionId();
  }

  @Override
  public void beforeEach(ExtensionContext context) {
    Authentication authentication = context.getRequiredTestMethod().getAnnotation(Authentication.class);
    if (authentication == null) {
      return;
    }
    setToken();
    if (context.getRequiredTestClass().isAnnotationPresent(WebTest.class)) {
      Selenide.open(Config.getInstance().frontUrl());
      Selenide.localStorage().setItem("id_token", TokenHolder.TOKEN.replace("Bearer ", ""));
      WebDriverRunner.getWebDriver().manage().addCookie(
          new Cookie("JSESSIONID", TokenHolder.SESSION_ID));
    }
  }

  public static void setToken() {
    TestMethodContextExtension.context().getStore(NAMESPACE).put("token", TokenHolder.TOKEN);
  }

  public static void setToken(String token) {
    TestMethodContextExtension.context().getStore(NAMESPACE).put("token", token);
  }

  public static String getToken() {
    return TestMethodContextExtension.context().getStore(NAMESPACE).get("token", String.class);
  }

  public static String getGlobalToken() {
    return TokenHolder.TOKEN;
  }
}
