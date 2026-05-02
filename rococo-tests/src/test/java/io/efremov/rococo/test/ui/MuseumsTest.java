package io.efremov.rococo.test.ui;

import static io.efremov.rococo.config.Constants.UI_TAG;

import io.efremov.rococo.jupiter.annotation.AnyMuseum;
import io.efremov.rococo.jupiter.annotation.Authentication;
import io.efremov.rococo.jupiter.annotation.NewMuseum;
import io.efremov.rococo.jupiter.annotation.meta.WebTest;
import io.efremov.rococo.model.MuseumInfoResponse;
import io.efremov.rococo.page.MuseumDetailPage;
import io.efremov.rococo.page.MuseumsPage;
import io.qameta.allure.Epic;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(UI_TAG)
@Epic("UI")
@Story("Museums")
@WebTest
class MuseumsTest {

  @Test
  @Authentication
  @DisplayName("Create museum")
  void createMuseumTest() {
    MuseumsPage.open()
        .verifyPageLoaded()
        .verifyUserIsLoggedIn()
        .createNewMuseum()
        .verifyMuseumIsCreated();
  }

  @Test
  @Authentication
  @NewMuseum
  @DisplayName("Update museum")
  void updateMuseumTest(MuseumInfoResponse museum) {
    MuseumDetailPage.open(museum.id())
        .verifyPageLoaded()
        .verifyUserIsLoggedIn()
        .updateMuseum()
        .verifyMuseumIsUpdated();
  }

  @Test
  @AnyMuseum
  @DisplayName("Open museum details")
  void openMuseumDetailsTest(MuseumInfoResponse museum) {
    MuseumsPage.open()
        .verifyPageLoaded()
        .openMuseumByName(museum.title())
        .verifyEditingIsNotAvailable()
        .verifyMuseumInfo(museum);
  }
}

