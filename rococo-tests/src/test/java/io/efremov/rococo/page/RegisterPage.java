package io.efremov.rococo.page;

import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.efremov.rococo.config.Config;
import io.qameta.allure.Step;

public class RegisterPage {

  private static final String URL = Config.getInstance().authUrl() + "register";
  protected final SelenideElement self = $(".content");
  private final SelenideElement usernameInput = self.find("input[name='username']");
  private final SelenideElement passwordInput = self.find("input[name='password']");
  private final SelenideElement passwordSubmitInput = self.find("input[name='passwordSubmit']");
  private final SelenideElement submitButton = self.find("button[type='submit']");
  private final SelenideElement enterButton = self.find(".form__submit");
  private final SelenideElement subheader = self.find(".form__subheader");
  private final SelenideElement errorContainer = self.find(".form__error");

  @Step("Open a register page")
  public static RegisterPage open() {
    return Selenide.open(URL, RegisterPage.class);
  }

  @Step("Verify a page is loaded")
  public RegisterPage verifyPageLoaded() {
    usernameInput.shouldBe(visible);
    passwordInput.shouldBe(visible);
    passwordSubmitInput.shouldBe(visible);
    submitButton.shouldBe(visible);
    return this;
  }

  @Step("Fill login page with credentials: username: '{0}', password: {1}")
  public RegisterPage fillRegisterPage(String login, String password) {
    setUsername(login);
    setPassword(password);
    setSubmitPassword(password);
    return this;
  }

  @Step("Submit login")
  public RegisterPage submit() {
    submitButton.click();
    return this;
  }

  @Step("Verify successfully registration")
  public RegisterPage verifySuccessfullyRegistration() {
    subheader.shouldBe(visible, exactText("Добро пожаловать в Rococo"));
    enterButton.shouldBe(visible);
    return this;
  }

  @Step("Click on the Enter button")
  public MainPage clickEnterButton() {
    enterButton.shouldBe(enabled).click();
    return new MainPage();
  }

  @Step("Set username: '{0}'")
  private RegisterPage setUsername(String username) {
    usernameInput.setValue(username);
    return this;
  }

  @Step("Set password: '{0}'")
  private RegisterPage setPassword(String password) {
    passwordInput.setValue(password);
    return this;
  }

  @Step("Set submit password: '{0}'")
  private RegisterPage setSubmitPassword(String password) {
    passwordSubmitInput.setValue(password);
    return this;
  }

  private RegisterPage checkMessagePasswordSubmit() {
    passwordSubmitInput.shouldHave(attribute("validationMessage", "Заполните это поле."));
    return this;
  }


  private RegisterPage checkMessageUsername() {
    usernameInput.shouldHave(attribute("validationMessage", "Заполните это поле."));
    return this;
  }
}
