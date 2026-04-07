package io.efremov.rococo.page;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.efremov.rococo.page.component.HeaderComponent;
import io.efremov.rococo.page.modal.MuseumFormModal;
import io.qameta.allure.Step;

public class MuseumDetailPage extends BasePage {

  private static final String URL = FRONT_URL + "/museum/";

  private final SelenideElement museumPhoto = $("img.museum, .museum-photo, .museum-image");
  private final SelenideElement museumTitle = $("h1, .museum-title");
  private final SelenideElement museumLocation = $(".location, .museum-location, .museum-city");
  private final SelenideElement museumDescription = $(".description, .museum-description, p");
  private final SelenideElement editButton = buttonByText("Редактировать");

  private SelenideElement buttonByText(String text) {
    return $$("button").findBy(com.codeborne.selenide.Condition.exactText(text));
  }

  public HeaderComponent header = new HeaderComponent();

  public static MuseumDetailPage open(String id) {
    return Selenide.open(URL + id, MuseumDetailPage.class);
  }

  @Step("Verify museum detail page is loaded")
  public MuseumDetailPage assertPageLoaded() {
    museumTitle.shouldBe(visible);
    return this;
  }

  @Step("Get museum title")
  public String getMuseumTitle() {
    return museumTitle.shouldBe(visible).getText();
  }

  @Step("Get museum location")
  public String getMuseumLocation() {
    return museumLocation.shouldBe(visible).getText();
  }

  @Step("Get museum description")
  public String getMuseumDescription() {
    return museumDescription.shouldBe(visible).getText();
  }

  @Step("Verify museum photo is visible")
  public void assertPhotoVisible() {
    museumPhoto.shouldBe(visible);
  }

  @Step("Verify museum title equals: {expected}")
  public void assertTitleEquals(String expected) {
    museumTitle.shouldHave(exactText(expected));
  }

  @Step("Verify location contains: {expected}")
  public void assertLocationContains(String expected) {
    museumLocation.shouldHave(text(expected));
  }

  @Step("Verify description contains: {expected}")
  public void assertDescriptionContains(String expected) {
    museumDescription.shouldHave(text(expected));
  }

  @Step("Open edit form for museum")
  public MuseumFormModal openEditForm() {
    editButton.shouldBe(visible).click();
    return new MuseumFormModal();
  }

  @Step("Verify edit button is visible")
  public void assertEditButtonVisible() {
    editButton.shouldBe(visible);
  }

  @Step("Verify edit button is not visible")
  public void assertEditButtonNotVisible() {
    editButton.shouldNotBe(visible);
  }
}
