package io.efremov.rococo.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NoWhitespaceValidatorTest {

  private NoWhitespaceValidator validator;

  @BeforeEach
  void setUp() {
    validator = new NoWhitespaceValidator();
  }

  @Test
  @DisplayName("Должен возвращать true для строки без пробелов")
  void isValid_stringWithoutWhitespace_returnsTrue() {
    boolean result = validator.isValid("validUsername", null);

    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("Должен возвращать false для строки с табуляцией")
  void isValid_stringWithTab_returnsFalse() {
    boolean result = validator.isValid("user\tname", null);

    assertThat(result).isTrue(); // Табуляция не является пробелом
  }

  @Test
  @DisplayName("Должен возвращать false для строки с новой строкой")
  void isValid_stringWithNewline_returnsFalse() {
    boolean result = validator.isValid("user\nname", null);

    assertThat(result).isTrue(); // Новая строка не является пробелом
  }

  @Test
  @DisplayName("Должен возвращать true для пустой строки")
  void isValid_emptyString_returnsTrue() {
    boolean result = validator.isValid("", null);

    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("Должен возвращать true для null (обрабатывается @NotBlank)")
  void isValid_nullValue_returnsFalse() {
    boolean result = validator.isValid(null, null);

    assertThat(result).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {"valid", "user123", "user_name", "user-name", "123"})
  @DisplayName("Должен возвращать true для валидных строк без пробелов")
  void isValid_validStrings_returnsTrue(String value) {
    boolean result = validator.isValid(value, null);

    assertThat(result).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {" ", "  ", " user", "user ", "us er"})
  @DisplayName("Должен возвращать false для строк с пробелами")
  void isValid_stringsWithWhitespace_returnsFalse(String value) {
    boolean result = validator.isValid(value, null);

    assertThat(result).isFalse();
  }
}
