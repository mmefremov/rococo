package io.efremov.rococo.page.modal;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import java.io.File;

public class ArtistFormModal extends BaseModal {

  private final SelenideElement nameInput = $("input[placeholder*='Имя'], input[name='name'], input#name");
  private final SelenideElement descriptionTextarea = $(
      "textarea[name='biography'], textarea[name='description'], textarea#biography");
  private final SelenideElement avatarUpload = $("input[type='file'].avatar-upload, input[name='avatar']");
  private final SelenideElement submitButton;

  public ArtistFormModal() {
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

  @Step("Set artist name: {name}")
  public ArtistFormModal setName(String name) {
    nameInput.shouldBe(visible).setValue(name);
    return this;
  }

  @Step("Set artist description: {description}")
  public ArtistFormModal setDescription(String description) {
    descriptionTextarea.shouldBe(visible).setValue(description);
    return this;
  }

  @Step("Upload avatar: {filePath}")
  public ArtistFormModal uploadAvatar(String filePath) {
    avatarUpload.shouldBe(visible).uploadFile(new File(filePath));
    return this;
  }

  @Override
  @Step("Click submit in artist form")
  public void submit() {
    submitButton.shouldBe(visible).click();
  }

  @Step("Get artist name")
  public String getName() {
    return nameInput.shouldBe(visible).getValue();
  }

  @Step("Get artist description")
  public String getDescription() {
    return descriptionTextarea.shouldBe(visible).getValue();
  }

  @Step("Verify artist modal is visible")
  public ArtistFormModal assertModalVisible() {
    modalDialog.shouldBe(visible);
    return this;
  }
}
