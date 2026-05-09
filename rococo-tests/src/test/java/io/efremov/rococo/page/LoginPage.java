package io.efremov.rococo.page;

import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static io.efremov.rococo.util.RandomDataUtils.randomPassword;
import static io.efremov.rococo.util.RandomDataUtils.randomUsername;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

public class LoginPage {

  protected final SelenideElement self = $(".content");
  private final SelenideElement usernameInput = self.find("input[name='username']");
  private final SelenideElement passwordInput = self.find("input[name='password']");
  private final SelenideElement submitButton = self.find("button[type='submit']");
  private final SelenideElement registerButton = self.find("[href='/register']");
  private final SelenideElement errorMessage = self.find(".form__error");

  @Step("Open a login page")
  public static LoginPage open() {
    return MainPage.open()
        .clickLoginButton();
  }

  @Step("Verify a page is loaded")
  public LoginPage verifyPageLoaded() {
    usernameInput.shouldBe(visible);
    passwordInput.shouldBe(visible);
    submitButton.shouldBe(visible);
    registerButton.shouldBe(visible);
    return this;
  }

  @Step("Click 'Sign up' button")
  public RegisterPage clickRegisterButton() {
    registerButton.click();
    return new RegisterPage();
  }

  @Step("Fill login page with credentials: username: '{0}', password: {1}")
  public LoginPage fillLoginPage(String login, String password) {
    setUsername(login);
    setPassword(password);
    return this;
  }

  @Step("Submit login")
  public MainPage submit() {
    submitButton.click();
    return new MainPage();
  }

  @Step("Check bad credentials error")
  public LoginPage checkBadCredetialsError() {
    fillLoginPage(randomUsername(), randomPassword());
    submitButton.click();
    errorMessage.shouldBe(visible)
        .shouldHave(exactText("Неверные учетные данные пользователя"));
    return this;
  }

  @Step("Check username required validation message")
  public LoginPage checkUsernameRequiredMessage() {
    clearUsername();
    submitButton.click();
    usernameInput.shouldHave(attribute("validationMessage", "Заполните это поле."));
    return this;
  }

  @Step("Check password required validation message")
  public LoginPage checkPasswordRequiredMessage() {
    fillLoginPage(randomUsername(), "");
    submitButton.click();
    passwordInput.shouldHave(attribute("validationMessage", "Заполните это поле."));
    return this;
  }

  @Step("Clear username")
  private LoginPage clearUsername() {
    usernameInput.clear();
    return this;
  }

  @Step("Set username: '{0}'")
  private LoginPage setUsername(String username) {
    usernameInput.setValue(username);
    return this;
  }

  @Step("Set password: '{0}'")
  private LoginPage setPassword(String password) {
    passwordInput.setValue(password);
    return this;
  }
}
