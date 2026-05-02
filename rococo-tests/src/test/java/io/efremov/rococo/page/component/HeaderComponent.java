package io.efremov.rococo.page.component;

import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.not;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.SelenideElement;
import io.efremov.rococo.page.ArtistsPage;
import io.efremov.rococo.page.LoginPage;
import io.efremov.rococo.page.MuseumsPage;
import io.efremov.rococo.page.PaintingsPage;
import io.qameta.allure.Step;

public class HeaderComponent {

  private final SelenideElement self = $("div[data-testid='app-bar']");
  private final SelenideElement paintingsLink = self.find("[data-testid='painting-page-link']");
  private final SelenideElement artistsLink = self.find("[data-testid='artist-page-link']");
  private final SelenideElement museumsLink = self.find("[data-testid='museum-page-link']");
  private final SelenideElement loginButton = self.find("[data-testid='login-button']");
  private final SelenideElement userAvatar = self.find("[data-testid='profile-button']");

  @Step("Verify the header is visible")
  public void shouldBeVisible() {
    self.shouldBe(visible);
    paintingsLink.shouldBe(visible);
    artistsLink.shouldBe(visible);
    museumsLink.shouldBe(visible);
  }

  public PaintingsPage goToPaintings() {
    paintingsLink.shouldBe(visible).click();
    return new PaintingsPage();
  }

  public ArtistsPage goToArtists() {
    artistsLink.shouldBe(visible).click();
    return new ArtistsPage();
  }

  public MuseumsPage goToMuseums() {
    museumsLink.shouldBe(visible).click();
    return new MuseumsPage();
  }

  public void isLoggedIn() {
    loginButton.shouldBe(not(visible));
    userAvatar.shouldBe(visible);
  }

  public void isNotLoggedIn() {
    loginButton.shouldBe(visible);
    userAvatar.shouldBe(not(visible));
  }

  @Step("Open profile modal")
  public void openProfileModal() {
    userAvatar.shouldBe(visible).click();
  }

  @Step("Click to login button")
  public LoginPage clickLoginButton() {
    loginButton.shouldBe(visible).shouldBe(enabled).click();
    return new LoginPage();
  }
}
