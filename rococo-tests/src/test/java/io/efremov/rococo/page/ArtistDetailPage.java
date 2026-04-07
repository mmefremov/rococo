package io.efremov.rococo.page;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.efremov.rococo.page.component.HeaderComponent;
import io.efremov.rococo.page.component.ListComponent;
import io.efremov.rococo.page.modal.ArtistFormModal;
import io.efremov.rococo.page.modal.PaintingFormModal;
import io.qameta.allure.Step;

public class ArtistDetailPage extends BasePage {

  private static final String URL = FRONT_URL + "/artist/";

  private final SelenideElement artistAvatar = $("img.avatar, .artist-avatar img, .artist-photo");
  private final SelenideElement artistName = $("h1, .artist-name");
  private final SelenideElement artistBiography = $(".biography, .artist-bio, p");
  private final SelenideElement editButton = buttonByText("Редактировать");
  private final SelenideElement addPaintingButton = buttonByText("Добавить картину");

  private final ElementsCollection paintingsList = $$(
      ".painting-card, [data-testid='painting-item'], a[href*='/painting/']");
  private final SelenideElement paintingsEmptyState = $(".empty-state, .empty");
  private final SelenideElement paintingsLoader = $(".loader, .spinner, [class*='loading']");

  public HeaderComponent header = new HeaderComponent();
  public ListComponent<PaintingDetailPage> paintingsListComponent =
      new ListComponent<>(paintingsList, paintingsEmptyState, paintingsLoader);

  public static ArtistDetailPage open(String id) {
    return Selenide.open(URL + id, ArtistDetailPage.class);
  }

  private SelenideElement buttonByText(String text) {
    return $$("button").findBy(com.codeborne.selenide.Condition.exactText(text));
  }

  @Step("Verify artist detail page is loaded")
  public ArtistDetailPage assertPageLoaded() {
    artistName.shouldBe(visible);
    return this;
  }

  @Step("Get artist name")
  public String getArtistName() {
    return artistName.shouldBe(visible).getText();
  }

  @Step("Get artist biography")
  public String getArtistBiography() {
    return artistBiography.shouldBe(visible).getText();
  }

  @Step("Verify artist avatar is visible")
  public void assertAvatarVisible() {
    artistAvatar.shouldBe(visible);
  }

  @Step("Verify artist name equals: {expected}")
  public void assertNameEquals(String expected) {
    artistName.shouldHave(exactText(expected));
  }

  @Step("Verify biography contains: {expected}")
  public void assertBiographyContains(String expected) {
    artistBiography.shouldHave(text(expected));
  }

  @Step("Open edit form for artist")
  public ArtistFormModal openEditForm() {
    editButton.shouldBe(visible).click();
    return new ArtistFormModal();
  }

  @Step("Open new painting form from artist page")
  public PaintingFormModal openNewPaintingForm() {
    addPaintingButton.shouldBe(visible).click();
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

  @Step("Verify add painting button is visible")
  public void assertAddPaintingButtonVisible() {
    addPaintingButton.shouldBe(visible);
  }

  @Step("Verify add painting button is not visible")
  public void assertAddPaintingButtonNotVisible() {
    addPaintingButton.shouldNotBe(visible);
  }
}
