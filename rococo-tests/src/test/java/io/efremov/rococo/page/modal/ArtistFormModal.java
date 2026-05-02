package io.efremov.rococo.page.modal;

import static com.codeborne.selenide.Condition.visible;

import com.codeborne.selenide.SelenideElement;
import io.efremov.rococo.grpc.CreateArtistRequest;
import io.efremov.rococo.provider.ArtistProvider;
import io.qameta.allure.Step;
import java.io.File;
import lombok.Getter;

public class ArtistFormModal extends BaseModal {

  private final SelenideElement nameInput = self.find("input[name='name']");
  private final SelenideElement biographyTextarea = self.find("textarea[name='biography']");
  private final SelenideElement photoInput = self.find("input[name='photo']");
  @Getter
  private CreateArtistRequest newArtistInfo;

  @Step("Fill all fields")
  public ArtistFormModal fillAllFields() {
    self.shouldBe(visible);
    newArtistInfo = ArtistProvider.getCreateArtistRequest();
    setName(newArtistInfo.getName())
        .setBiography(newArtistInfo.getBiography())
        .uploadPhoto(newArtistInfo.getPhoto());
    return this;
  }

  @Step("Upload artist photo")
  private ArtistFormModal uploadPhoto(String imageFile) {
    File file = createTempFile(imageFile);
    photoInput.shouldBe(visible).uploadFile(file);
    return this;
  }

  @Step("Set artist name: {name}")
  private ArtistFormModal setName(String name) {
    nameInput.shouldBe(visible).setValue(name);
    return this;
  }

  @Step("Set artist biography")
  private ArtistFormModal setBiography(String biography) {
    biographyTextarea.shouldBe(visible).setValue(biography);
    return this;
  }
}
