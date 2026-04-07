package io.efremov.rococo.page;

import com.codeborne.selenide.Selenide;
import io.efremov.rococo.config.Config;
import io.qameta.allure.Step;

public class BasePage {

  protected static final String FRONT_URL = Config.getInstance().frontUrl();

  @Step("Get current URL")
  public static String getCurrentUrl() {
    return com.codeborne.selenide.WebDriverRunner.getWebDriver().getCurrentUrl();
  }

  @Step("Refresh page")
  public static void refresh() {
    Selenide.refresh();
  }
}
