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
import io.qameta.allure.Step;

public class ArtistsPage extends BasePage {

  private static final String URL = FRONT_URL + "/artist";

  private final SelenideElement pageTitle = $("h1");
  private final SelenideElement searchInput = $("input[placeholder*='художников'], input[type='search']");
  private final SelenideElement addArtistButton = buttonByText("Добавить художника");

  private final ElementsCollection artistsList = $$(".artist-card, [data-testid='artist-item'], a[href*='/artist/']");
  private final SelenideElement emptyState = $(".empty-state, .empty");
  private final SelenideElement loader = $(".loader, .spinner, [class*='loading']");

  public HeaderComponent header = new HeaderComponent();
  public ListComponent<ArtistDetailPage> list = new ListComponent<>(artistsList, emptyState, loader);

  public static ArtistsPage open() {
    return Selenide.open(URL, ArtistsPage.class);
  }

  private SelenideElement buttonByText(String text) {
    return $$("button").findBy(exactText(text));
  }

  @Step("Verify artists page is loaded")
  public ArtistsPage assertPageLoaded() {
    pageTitle.shouldBe(visible);
    return this;
  }

  @Step("Search for: {query}")
  public ArtistsPage search(String query) {
    searchInput.shouldBe(visible).setValue(query);
    return this;
  }

  @Step("Open new artist form")
  public ArtistFormModal openNewArtistForm() {
    addArtistButton.shouldBe(visible).click();
    return new ArtistFormModal();
  }

  @Step("Open artist by name: {name}")
  public ArtistDetailPage openArtistByName(String name) {
    artistsList.findBy(text(name)).shouldBe(visible).click();
    return new ArtistDetailPage();
  }

  @Step("Verify add artist button is visible")
  public void assertAddButtonVisible() {
    addArtistButton.shouldBe(visible);
  }

  @Step("Verify add artist button is not visible")
  public void assertAddButtonNotVisible() {
    addArtistButton.shouldNotBe(visible);
  }

  @Step("Verify page title equals: {expected}")
  public void assertTitleEquals(String expected) {
    pageTitle.shouldHave(text(expected));
  }
}
