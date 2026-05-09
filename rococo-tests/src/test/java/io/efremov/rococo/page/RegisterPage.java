package io.efremov.rococo.page;

import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static io.efremov.rococo.config.Constants.DEFAULT_PASSWORD;
import static io.efremov.rococo.util.RandomDataUtils.randomUsername;

import com.codeborne.selenide.SelenideElement;
import io.efremov.rococo.model.UserInfoResponse;
import io.efremov.rococo.util.RandomDataUtils;
import io.qameta.allure.Step;

public class RegisterPage {

  protected final SelenideElement self = $(".content");
  private final SelenideElement usernameInput = self.find("input[name='username']");
  private final SelenideElement passwordInput = self.find("input[name='password']");
  private final SelenideElement passwordSubmitInput = self.find("input[name='passwordSubmit']");
  private final SelenideElement submitButton = self.find("button[type='submit']");
  private final SelenideElement enterButton = self.find(".form__submit");
  private final SelenideElement subheader = self.find(".form__subheader");
  private final SelenideElement usernameError = self.find(".form__error.error__username");
  private final SelenideElement passwordError = self.find(".form__error.error__password");

  @Step("Open a register page")
  public static RegisterPage open() {
    return LoginPage.open()
        .clickRegisterButton();
  }

  @Step("Verify a page is loaded")
  public RegisterPage verifyPageLoaded() {
    usernameInput.shouldBe(visible);
    passwordInput.shouldBe(visible);
    passwordSubmitInput.shouldBe(visible);
    submitButton.shouldBe(visible);
    return this;
  }

  @Step("Fill register page: username: '{0}', password: '{1}'")
  public RegisterPage fillRegisterPage(String login, String password) {
    setUsername(login);
    setPassword(password);
    setSubmitPassword(password);
    return this;
  }

  @Step("Fill register page: username: '{0}', password: '{1}', passwordSubmit: '{2}'")
  public RegisterPage fillRegisterPage(String login, String password, String passwordSubmit) {
    setUsername(login);
    setPassword(password);
    setSubmitPassword(passwordSubmit);
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

  @Step("Check username required validation message")
  public RegisterPage checkUsernameRequiredMessage() {
    clearUsername();
    submit();
    usernameInput.shouldHave(attribute("validationMessage", "Заполните это поле."));
    return this;
  }

  @Step("Check password required validation message")
  public RegisterPage checkPasswordRequiredMessage() {
    fillRegisterPage(randomUsername(), "");
    submit();
    passwordInput.shouldHave(attribute("validationMessage", "Заполните это поле."));
    return this;
  }

  @Step("Check passwordSubmit required validation message")
  public RegisterPage checkPasswordSubmitRequiredMessage() {
    fillRegisterPage(randomUsername(), DEFAULT_PASSWORD, "");
    submit();
    passwordSubmitInput.shouldHave(attribute("validationMessage", "Заполните это поле."));
    return this;
  }

  @Step("Check min allowed lengths of fields errors")
  public RegisterPage checkMinAllowedLengthOfFieldsErrors() {
    fillRegisterPage("ab", "ab");
    submit();
    usernameError.shouldBe(visible).shouldHave(exactText("Allowed username length should be from 3 to 50 characters"));
    passwordError.shouldBe(visible).shouldHave(exactText("Allowed password length should be from 3 to 12 characters"));
    return this;
  }

  @Step("Check max allowed lengths of fields errors")
  public RegisterPage checkMaxAllowedLengthOfFieldsErrors() {
    String username = RandomDataUtils.GEN.string().length(51).get();
    String password = RandomDataUtils.GEN.string().length(13).get();
    fillRegisterPage(username, password);
    submit();
    usernameError.shouldBe(visible).shouldHave(exactText("Allowed username length should be from 3 to 50 characters"));
    passwordError.shouldBe(visible).shouldHave(exactText("Allowed password length should be from 3 to 12 characters"));
    return this;
  }

  @Step("Check credentials contain whitespace message")
  public RegisterPage checkCredentialsContainWhitespaceMessage() {
    fillRegisterPage("user name", "user pass");
    submit();
    usernameError.shouldHave(exactText("Username must not contain whitespaces"));
    passwordError.shouldHave(exactText("Password must not contain whitespaces"));
    return this;
  }

  @Step("Check passwords are different message")
  public RegisterPage checkPasswordsAreDifferentMessage() {
    fillRegisterPage(randomUsername(), "password1", "password2");
    submit();
    passwordError.shouldBe(visible).shouldHave(exactText("Passwords should be equal"));
    return this;
  }

  @Step("Check existing username message")
  public RegisterPage checkExistingUsernameMessage(UserInfoResponse existingUser) {
    fillRegisterPage(existingUser.username(), DEFAULT_PASSWORD);
    submit();
    usernameError.shouldBe(visible).shouldHave(exactText("Username `" + existingUser.username() + "` already exists"));
    return this;
  }

  @Step("Clear username")
  private RegisterPage clearUsername() {
    usernameInput.clear();
    return this;
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
}
