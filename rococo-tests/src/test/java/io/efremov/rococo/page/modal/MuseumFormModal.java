package io.efremov.rococo.page.modal;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import java.io.File;

public class MuseumFormModal extends BaseModal {

  private final SelenideElement titleInput = $("input[placeholder*='Название'], input[name='title'], input#title");
  private final SelenideElement descriptionTextarea = $("textarea[name='description'], textarea#description");
  private final SelenideElement photoUpload = $("input[type='file'].image-upload, input[name='photo']");
  private final SelenideElement citySelect = $("select[name='cityId'], select#city, .city-select");
  private final SelenideElement countrySelect = $("select[name='countryId'], select#country, .country-select");
  private final SelenideElement submitButton;

  public MuseumFormModal() {
    super($(".modal, [role='dialog'], .form-wrapper"));
    this.submitButton = anyButtonByText("Сохранить", "Создать", "Обновить");
  }

  private SelenideElement anyButtonByText(String... texts) {
    SelenideElement result = null;
    for (String text : texts) {
      SelenideElement candidate = $$("button").findBy(com.codeborne.selenide.Condition.exactText(text));
      if (result == null) {
        result = candidate;
      }
    }
    return result;
  }

  @Step("Set museum title: {title}")
  public MuseumFormModal setTitle(String title) {
    titleInput.shouldBe(visible).setValue(title);
    return this;
  }

  @Step("Set museum description: {description}")
  public MuseumFormModal setDescription(String description) {
    descriptionTextarea.shouldBe(visible).setValue(description);
    return this;
  }

  @Step("Upload museum photo: {filePath}")
  public MuseumFormModal uploadPhoto(String filePath) {
    photoUpload.shouldBe(visible).uploadFile(new File(filePath));
    return this;
  }

  @Step("Select city: {cityName}")
  public MuseumFormModal selectCity(String cityName) {
    citySelect.shouldBe(visible).selectOption(cityName);
    return this;
  }

  @Step("Select country: {countryName}")
  public MuseumFormModal selectCountry(String countryName) {
    countrySelect.shouldBe(visible).selectOption(countryName);
    return this;
  }

  @Override
  @Step("Click submit in museum form")
  public void submit() {
    submitButton.shouldBe(visible).click();
  }

  @Step("Get museum title")
  public String getTitle() {
    return titleInput.shouldBe(visible).getValue();
  }

  @Step("Get museum description")
  public String getDescription() {
    return descriptionTextarea.shouldBe(visible).getValue();
  }

  @Step("Verify museum modal is visible")
  public MuseumFormModal assertModalVisible() {
    modalDialog.shouldBe(visible);
    return this;
  }
}
