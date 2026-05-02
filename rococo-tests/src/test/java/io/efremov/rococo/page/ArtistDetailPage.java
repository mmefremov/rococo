package io.efremov.rococo.page;

import static com.codeborne.selenide.CollectionCondition.itemWithText;
import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.efremov.rococo.model.ArtistInfoResponse;
import io.efremov.rococo.page.modal.ArtistFormModal;
import io.efremov.rococo.page.modal.PaintingFormModal;
import io.qameta.allure.Step;
import java.util.UUID;

public class ArtistDetailPage extends BasePage<ArtistDetailPage> {

  private static final String URL = FRONT_URL + "artist/";

  private final SelenideElement name = self.find("[data-testid='artist-name']");
  private final SelenideElement avatar = self.find("[data-testid='avatar']");
  private final SelenideElement photo = avatar.$("img");
  private final SelenideElement biography = self.find("[data-testid='artist-biography']");
  private final SelenideElement editButton = self.find("[data-testid='edit-artist']");
  private final SelenideElement addPaintingButton = self.find("[data-testid='add-painting-button']");
  private final ElementsCollection paintingCards = self.findAll(".w-100 div");

  private final PaintingFormModal paintingFormModal = new PaintingFormModal();
  private final ArtistFormModal artistFormModal = new ArtistFormModal();

  @Step("Open an artist detail page")
  public static ArtistDetailPage open(UUID id) {
    return Selenide.open(URL + id, ArtistDetailPage.class);
  }

  @Override
  @Step("Verify an artist detail page is loaded")
  public ArtistDetailPage verifyPageLoaded() {
    super.verifyPageLoaded();
    name.shouldBe(visible);
    avatar.shouldBe(visible);
    biography.shouldBe(visible);
    return this;
  }

  @Step("Open new painting form from artist page")
  public PaintingFormModal openNewPaintingForm() {
    addPaintingButton.shouldBe(visible).click();
    return new PaintingFormModal();
  }

  @Step("Update artist")
  public ArtistDetailPage updateArtist() {
    editButton.shouldBe(visible).click();
    artistFormModal.fillAllFields().submit();
    toast.verifyAppearedMessage("Обновлен художник: " + artistFormModal.getNewArtistInfo().getName());
    return this;
  }

  @Step("Verify artist is updated")
  public ArtistDetailPage verifyArtistIsUpdated() {
    var updatedArtist = artistFormModal.getNewArtistInfo();
    name.shouldHave(text(updatedArtist.getName()));
    biography.shouldHave(text(updatedArtist.getBiography()));
    photo.shouldHave(attribute("src", updatedArtist.getPhoto()));
    return this;
  }

  @Step("Verify artist info")
  public ArtistDetailPage verifyArtistInfo(ArtistInfoResponse info) {
    name.shouldHave(text(info.name()));
    biography.shouldHave(text(info.biography()));
    photo.shouldHave(attribute("src", info.photo()));
    return this;
  }

  @Step("Create new painting")
  public ArtistDetailPage createNewPainting() {
    addPaintingButton.shouldBe(visible).click();
    paintingFormModal.fillAllFieldsExceptAuthor().submit();
    toast.verifyAppearedMessage("Добавлена картина: " + paintingFormModal.getNewPaintingInfo().title());
    return this;
  }

  @Step("Verify painting is created")
  public ArtistDetailPage verifyPaintingIsCreated() {
    var createdPainting = paintingFormModal.getNewPaintingInfo();
    paintingCards.shouldHave(itemWithText(createdPainting.title()));
    return this;
  }

  @Step("Verify editing is not available")
  public ArtistDetailPage verifyEditingIsNotAvailable() {
    editButton.shouldNotBe(visible);
    addPaintingButton.shouldNotBe(visible);
    return this;
  }

  @Step("Open painting by title: {title}")
  public PaintingDetailPage openPaintingByTitle(String title) {
    paintingCards.findBy(text(title)).shouldBe(visible).click();
    return new PaintingDetailPage();
  }
}
