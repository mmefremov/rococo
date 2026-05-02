package io.efremov.rococo.page.component;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.value;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.ScrollIntoViewOptions;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.ex.ListSizeMismatch;
import java.util.Objects;

public class SelectorComponent {

  private final SelenideElement self;
  private final ElementsCollection options;

  public SelectorComponent(String cssSelector) {
    self = $(cssSelector);
    options = self.findAll("option");
  }

  public SelenideElement selectOption(String value) {
    self.shouldBe(visible);
    SelenideElement selectedOption;
    int checkedValues = 0;
    while (true) {
      var elementOptional = options.stream()
          .skip(checkedValues)
          .filter(element -> element.has(value(value)))
          .findFirst();
      if (elementOptional.isPresent()) {
        selectedOption = elementOptional.get();
        break;
      }
      checkedValues = options.size();
      options.last().scrollIntoView(ScrollIntoViewOptions.instant());
      try {
        options.shouldHave(sizeGreaterThan(checkedValues));
      } catch (ListSizeMismatch e) {
        throw new IllegalStateException("Couldn't find the option: " + value, e.getCause());
      }
    }
    self.selectOption(Objects.requireNonNull(selectedOption.getText()));
    return selectedOption;
  }
}
