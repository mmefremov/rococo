package io.efremov.rococo.kafka;

import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisteredProducer {

  private static final String TOPIC = "user-registered";

  private final KafkaTemplate<String, String> kafkaTemplate;

  public void sendUserRegisteredEvent(@NonNull String username) {
    log.info("Sending user-registered event for username: {}", username);
    CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(TOPIC, username, username);
    future.whenComplete((result, ex) -> {
      if (ex != null) {
        log.error("Failed to send user-registered event for username: {}", username, ex);
      } else {
        log.debug("user-registered event sent for username: {}, offset: {}",
            username, result.getRecordMetadata().offset());
      }
    });
  }
}
