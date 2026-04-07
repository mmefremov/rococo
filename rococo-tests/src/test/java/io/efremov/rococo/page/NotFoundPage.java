package io.efremov.rococo.page;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.efremov.rococo.page.component.HeaderComponent;
import io.qameta.allure.Step;

public class NotFoundPage extends BasePage {

  private static final String URL = FRONT_URL + "/not-found";

  private final SelenideElement heading = $("h1, .not-found-title, .error-title");
  private final SelenideElement message = $("p, .not-found-message, .error-message");
  private final SelenideElement homeLink = $("a[href='/']");

  public HeaderComponent header = new HeaderComponent();

  public static NotFoundPage open() {
    return Selenide.open(URL, NotFoundPage.class);
  }

  @Step("Verify not found page is loaded")
  public NotFoundPage assertPageLoaded() {
    heading.shouldBe(visible);
    return this;
  }

  @Step("Verify heading contains: {expected}")
  public void assertHeadingContains(String expected) {
    heading.shouldHave(text(expected));
  }

  @Step("Verify error message is visible")
  public void assertMessageVisible() {
    message.shouldBe(visible);
  }

  @Step("Verify home link is visible")
  public void assertHomeLinkVisible() {
    homeLink.shouldBe(visible);
  }

  @Step("Navigate to home page")
  public MainPage goToHome() {
    homeLink.shouldBe(visible).click();
    return new MainPage();
  }
}
