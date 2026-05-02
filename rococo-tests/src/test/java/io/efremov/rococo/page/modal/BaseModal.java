package io.efremov.rococo.page.modal;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.UUID;

public abstract class BaseModal {

  protected final SelenideElement self = $("form.modal-form");
  protected final SelenideElement closeButton = self.find("[data-testid='close-button']");
  protected final SelenideElement submitButton = self.find("[data-testid='submit-button']");

  @Step("Close modal form")
  public void close() {
    closeButton.shouldBe(visible).click();
  }

  @Step("Submit modal form")
  public void submit() {
    submitButton.shouldBe(visible).click();
  }

  protected File createTempFile(String imageFile) {
    try {
      String cleanBase64 = imageFile.replaceAll("^data:image/\\w+;base64,", "");
      byte[] bytes = Base64.getDecoder().decode(cleanBase64);
      Path tempFile = Files.createTempFile(UUID.randomUUID().toString(), ".png");
      Files.write(tempFile, bytes);
      File file = tempFile.toFile();
      file.deleteOnExit();
      return file;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
