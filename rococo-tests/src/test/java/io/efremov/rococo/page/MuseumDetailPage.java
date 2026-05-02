package io.efremov.rococo.page;

import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.efremov.rococo.model.MuseumInfoResponse;
import io.efremov.rococo.page.modal.MuseumFormModal;
import io.qameta.allure.Step;
import java.util.UUID;

public class MuseumDetailPage extends BasePage<MuseumDetailPage> {

  private static final String URL = FRONT_URL + "museum/";

  private final SelenideElement title = self.find("[data-testid='museum-title']");
  private final SelenideElement photo = self.$("[data-testid='museum-photo']");
  private final SelenideElement description = self.find("[data-testid='museum-description']");
  private final SelenideElement location = self.find("[data-testid='museum-location']");
  private final SelenideElement editButton = self.find("[data-testid='edit-museum']");

  private final MuseumFormModal formModal = new MuseumFormModal();

  @Step("Open a museum detail page")
  public static MuseumDetailPage open(UUID id) {
    return Selenide.open(URL + id, MuseumDetailPage.class);
  }

  @Override
  @Step("Verify a museum detail page is loaded")
  public MuseumDetailPage verifyPageLoaded() {
    super.verifyPageLoaded();
    title.shouldBe(visible);
    photo.shouldBe(visible);
    description.shouldBe(visible);
    location.shouldBe(visible);
    return this;
  }

  @Step("Update museum")
  public MuseumDetailPage updateMuseum() {
    editButton.shouldBe(visible).click();
    formModal.fillAllFields()
        .submit();
    toast.verifyAppearedMessage("Обновлен музей: " + formModal.getNewMuseumInfo().title());
    return this;
  }

  @Step("Verify museum is updated")
  public MuseumDetailPage verifyMuseumIsUpdated() {
    var updatedMuseum = formModal.getNewMuseumInfo();
    title.shouldHave(text(updatedMuseum.title()));
    description.shouldHave(text(updatedMuseum.description()));
    photo.shouldHave(attribute("src", updatedMuseum.photo()));
    String expectedText = "%s, %s".formatted(formModal.getCountryName(), updatedMuseum.geo().city());
    location.shouldHave(text(expectedText));
    return this;
  }

  @Step("Verify museum info")
  public MuseumDetailPage verifyMuseumInfo(MuseumInfoResponse info) {
    title.shouldHave(text(info.title()));
    description.shouldHave(text(info.description()));
    photo.shouldHave(attribute("src", info.photo()));
    String country = info.geo().country().name();
    String city = info.geo().city();
    location.shouldHave(text("%s, %s".formatted(country, city)));
    return this;
  }

  @Step("Verify editing is not available")
  public MuseumDetailPage verifyEditingIsNotAvailable() {
    editButton.shouldNotBe(visible);
    return this;
  }
}
