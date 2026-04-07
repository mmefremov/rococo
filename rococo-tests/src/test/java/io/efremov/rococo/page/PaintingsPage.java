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
import io.efremov.rococo.page.modal.PaintingFormModal;
import io.qameta.allure.Step;

public class PaintingsPage extends BasePage {

  private static final String URL = FRONT_URL + "/painting";

  private final SelenideElement pageTitle = $("h1");
  private final SelenideElement searchInput = $("input[placeholder*='картины'], input[type='search']");
  private final SelenideElement addPaintingButton = buttonByText("Добавить картину");

  private final ElementsCollection paintingsList = $$(
      ".painting-card, [data-testid='painting-item'], a[href*='/painting/']");
  private final SelenideElement emptyState = $(".empty-state, .empty");
  private final SelenideElement loader = $(".loader, .spinner, [class*='loading']");

  public HeaderComponent header = new HeaderComponent();
  public ListComponent<PaintingDetailPage> list = new ListComponent<>(paintingsList, emptyState, loader);

  public static PaintingsPage open() {
    return Selenide.open(URL, PaintingsPage.class);
  }

  private SelenideElement buttonByText(String text) {
    return $$("button").findBy(com.codeborne.selenide.Condition.exactText(text));
  }

  @Step("Verify paintings page is loaded")
  public PaintingsPage assertPageLoaded() {
    pageTitle.shouldBe(visible);
    return this;
  }

  @Step("Search for: {query}")
  public PaintingsPage search(String query) {
    searchInput.shouldBe(visible).setValue(query);
    return this;
  }

  @Step("Open new painting form")
  public PaintingFormModal openNewPaintingForm() {
    addPaintingButton.shouldBe(visible).click();
    return new PaintingFormModal();
  }

  @Step("Open painting by name: {name}")
  public PaintingDetailPage openPaintingByName(String name) {
    paintingsList.findBy(text(name)).shouldBe(visible).click();
    return new PaintingDetailPage();
  }

  @Step("Verify add painting button is visible")
  public void assertAddButtonVisible() {
    addPaintingButton.shouldBe(visible);
  }

  @Step("Verify add painting button is not visible")
  public void assertAddButtonNotVisible() {
    addPaintingButton.shouldNotBe(visible);
  }

  @Step("Verify page title equals: {expected}")
  public void assertTitleEquals(String expected) {
    pageTitle.shouldHave(text(expected));
  }
}
