package io.efremov.rococo.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonUtils {

  public static final int MAX_FIELD_LENGTH = 1_000;
  public static final int TRUNCATED_PREFIX_LENGTH = 15;
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

  @NonNull
  public static String writeValueAsStringWithLongFieldsTruncating(@NonNull String json) {
    JsonNode node = readTree(json);
    truncateNode(node);
    return writeValueAsString(node);
  }

  private static void truncateNode(JsonNode node) {
    if (node.isObject()) {
      ObjectNode obj = (ObjectNode) node;
      obj.properties().forEach(entry -> {
        JsonNode value = entry.getValue();
        if (value.isTextual() && value.asText().length() > MAX_FIELD_LENGTH) {
          obj.put(entry.getKey(), value.asText().substring(0, TRUNCATED_PREFIX_LENGTH)
              + "... [truncated, original size: " + value.asText().length() + " chars]");
        } else {
          truncateNode(value);
        }
      });
    } else if (node.isArray()) {
      for (int i = 0; i < node.size(); i++) {
        truncateNode(node.get(i));
      }
    }
  }
}
