package io.efremov.rococo.page;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.efremov.rococo.page.component.HeaderComponent;
import io.efremov.rococo.page.component.ListComponent;
import io.efremov.rococo.page.modal.MuseumFormModal;
import io.qameta.allure.Step;

public class MuseumsPage extends BasePage {

  private static final String URL = FRONT_URL + "/museum";

  private final SelenideElement pageTitle = $("h1");
  private final SelenideElement searchInput = $("input[placeholder*='музей'], input[type='search']");
  private final SelenideElement addMuseumButton = buttonByText("Добавить музей");

  private final ElementsCollection museumsList = $$(".museum-card, [data-testid='museum-item'], a[href*='/museum/']");
  private final SelenideElement emptyState = $(".empty-state, .empty");
  private final SelenideElement loader = $(".loader, .spinner, [class*='loading']");

  public HeaderComponent header = new HeaderComponent();
  public ListComponent<MuseumDetailPage> list = new ListComponent<>(museumsList, emptyState, loader);

  public static MuseumsPage open() {
    return Selenide.open(URL, MuseumsPage.class);
  }

  private SelenideElement buttonByText(String text) {
    return $$("button").findBy(com.codeborne.selenide.Condition.exactText(text));
  }

  @Step("Verify museums page is loaded")
  public MuseumsPage assertPageLoaded() {
    pageTitle.shouldBe(visible);
    return this;
  }

  @Step("Search for: {query}")
  public MuseumsPage search(String query) {
    searchInput.shouldBe(visible).setValue(query);
    return this;
  }

  @Step("Open new museum form")
  public MuseumFormModal openNewMuseumForm() {
    addMuseumButton.shouldBe(visible).click();
    return new MuseumFormModal();
  }

  @Step("Open museum by name: {name}")
  public MuseumDetailPage openMuseumByName(String name) {
    museumsList.findBy(text(name)).shouldBe(visible).click();
    return new MuseumDetailPage();
  }

  @Step("Verify add museum button is visible")
  public void assertAddButtonVisible() {
    addMuseumButton.shouldBe(visible);
  }

  @Step("Verify add museum button is not visible")
  public void assertAddButtonNotVisible() {
    addMuseumButton.shouldNotBe(visible);
  }

  @Step("Verify page title equals: {expected}")
  public void assertTitleEquals(String expected) {
    pageTitle.shouldHave(text(expected));
  }
}
