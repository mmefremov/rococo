package io.efremov.rococo.page.modal;

import static com.codeborne.selenide.Condition.appear;
import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.empty;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.value;
import static com.codeborne.selenide.Condition.visible;

import com.codeborne.selenide.SelenideElement;
import io.efremov.rococo.model.UpdateUserInfoRequest;
import io.efremov.rococo.model.UserInfoResponse;
import io.efremov.rococo.service.GatewayApiClient;
import io.qameta.allure.Step;
import java.io.File;
import java.util.Objects;

public class UserModal extends BaseModal {

  private final SelenideElement avatar = self.find(".avatar img");
  private final SelenideElement username = self.find("h4");
  private final SelenideElement photoInput = self.find("input[name='content']");
  private final SelenideElement firstNameInput = self.find("input[name='firstname']");
  private final SelenideElement surnameInput = self.find("input[name='surname']");
  private final SelenideElement logoutButton = self.find("button.variant-ghost");
  private UpdateUserInfoRequest updatedInfo;

  @Step("Verify user info")
  public UserModal verifyUserInfo() {
    UserInfoResponse info = new GatewayApiClient().getUser().body();
    if (Objects.isNull(info.avatar())) {
      avatar.shouldNotBe(appear);
    } else {
      avatar.shouldHave(attribute("src", info.avatar()));
    }
    username.shouldHave(text(info.username()));
    photoInput.shouldBe(empty);
    if (Objects.isNull(info.firstname())) {
      firstNameInput.shouldBe(empty);
    } else {
      firstNameInput.shouldHave(value(info.firstname()));
    }
    if (Objects.isNull(info.lastname())) {
      surnameInput.shouldBe(empty);
    } else {
      surnameInput.shouldHave(value(info.lastname()));
    }
    return this;
  }

  @Step("Click logout button")
  public void logout() {
    logoutButton.shouldBe(visible).click();
  }

  @Step("Fill all fields")
  public UserModal fillAllFields(UpdateUserInfoRequest info) {
    updatedInfo = info;
    self.shouldBe(visible);
    setFirstName(updatedInfo.firstname())
        .setLastName(updatedInfo.lastname())
        .uploadPhoto(updatedInfo.avatar());
    return this;
  }

  @Step("Verify user info is updated")
  public void verifyUserInfoIsUpdated() {
    if (Objects.isNull(updatedInfo.avatar())) {
      avatar.shouldNotBe(appear);
    } else {
      avatar.shouldHave(attribute("src", updatedInfo.avatar()));
    }
    photoInput.shouldBe(empty);
    if (Objects.isNull(updatedInfo.firstname())) {
      firstNameInput.shouldBe(empty);
    } else {
      firstNameInput.shouldHave(value(updatedInfo.firstname()));
    }
    if (Objects.isNull(updatedInfo.lastname())) {
      surnameInput.shouldBe(empty);
    } else {
      surnameInput.shouldHave(value(updatedInfo.lastname()));
    }
  }

  @Step("Set first name: {firstName}")
  private UserModal setFirstName(String firstName) {
    firstNameInput.shouldBe(visible).setValue(firstName);
    return this;
  }

  @Step("Set last name: {lastName}")
  private UserModal setLastName(String lastName) {
    surnameInput.shouldBe(visible).setValue(lastName);
    return this;
  }

  @Step("Upload user photo")
  private void uploadPhoto(String imageFile) {
    if (imageFile != null) {
      File file = createTempFile(imageFile);
      photoInput.shouldBe(visible).uploadFile(file);
    }
  }
}
