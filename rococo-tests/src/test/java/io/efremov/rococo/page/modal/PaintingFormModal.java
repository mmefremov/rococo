package io.efremov.rococo.page.modal;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import java.io.File;

public class PaintingFormModal extends BaseModal {

  private final SelenideElement titleInput = $("input[placeholder*='Название'], input[name='title'], input#title");
  private final SelenideElement descriptionTextarea = $("textarea[name='description'], textarea#description");
  private final SelenideElement imageUpload = $("input[type='file'].image-upload, input[name='image']");
  private final SelenideElement artistSelect = $("select[name='artistId'], select#artist, .artist-select");
  private final SelenideElement submitButton;

  public PaintingFormModal() {
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

  @Step("Set painting title: {title}")
  public PaintingFormModal setTitle(String title) {
    titleInput.shouldBe(visible).setValue(title);
    return this;
  }

  @Step("Set painting description: {description}")
  public PaintingFormModal setDescription(String description) {
    descriptionTextarea.shouldBe(visible).setValue(description);
    return this;
  }

  @Step("Upload painting image: {filePath}")
  public PaintingFormModal uploadImage(String filePath) {
    imageUpload.shouldBe(visible).uploadFile(new File(filePath));
    return this;
  }

  @Step("Select artist: {artistName}")
  public PaintingFormModal selectArtist(String artistName) {
    artistSelect.shouldBe(visible).selectOption(artistName);
    return this;
  }

  @Step("Select artist by index: {index}")
  public PaintingFormModal selectArtistByIndex(int index) {
    artistSelect.shouldBe(visible).getSelectedOption();
    artistSelect.selectOption(index);
    return this;
  }

  @Override
  @Step("Click submit in painting form")
  public void submit() {
    submitButton.shouldBe(visible).click();
  }

  @Step("Get painting title")
  public String getTitle() {
    return titleInput.shouldBe(visible).getValue();
  }

  @Step("Get painting description")
  public String getDescription() {
    return descriptionTextarea.shouldBe(visible).getValue();
  }

  @Step("Verify painting modal is visible")
  public PaintingFormModal assertModalVisible() {
    modalDialog.shouldBe(visible);
    return this;
  }
}
