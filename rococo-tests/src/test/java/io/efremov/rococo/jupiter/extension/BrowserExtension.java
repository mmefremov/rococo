package io.efremov.rococo.jupiter.extension;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.Allure;
import io.qameta.allure.selenide.AllureSelenide;
import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.LifecycleMethodExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.chrome.ChromeOptions;

public class BrowserExtension implements
    BeforeAllCallback,
    BeforeEachCallback,
    AfterEachCallback,
    TestExecutionExceptionHandler,
    LifecycleMethodExecutionExceptionHandler {

  @Override
  public void beforeAll(ExtensionContext context) {
    Configuration.browser = "chrome";
    Configuration.pageLoadStrategy = "eager";

    ChromeOptions options = new ChromeOptions()
        .addArguments("--lang=ru-RU")
        .addArguments("--no-sandbox");
    options.setExperimentalOption("prefs", Map.of("intl.accept_languages", "ru-RU"));

    if (Objects.equals(System.getProperty("test.env"), "docker")) {
      Configuration.remote = "http://selenoid:4444/wd/hub";
      Configuration.browserVersion = "147.0";
      options.setCapability("selenoid:options",
          Map.of("env", new String[]{"LANG=ru_RU.UTF-8", "LANGUAGE=ru_RU:ru", "LC_ALL=ru_RU.UTF-8"}));
    }
    Configuration.browserCapabilities = options;
  }

  @Override
  public void afterEach(ExtensionContext context) {
    if (WebDriverRunner.hasWebDriverStarted()) {
      Selenide.closeWebDriver();
    }
  }

  @Override
  public void beforeEach(ExtensionContext context) {
    SelenideLogger.addListener("Allure-selenide", new AllureSelenide()
        .savePageSource(false)
        .screenshots(false)
    );
  }

  @Override
  public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
    doScreenshot();
    throw throwable;
  }

  @Override
  public void handleBeforeEachMethodExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
    doScreenshot();
    throw throwable;
  }

  @Override
  public void handleAfterEachMethodExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
    doScreenshot();
    throw throwable;
  }

  private static void doScreenshot() {
    if (WebDriverRunner.hasWebDriverStarted()) {
      Allure.addAttachment(
          "Screen on fail",
          new ByteArrayInputStream(
              ((TakesScreenshot) WebDriverRunner.getWebDriver()).getScreenshotAs(OutputType.BYTES)
          )
      );
    }
  }
}
