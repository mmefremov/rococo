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
    try {
      userService.createUserIfNotExists(username);
    } catch (IllegalArgumentException e) {
      log.warn("Skipping invalid user registration message: {}", e.getMessage());
    } catch (Exception e) {
      log.error("Failed to process user-registered event for username: {}", username, e);
      throw e;
    }
  }
}
