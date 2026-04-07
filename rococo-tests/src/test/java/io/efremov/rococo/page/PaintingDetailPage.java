package io.efremov.rococo.page;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.efremov.rococo.page.component.HeaderComponent;
import io.efremov.rococo.page.modal.PaintingFormModal;
import io.qameta.allure.Step;

public class PaintingDetailPage extends BasePage {

  private static final String URL = FRONT_URL + "/painting/";

  private final SelenideElement paintingImage = $("img.painting, .painting-image, img[src*='painting']");
  private final SelenideElement paintingTitle = $("h1, .painting-title");
  private final SelenideElement artistName = $("a[href*='/artist/'], .artist-name");
  private final SelenideElement paintingDescription = $(".description, .painting-description, p");
  private final SelenideElement editButton = buttonByText("Редактировать");

  private SelenideElement buttonByText(String text) {
    return $$("button").findBy(com.codeborne.selenide.Condition.exactText(text));
  }

  public HeaderComponent header = new HeaderComponent();

  public static PaintingDetailPage open(String id) {
    return Selenide.open(URL + id, PaintingDetailPage.class);
  }

  @Step("Verify painting detail page is loaded")
  public PaintingDetailPage assertPageLoaded() {
    paintingTitle.shouldBe(visible);
    return this;
  }

  @Step("Get painting title")
  public String getPaintingTitle() {
    return paintingTitle.shouldBe(visible).getText();
  }

  @Step("Get artist name")
  public String getArtistName() {
    return artistName.shouldBe(visible).getText();
  }

  @Step("Get painting description")
  public String getPaintingDescription() {
    return paintingDescription.shouldBe(visible).getText();
  }

  @Step("Verify painting image is visible")
  public void assertImageVisible() {
    paintingImage.shouldBe(visible);
  }

  @Step("Verify painting title equals: {expected}")
  public void assertTitleEquals(String expected) {
    paintingTitle.shouldHave(exactText(expected));
  }

  @Step("Verify artist name equals: {expected}")
  public void assertArtistEquals(String expected) {
    artistName.shouldHave(text(expected));
  }

  @Step("Verify description contains: {expected}")
  public void assertDescriptionContains(String expected) {
    paintingDescription.shouldHave(text(expected));
  }

  @Step("Open edit form for painting")
  public PaintingFormModal openEditForm() {
    editButton.shouldBe(visible).click();
    return new PaintingFormModal();
  }

  @Step("Verify edit button is visible")
  public void assertEditButtonVisible() {
    editButton.shouldBe(visible);
  }

  @Step("Verify edit button is not visible")
  public void assertEditButtonNotVisible() {
    editButton.shouldNotBe(visible);
  }

  @Step("Navigate to artist detail page")
  public ArtistDetailPage goToArtistPage() {
    artistName.shouldBe(visible).click();
    return new ArtistDetailPage();
  }
}
