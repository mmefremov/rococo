package io.efremov.rococo.page;

import static com.codeborne.selenide.CollectionCondition.itemWithText;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$$;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.efremov.rococo.page.modal.ArtistFormModal;
import io.qameta.allure.Step;

public class ArtistsPage extends BasePage<ArtistsPage> {

  private static final String URL = FRONT_URL + "artist";

  private final SelenideElement title = self.find("h2");
  private final SelenideElement searchInput = self.find("input[type='search']");
  private final SelenideElement addButton = self.find("[data-testid='add-artist-button']");

  private final ElementsCollection list = $$(".w-100 li");
  private final ArtistFormModal formModal = new ArtistFormModal();

  @Step("Open an artist page")
  public static ArtistsPage open() {
    return Selenide.open(URL, ArtistsPage.class);
  }

  @Override
  @Step("Verify an artist page is loaded")
  public ArtistsPage verifyPageLoaded() {
    super.verifyPageLoaded();
    title.shouldBe(visible)
        .shouldHave(text("Художники"));
    return this;
  }

  @Step("Verify artist is created")
  public ArtistsPage verifyArtistIsCreated() {
    var createdArtist = formModal.getNewArtistInfo();
    searchInput.setValue(createdArtist.getName()).pressEnter();
    list.shouldHave(itemWithText(createdArtist.getName()));
    return this;
  }

  @Step("Create new artist")
  public ArtistsPage createNewArtist() {
    addButton.shouldBe(visible).click();
    formModal.fillAllFields().submit();
    toast.verifyAppearedMessage("Добавлен художник: " + formModal.getNewArtistInfo().getName());
    return this;
  }

  @Step("Search and open artist by name: {name}")
  public ArtistDetailPage openArtistByName(String name) {
    searchInput.shouldBe(visible).setValue(name).pressEnter();
    list.findBy(text(name)).shouldBe(visible).click();
    return new ArtistDetailPage();
  }

  @Step("Verify artists page editing is not available")
  public ArtistsPage verifyEditingIsNotAvailable() {
    addButton.shouldNotBe(visible);
    return this;
  }
}
