package io.efremov.rococo.test.ui;

import static io.efremov.rococo.config.Constants.UI_TAG;

import io.efremov.rococo.jupiter.annotation.AnyArtist;
import io.efremov.rococo.jupiter.annotation.Authentication;
import io.efremov.rococo.jupiter.annotation.NewArtist;
import io.efremov.rococo.jupiter.annotation.meta.WebTest;
import io.efremov.rococo.model.ArtistInfoResponse;
import io.efremov.rococo.page.ArtistDetailPage;
import io.efremov.rococo.page.ArtistsPage;
import io.qameta.allure.Epic;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(UI_TAG)
@Epic("UI")
@Story("Artists")
@WebTest
class ArtistsTest {

  @Test
  @Authentication
  @DisplayName("Create artist")
  void createArtistTest() {
    ArtistsPage.open()
        .verifyPageLoaded()
        .verifyUserIsLoggedIn()
        .createNewArtist()
        .verifyArtistIsCreated();
  }

  @Test
  @Authentication
  @NewArtist
  @DisplayName("Update artist")
  void updateArtistTest(ArtistInfoResponse artist) {
    ArtistDetailPage.open(artist.id())
        .verifyPageLoaded()
        .verifyUserIsLoggedIn()
        .updateArtist()
        .verifyArtistIsUpdated();
  }

  @Test
  @AnyArtist
  @DisplayName("Open artist details")
  void openArtistDetailsTest(ArtistInfoResponse artist) {
    ArtistsPage.open()
        .verifyPageLoaded()
        .openArtistByName(artist.name())
        .verifyEditingIsNotAvailable()
        .verifyArtistInfo(artist);
  }
}

