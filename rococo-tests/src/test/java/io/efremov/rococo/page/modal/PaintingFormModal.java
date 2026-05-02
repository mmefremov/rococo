package io.efremov.rococo.page.modal;

import static com.codeborne.selenide.Condition.visible;

import com.codeborne.selenide.SelenideElement;
import io.efremov.rococo.model.CreatePaintingInfoRequest;
import io.efremov.rococo.page.component.SelectorComponent;
import io.efremov.rococo.provider.PaintingProvider;
import io.qameta.allure.Step;
import java.io.File;
import lombok.Getter;

public class PaintingFormModal extends BaseModal {

  private final SelenideElement titleInput = self.find("input[name='title']");
  private final SelenideElement contentInput = self.find("input[name='content']");
  private final SelenideElement descriptionTextarea = self.find("textarea[name='description']");
  private final SelectorComponent authorSelector = new SelectorComponent("select[name='authorId']");
  private final SelectorComponent museumSelector = new SelectorComponent("select[name='museumId']");

  @Getter
  private CreatePaintingInfoRequest newPaintingInfo;
  @Getter
  private String authorName;

  @Step("Fill all fields")
  public PaintingFormModal fillAllFields() {
    self.shouldBe(visible);
    newPaintingInfo = PaintingProvider.getCreatePaintingInfoRequest();

    setTitle(newPaintingInfo.title())
        .uploadContent(newPaintingInfo.content())
        .selectAuthor(newPaintingInfo.artist().id().toString())
        .setDescription(newPaintingInfo.description())
        .selectMuseum(newPaintingInfo.museum().id().toString());
    return this;
  }

  @Step("Fill all fields except author")
  public PaintingFormModal fillAllFieldsExceptAuthor() {
    self.shouldBe(visible);
    newPaintingInfo = PaintingProvider.getCreatePaintingInfoRequest();
    setTitle(newPaintingInfo.title())
        .uploadContent(newPaintingInfo.content())
        .setDescription(newPaintingInfo.description())
        .selectMuseum(newPaintingInfo.museum().id().toString());
    return this;
  }

  @Step("Set museum title: {title}")
  private PaintingFormModal setTitle(String title) {
    titleInput.shouldBe(visible).setValue(title);
    return this;
  }

  @Step("Upload museum content")
  private PaintingFormModal uploadContent(String imageFile) {
    File file = createTempFile(imageFile);
    contentInput.shouldBe(visible).uploadFile(file);
    return this;
  }

  @Step("Set museum description: {description}")
  private PaintingFormModal setDescription(String description) {
    descriptionTextarea.shouldBe(visible).setValue(description);
    return this;
  }

  @Step("Select author id: {authorId}")
  private PaintingFormModal selectAuthor(String authorId) {
    SelenideElement author = authorSelector.selectOption(authorId);
    authorName = author.getText();
    return this;
  }

  @Step("Select museum id: {museumId}")
  private PaintingFormModal selectMuseum(String museumId) {
    museumSelector.selectOption(museumId);
    return this;
  }
}
