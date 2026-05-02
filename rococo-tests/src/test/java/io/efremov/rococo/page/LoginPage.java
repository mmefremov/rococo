package io.efremov.rococo.page;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.efremov.rococo.config.Config;
import io.qameta.allure.Step;

public class LoginPage {

  private static final String URL = Config.getInstance().authUrl() + "/login";

  protected final SelenideElement self = $(".content");
  private final SelenideElement usernameInput = self.find("input[name='username']");
  private final SelenideElement passwordInput = self.find("input[name='password']");
  private final SelenideElement submitButton = self.find("button[type='submit']");
  private final SelenideElement registerButton = self.find("[href='/register']");

  @Step("Open a login page")
  public static LoginPage open() {
    return Selenide.open(URL, LoginPage.class);
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
