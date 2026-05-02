package io.efremov.rococo.page;

import static com.codeborne.selenide.Condition.visible;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

public class MainPage extends BasePage<MainPage> {

  private static final String URL = FRONT_URL;
  private final SelenideElement paintingsCard = self.find("[data-testid='painting-page-link']");
  private final SelenideElement artistsCard = self.find("[data-testid='artist-page-link']");
  private final SelenideElement museumsCard = self.find("[data-testid='museum-page-link']");

  @Step("Open a main page")
  public static MainPage open() {
    return Selenide.open(URL, MainPage.class)
        .verifyPageLoaded();
  }

  @Override
  @Step("Verify a main page is loaded")
  public MainPage verifyPageLoaded() {
    super.verifyPageLoaded();
    paintingsCard.shouldBe(visible);
    artistsCard.shouldBe(visible);
    museumsCard.shouldBe(visible);
    return this;
  }

  @Step("Navigate to paintings page via card")
  public PaintingsPage goToPaintingsCard() {
    paintingsCard.shouldBe(visible).click();
    return new PaintingsPage();
  }

  @Step("Navigate to artists page via card")
  public ArtistsPage goToArtistsCard() {
    artistsCard.shouldBe(visible).click();
    return new ArtistsPage();
  }

  @Step("Navigate to museums page via card")
  public MuseumsPage goToMuseumsCard() {
    museumsCard.shouldBe(visible).click();
    return new MuseumsPage();
  }
}
