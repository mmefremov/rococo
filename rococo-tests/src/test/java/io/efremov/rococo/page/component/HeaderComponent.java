package io.efremov.rococo.page.component;

import static com.codeborne.selenide.Condition.not;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.SelenideElement;
import io.efremov.rococo.page.ArtistsPage;
import io.efremov.rococo.page.MuseumsPage;
import io.efremov.rococo.page.PaintingsPage;
import io.efremov.rococo.page.modal.UserModal;
import io.qameta.allure.Step;

public class HeaderComponent {

  private final SelenideElement self = $("div[data-testid='app-bar']");
  private final SelenideElement paintingsLink = self.find("a[href='/painting']");
  private final SelenideElement artistsLink = self.find("a[href='/artist']");
  private final SelenideElement museumsLink = self.find("a[href='/museum']");
  private final SelenideElement loginButton = self.find(".app-bar-slot-trail .btn");
  private final SelenideElement userAvatar = self.find("img.avatar, .avatar");

  @Step("Verify the header is visible")
  public void shouldBeVisible() {
    self.shouldBe(visible);
  }

  @Step("Navigate to paintings page")
  public PaintingsPage goToPaintings() {
    paintingsLink.shouldBe(visible).click();
    return new PaintingsPage();
  }

  @Step("Navigate to artists page")
  public ArtistsPage goToArtists() {
    artistsLink.shouldBe(visible).click();
    return new ArtistsPage();
  }

  @Step("Navigate to museums page")
  public MuseumsPage goToMuseums() {
    museumsLink.shouldBe(visible).click();
    return new MuseumsPage();
  }

  @Step("Click login button")
  public void clickLogin() {
    loginButton.shouldBe(visible).click();
  }

  @Step("Check if user is logged in")
  public boolean isLoggedIn() {
    return loginButton.is(not(visible)) || userAvatar.is(visible);
  }

  @Step("Open profile modal")
  public UserModal openProfileModal() {
    userAvatar.shouldBe(visible).click();
    return new UserModal();
  }

  @Step("Verify login button is visible")
  public void assertLoginButtonVisible() {
    loginButton.shouldBe(visible);
  }
}
