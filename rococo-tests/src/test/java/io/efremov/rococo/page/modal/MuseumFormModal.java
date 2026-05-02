package io.efremov.rococo.page.modal;

import static com.codeborne.selenide.Condition.visible;

import com.codeborne.selenide.SelenideElement;
import io.efremov.rococo.model.CreateMuseumInfoRequest;
import io.efremov.rococo.page.component.SelectorComponent;
import io.efremov.rococo.provider.MuseumProvider;
import io.qameta.allure.Step;
import java.io.File;
import lombok.Getter;

public class MuseumFormModal extends BaseModal {

  private final SelenideElement photoInput = self.find("input[name='photo']");
  private final SelenideElement titleInput = self.find("input[name='title']");
  private final SelenideElement cityInput = self.find("input[name='city']");
  private final SelenideElement descriptionTextarea = self.find("textarea[name='description']");
  private final SelectorComponent countrySelector = new SelectorComponent("select[name='countryId']");
  @Getter
  private CreateMuseumInfoRequest newMuseumInfo;
  @Getter
  private String countryName;

  @Step("Fill all fields")
  public MuseumFormModal fillAllFields() {
    self.shouldBe(visible);
    newMuseumInfo = MuseumProvider.getCreateMuseumInfoRequest();
    setTitle(newMuseumInfo.title())
        .selectCountry(newMuseumInfo.geo().country().id().toString())
        .setCity(newMuseumInfo.geo().city())
        .uploadPhoto(newMuseumInfo.photo())
        .setDescription(newMuseumInfo.description());
    return this;
  }

  @Step("Upload museum photo")
  private MuseumFormModal uploadPhoto(String imageFile) {
    File file = createTempFile(imageFile);
    photoInput.shouldBe(visible).uploadFile(file);
    return this;
  }

  @Step("Set museum title: {title}")
  private MuseumFormModal setTitle(String title) {
    titleInput.shouldBe(visible).setValue(title);
    return this;
  }

  @Step("Set museum description: {description}")
  private MuseumFormModal setDescription(String description) {
    descriptionTextarea.shouldBe(visible).setValue(description);
    return this;
  }

  @Step("Set city: {city}")
  private MuseumFormModal setCity(String city) {
    cityInput.shouldBe(visible).setValue(city);
    return this;
  }

  @Step("Select country id: {countryId}")
  private MuseumFormModal selectCountry(String countryId) {
    SelenideElement country = countrySelector.selectOption(countryId);
    countryName = country.getText();
    return this;
  }
}
