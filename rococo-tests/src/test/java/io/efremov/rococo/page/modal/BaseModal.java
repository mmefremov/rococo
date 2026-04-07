package io.efremov.rococo.page.modal;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

public abstract class BaseModal {

  protected final SelenideElement modalDialog;

  protected BaseModal(SelenideElement modalDialog) {
    this.modalDialog = modalDialog;
  }

  @Step("Wait for modal to be visible")
  public BaseModal waitForVisible() {
    modalDialog.shouldBe(visible);
    return this;
  }

  @Step("Click cancel button")
  public void cancel() {
    getSubmitButtonByText("Отмена").click();
  }

  public abstract void submit();

  @Step("Close modal by Escape")
  public void closeByEscape() {
    $("body").sendKeys("{Escape}");
  }

  protected SelenideElement getSubmitButtonByText(String text) {
    return $$("button").findBy(com.codeborne.selenide.Condition.exactText(text));
  }
}
