package io.efremov.rococo.page;

import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.efremov.rococo.model.PaintingInfoResponse;
import io.efremov.rococo.page.modal.PaintingFormModal;
import io.qameta.allure.Step;
import java.util.UUID;

public class PaintingDetailPage extends BasePage<PaintingDetailPage> {

  private static final String URL = FRONT_URL + "painting/";

  private final SelenideElement title = self.find("[data-testid='painting-title']");
  private final SelenideElement content = self.$("[data-testid='painting-content']");
  private final SelenideElement author = self.find("[data-testid='painting-artist']");
  private final SelenideElement description = self.find("[data-testid='painting-description']");
  private final SelenideElement editButton = self.find("[data-testid='edit-painting']");

  private final PaintingFormModal formModal = new PaintingFormModal();

  @Step("Open a painting detail page")
  public static PaintingDetailPage open(UUID id) {
    return Selenide.open(URL + id, PaintingDetailPage.class);
  }

  @Override
  @Step("Verify a painting detail page is loaded")
  public PaintingDetailPage verifyPageLoaded() {
    title.shouldBe(visible);
    content.shouldBe(visible);
    author.shouldBe(visible);
    description.shouldBe(visible);
    return this;
  }

  @Step("Update painting")
  public PaintingDetailPage updatePainting() {
    editButton.shouldBe(visible).click();
    formModal.fillAllFields().submit();
    toast.verifyAppearedMessage("Обновлена картина: " + formModal.getNewPaintingInfo().title());
    return this;
  }

  @Step("Verify painting is updated")
  public PaintingDetailPage verifyPaintingIsUpdated() {
    var updatedPainting = formModal.getNewPaintingInfo();
    title.shouldHave(text(updatedPainting.title()));
    content.shouldHave(attribute("src", updatedPainting.content()));
    author.shouldHave(text(formModal.getAuthorName()));
    description.shouldHave(text(updatedPainting.description()));
    return this;
  }

  @Step("Verify painting info")
  public PaintingDetailPage verifyPaintingInfo(PaintingInfoResponse info) {
    title.shouldHave(text(info.title()));
    content.shouldHave(attribute("src", info.content()));
    author.shouldHave(text(info.artist().name()));
    description.shouldHave(text(info.description()));
    return this;
  }

  @Step("Verify editing is not available")
  public PaintingDetailPage verifyEditingIsNotAvailable() {
    editButton.shouldNotBe(visible);
    return this;
  }
}
