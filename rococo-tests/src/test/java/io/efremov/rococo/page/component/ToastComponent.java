package io.efremov.rococo.page.component;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$$;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

public class ToastComponent {

  @Step("Wait for success toast")
  public static SelenideElement waitSuccessToast() {
    return $$(".toast, [role='alert'], .notification")
        .filterBy(text("успешно"))
        .first()
        .shouldBe(visible);
  }

  @Step("Wait for error toast")
  public static SelenideElement waitErrorToast() {
    return $$(".toast, [role='alert'], .notification")
        .filterBy(text("ошибка"))
        .first()
        .shouldBe(visible);
  }

  @Step("Verify any toast is visible")
  public static void assertAnyToastVisible() {
    $$(".toast, [role='alert'], .notification")
        .shouldHave(sizeGreaterThan(0));
  }

  @Step("Verify no toasts are visible")
  public static void assertNoToastsVisible() {
    $$(".toast, [role='alert'], .notification").shouldHave(size(0));
  }
}
