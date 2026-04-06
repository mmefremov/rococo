package io.efremov.rococo.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NoWhitespaceValidator implements ConstraintValidator<NoWhitespace, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }
    return !value.contains(" ");
  }
}
