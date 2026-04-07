package io.efremov.rococo.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonUtils {

  @Getter
  private static final ObjectMapper mapper;


  static {
    mapper = new ObjectMapper()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .enable(SerializationFeature.INDENT_OUTPUT);
    mapper.registerModule(new JavaTimeModule());
  }


  @NonNull
  public static <T> T readValue(String content, Class<T> valueType) {
    try {
      return mapper.readValue(content, valueType);
    } catch (JsonProcessingException e) {
      log.error("Can't read the content: [{}]", content);
      throw new RuntimeException(e);
    }
  }

  @NonNull
  public static JsonNode readTree(String content) {
    try {
      return mapper.readTree(content);
    } catch (JsonProcessingException e) {
      log.error("Can't read the tree: [{}]", content);
      throw new RuntimeException(e);
    }
  }

  @NonNull
  public static String writeValueAsString(Object value) {
    try {
      return mapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      log.error("Can't write the value: [{}]", value);
      throw new RuntimeException(e);
    }
  }
}
