package io.efremov.rococo.page.component;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.not;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

public class ListComponent<T> {

  private final ElementsCollection itemsCollection;
  private final SelenideElement emptyState;
  private final SelenideElement loader;

  public ListComponent(ElementsCollection itemsCollection,
      SelenideElement emptyState,
      SelenideElement loader) {
    this.itemsCollection = itemsCollection;
    this.emptyState = emptyState;
    this.loader = loader;
  }

  @Step("Get items count")
  public int getItemsCount() {
    return itemsCollection.size();
  }

  @Step("Verify list is not empty")
  public void assertNotEmpty() {
    itemsCollection.shouldHave(sizeGreaterThan(0));
  }

  @Step("Verify list is empty")
  public void assertEmpty() {
    emptyState.shouldBe(visible);
  }

  @Step("Verify loader is not visible")
  public void assertLoaderNotVisible() {
    loader.should(not(visible));
  }

  @Step("Click item at index: {index}")
  public void clickItem(int index) {
    itemsCollection.get(index).shouldBe(visible).click();
  }

  @Step("Click item by text: {text}")
  public void clickItemByText(String text) {
    itemsCollection.findBy(text(text)).shouldBe(visible).click();
  }

  @Step("Verify item exists: {text}")
  public void assertItemExists(String text) {
    itemsCollection.findBy(text(text)).shouldBe(visible);
  }

  public ElementsCollection getItems() {
    return itemsCollection;
  }
}
