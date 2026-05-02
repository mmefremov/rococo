package io.efremov.rococo.test.ui;

import static io.efremov.rococo.config.Constants.UI_TAG;

import io.efremov.rococo.jupiter.annotation.AnyArtist;
import io.efremov.rococo.jupiter.annotation.AnyPainting;
import io.efremov.rococo.jupiter.annotation.Authentication;
import io.efremov.rococo.jupiter.annotation.NewPainting;
import io.efremov.rococo.jupiter.annotation.meta.WebTest;
import io.efremov.rococo.model.ArtistInfoResponse;
import io.efremov.rococo.model.PaintingInfoResponse;
import io.efremov.rococo.page.ArtistDetailPage;
import io.efremov.rococo.page.PaintingDetailPage;
import io.efremov.rococo.page.PaintingsPage;
import io.qameta.allure.Epic;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(UI_TAG)
@Epic("UI")
@Story("Paintings")
@WebTest
class PaintingsTest {

  @Test
  @Authentication
  @DisplayName("Create painting on the 'paintings' page")
  void createPaintingOnPaintingPageTest() {
    PaintingsPage.open()
        .verifyPageLoaded()
        .verifyUserIsLoggedIn()
        .createNewPainting()
        .verifyPaintingIsCreated();
  }

  @Test
  @Authentication
  @AnyArtist
  @DisplayName("Create painting on the 'artist details' page")
  void createPaintingOnArtistDetailPageTest(ArtistInfoResponse artist) {
    ArtistDetailPage.open(artist.id())
        .verifyPageLoaded()
        .verifyUserIsLoggedIn()
        .createNewPainting()
        .verifyPaintingIsCreated();
  }

  @Test
  @Authentication
  @NewPainting
  @DisplayName("Update painting")
  void updatePaintingTest(PaintingInfoResponse painting) {
    PaintingDetailPage.open(painting.id())
        .verifyPageLoaded()
        .verifyUserIsLoggedIn()
        .updatePainting()
        .verifyPaintingIsUpdated();
  }

  @Test
  @AnyPainting
  @DisplayName("Open painting details on the 'paintings' page")
  void openPaintingDetailsOnPaintingsPageTest(PaintingInfoResponse painting) {
    PaintingsPage.open()
        .verifyPageLoaded()
        .openPaintingByTitle(painting.title())
        .verifyEditingIsNotAvailable()
        .verifyPaintingInfo(painting);
  }

  @Test
  @AnyPainting
  @DisplayName("Open painting details on the 'artist details' page")
  void openPaintingDetailsOnArtistDetailPageTest(PaintingInfoResponse painting) {
    ArtistDetailPage.open(painting.artist().id())
        .verifyPageLoaded()
        .openPaintingByTitle(painting.title())
        .verifyEditingIsNotAvailable()
        .verifyPaintingInfo(painting);
  }
}

