package io.efremov.rococo.page;

import static com.codeborne.selenide.CollectionCondition.itemWithText;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$$;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.efremov.rococo.page.modal.PaintingFormModal;
import io.qameta.allure.Step;

public class PaintingsPage extends BasePage<PaintingsPage> {

  private static final String URL = FRONT_URL + "painting";

  private final SelenideElement title = self.find("h2");
  private final SelenideElement searchInput = self.find("input[type='search']");
  private final SelenideElement addButton = self.find("[data-testid='add-painting-button']");

  private final ElementsCollection list = $$(".w-100 li");
  private final PaintingFormModal formModal = new PaintingFormModal();

  @Step("Open a painting page")
  public static PaintingsPage open() {
    return Selenide.open(URL, PaintingsPage.class);
  }

  @Override
  @Step("Verify a paintings page is loaded")
  public PaintingsPage verifyPageLoaded() {
    title.shouldBe(visible)
        .shouldHave(text("Картины"));
    return this;
  }

  @Step("Verify painting is created")
  public PaintingsPage verifyPaintingIsCreated() {
    var createdPainting = formModal.getNewPaintingInfo();
    searchInput.setValue(createdPainting.title()).pressEnter();
    list.shouldHave(itemWithText(createdPainting.title()));
    return this;
  }

  @Step("Create new painting")
  public PaintingsPage createNewPainting() {
    addButton.shouldBe(visible).click();
    formModal.fillAllFields().submit();
    toast.verifyAppearedMessage("Добавлена картина: " + formModal.getNewPaintingInfo().title());
    return this;
  }

  @Step("Search and open painting by title: {title}")
  public PaintingDetailPage openPaintingByTitle(String title) {
    searchInput.shouldBe(visible).setValue(title).pressEnter();
    list.findBy(text(title)).shouldBe(visible).click();
    return new PaintingDetailPage();
  }

  @Step("Verify paintings page editing is not available")
  public PaintingsPage verifyEditingIsNotAvailable() {
    addButton.shouldNotBe(visible);
    return this;
  }
}
