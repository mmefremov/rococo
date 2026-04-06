package io.efremov.rococo.validation;

import static org.assertj.core.api.Assertions.assertThat;

import io.efremov.rococo.model.RegistrationForm;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EqualPasswordsValidatorTest {

  private static Validator validator;

  @BeforeAll
  static void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  @DisplayName("Должен возвращать true, если пароли совпадают")
  void isValid_matchingPasswords_returnsTrue() {
    RegistrationForm form = new RegistrationForm("user", "password123", "password123");

    Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);

    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("Должен возвращать false, если пароли не совпадают")
  void isValid_nonMatchingPasswords_returnsFalse() {
    RegistrationForm form = new RegistrationForm("user", "Password", "password");

    Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);

    assertThat(violations).isNotEmpty();
  }
}
