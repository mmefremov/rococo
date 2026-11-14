package io.efremov.rococo.common.validation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class FieldValidatorTest {

  @ParameterizedTest
  @ValueSource(strings = {"jpeg", "png", "webp"})
  void requireImageBase64_supportedPrefix_acceptsBase64(String type) {
    assertThatCode(() -> FieldValidator.requireImageBase64("data:image/" + type + ";base64,AQID", "photo"))
        .as("supported image encoding").doesNotThrowAnyException();
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "photo", "data:image/gif;base64,AQID", "data:image/png;base64,%%%",
      "data:image/png;base64,"})
  void requireImageBase64_invalidPayload_rejects(String value) {
    assertThatThrownBy(() -> FieldValidator.requireImageBase64(value, "photo"))
        .as("invalid image payload").isInstanceOf(IllegalArgumentException.class)
        .hasMessageStartingWith("photo:");
  }

  @Test
  void requireImageBase64_oversizedPayload_rejectsBeforeDecoding() {
    assertThatThrownBy(() -> FieldValidator.requireImageBase64(
        "data:image/png;base64," + "A".repeat(1_048_576), "content"))
        .as("image size limit").hasMessage("content: must not exceed 1 MB");
  }
}
