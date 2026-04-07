package io.efremov.rococo.test.ui;

import static io.efremov.rococo.config.Constants.UI_TAG;

import io.efremov.rococo.jupiter.annotation.Authentication;
import io.efremov.rococo.jupiter.annotation.meta.WebTest;
import io.efremov.rococo.page.MainPage;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(UI_TAG)
@Epic("UI")
@Story("Main page")
@Feature("Main page")
@WebTest
class MainPageTest {

  @Test
  @Authentication
  @DisplayName("main page")
  void mainPageTest() {
    MainPage.open()
        .verifyPageLoaded();
  }
}

