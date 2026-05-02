package io.efremov.rococo.page.component;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

public class ToastComponent {

  private final SelenideElement self = $(".toast .text-base");

  @Step("Appeared toast has message '{message}'")
  public void verifyAppearedMessage(String message) {
    self.shouldBe(visible).shouldHave(exactText(message));
  }
}
