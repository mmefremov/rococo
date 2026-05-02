package io.efremov.rococo.page;

import static com.codeborne.selenide.CollectionCondition.itemWithText;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$$;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.efremov.rococo.page.modal.MuseumFormModal;
import io.qameta.allure.Step;

public class MuseumsPage extends BasePage<MuseumsPage> {

  private static final String URL = FRONT_URL + "museum";

  private final SelenideElement title = self.find("h2");
  private final SelenideElement searchInput = self.find("input[type='search']");
  private final SelenideElement addButton = self.find("[data-testid='add-museum-button']");

  private final ElementsCollection list = $$(".w-100 li");
  private final MuseumFormModal formModal = new MuseumFormModal();

  @Step("Open a museum page")
  public static MuseumsPage open() {
    return Selenide.open(URL, MuseumsPage.class);
  }

  @Override
  @Step("Verify museums page is loaded")
  public MuseumsPage verifyPageLoaded() {
    super.verifyPageLoaded();
    title.shouldBe(visible)
        .shouldHave(text("Музеи"));
    return this;
  }

  @Step("Verify museum is created")
  public MuseumsPage verifyMuseumIsCreated() {
    var createdMuseum = formModal.getNewMuseumInfo();
    searchInput.setValue(createdMuseum.title()).pressEnter();
    String expectedText = "%s\n%s, %s".formatted(
        createdMuseum.title(), createdMuseum.geo().city(), formModal.getCountryName());
    list.shouldHave(itemWithText(expectedText));
    return this;
  }

  @Step("Create new museum")
  public MuseumsPage createNewMuseum() {
    addButton.shouldBe(visible).click();
    formModal.fillAllFields().submit();
    toast.verifyAppearedMessage("Добавлен музей: " + formModal.getNewMuseumInfo().title());
    return this;
  }

  @Step("Search and open museum by title: {title}")
  public MuseumDetailPage openMuseumByName(String title) {
    searchInput.shouldBe(visible).setValue(title).pressEnter();
    list.findBy(text(title)).shouldBe(visible).click();
    return new MuseumDetailPage();
  }

  @Step("Verify museums page editing is not available")
  public MuseumsPage verifyEditingIsNotAvailable() {
    addButton.shouldNotBe(visible);
    return this;
  }
}
