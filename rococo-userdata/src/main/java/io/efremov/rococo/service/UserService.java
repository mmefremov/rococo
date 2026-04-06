package io.efremov.rococo.service;

import io.efremov.rococo.data.UserEntity;
import io.efremov.rococo.data.repository.UserRepository;
import io.efremov.rococo.exception.UserNotFoundException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  @Transactional
  public @NonNull UserEntity createUserIfNotExists(@NonNull String username) {
    if (username == null || username.trim().isBlank()) {
      throw new IllegalArgumentException("username: must not be blank");
    }
    if (username.length() < 3 || username.length() > 50) {
      throw new IllegalArgumentException("username: must be from 3 to 50 characters");
    }
    if (userRepository.existsByUserName(username)) {
      log.warn("User already exists, skipping creation: {}", username);
      return userRepository.findByUserName(username).orElseThrow();
    }
    log.info("Creating user profile for: {}", username);
    UserEntity user = new UserEntity();
    user.setUserName(username);
    return userRepository.save(user);
  }

  @Transactional
  public @NonNull UserEntity updateUser(@NonNull String username, @NonNull String firstName, @NonNull String lastName,
      @NonNull String avatarBase64) {
    UserEntity user = findByUsername(username);
    if (!firstName.isBlank() && firstName.length() > 255) {
      throw new IllegalArgumentException("firstName: must not exceed 255 characters");
    }
    if (!lastName.isBlank() && lastName.length() > 255) {
      throw new IllegalArgumentException("lastName: must not exceed 255 characters");
    }
    if (avatarBase64 != null && avatarBase64.length() > 1_048_576) {
      throw new IllegalArgumentException("photo: must not exceed 1 MB");
    }
    log.info("Updating user profile for: {}", username);
    if (!firstName.isBlank()) {
      user.setFirstName(firstName);
    }
    if (!lastName.isBlank()) {
      user.setLastName(lastName);
    }
    if (!avatarBase64.isBlank()) {
      user.setAvatar(avatarBase64.getBytes(StandardCharsets.UTF_8));
    }
    return userRepository.save(user);
  }

  @Transactional(readOnly = true)
  public @NonNull UserEntity findByUsername(@NonNull String username) {
    log.debug("Looking up user by username: {}", username);
    return userRepository.findByUserName(username)
        .orElseThrow(() -> new UserNotFoundException(username));
  }
}
