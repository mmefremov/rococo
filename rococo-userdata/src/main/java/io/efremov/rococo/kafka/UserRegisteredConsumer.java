package io.efremov.rococo.kafka;

import io.efremov.rococo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisteredConsumer {

  private final UserService userService;

  @KafkaListener(topics = "user-registered", groupId = "rococo-userdata")
  public void consumeUserRegistered(String username) {
    log.info("Received user-registered event for username: {}", username);
    if (username == null || username.isBlank()) {
      log.warn("Skipping blank username in user-registered event");
      return;
    }
    userService.createUserIfNotExists(username);
  }
}
