package io.efremov.rococo.page.modal;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import java.io.File;

public class UserModal extends BaseModal {

  private final SelenideElement avatar = $("img.avatar, .user-avatar img");
  private final SelenideElement usernameDisplay = $(".username, .user-username, [class*='username']");
  private final SelenideElement firstNameInput = $(
      "input[placeholder*='Имя'], input[name='firstName'], input#firstName");
  private final SelenideElement lastNameInput = $(
      "input[placeholder*='Фамилия'], input[name='lastName'], input#lastName");
  private final SelenideElement updateProfileButton = buttonByText("Обновить профиль");
  private final SelenideElement logoutButton = buttonByText("Выйти");
  private final SelenideElement imageUploadInput = $("input[type='file'].avatar-upload, input[name='avatar']");

  public UserModal() {
    super($(".modal, [role='dialog'], .form-wrapper"));
  }

  private SelenideElement buttonByText(String text) {
    return $$("button").findBy(com.codeborne.selenide.Condition.exactText(text));
  }

  @Step("Set first name: {firstName}")
  public UserModal setFirstName(String firstName) {
    firstNameInput.shouldBe(visible).setValue(firstName);
    return this;
  }

  @Step("Set last name: {lastName}")
  public UserModal setLastName(String lastName) {
    lastNameInput.shouldBe(visible).setValue(lastName);
    return this;
  }

  @Step("Upload avatar: {filePath}")
  public UserModal uploadAvatar(String filePath) {
    imageUploadInput.shouldBe(visible).uploadFile(new File(filePath));
    return this;
  }

  @Override
  @Step("Click update profile button")
  public void submit() {
    updateProfileButton.shouldBe(visible).click();
  }

  @Step("Click logout button")
  public void logout() {
    logoutButton.shouldBe(visible).click();
  }

  @Step("Get displayed username")
  public String getDisplayedUsername() {
    return usernameDisplay.shouldBe(visible).getText();
  }

  @Step("Get first name")
  public String getFirstName() {
    return firstNameInput.shouldBe(visible).getValue();
  }

  @Step("Get last name")
  public String getLastName() {
    return lastNameInput.shouldBe(visible).getValue();
  }

  @Step("Verify avatar is visible")
  public void assertAvatarVisible() {
    avatar.shouldBe(visible);
  }

  @Step("Verify logout button is visible")
  public void assertLogoutButtonVisible() {
    logoutButton.shouldBe(visible);
  }
}
