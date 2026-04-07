package io.efremov.rococo.page;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.efremov.rococo.page.component.HeaderComponent;
import io.qameta.allure.Step;

public class MainPage extends BasePage {

  private static final String URL = FRONT_URL + "/";

  private final HeaderComponent header = new HeaderComponent();
  private final SelenideElement paintingsCard = $("a[href='/painting'].card, a[href='/painting']");
  private final SelenideElement artistsCard = $("a[href='/artist'].card, a[href='/artist']");
  private final SelenideElement museumsCard = $("a[href='/museum'].card, a[href='/museum']");

  public static MainPage open() {
    return Selenide.open(URL, MainPage.class);
  }

  @Step("Verify main page is loaded")
  public MainPage verifyPageLoaded() {
    header.shouldBeVisible();
    verifyPaintingsCardVisible();
    verifyArtistsCardVisible();
    verifyMuseumsCardVisible();
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

  @Step("Verify paintings card is visible")
  private void verifyPaintingsCardVisible() {
    paintingsCard.shouldBe(visible);
  }

  @Step("Verify artists card is visible")
  private void verifyArtistsCardVisible() {
    artistsCard.shouldBe(visible);
  }

  @Step("Verify museums card is visible")
  private void verifyMuseumsCardVisible() {
    museumsCard.shouldBe(visible);
  }
}
