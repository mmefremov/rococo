package io.efremov.rococo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.efremov.rococo.data.UserEntity;
import io.efremov.rococo.data.repository.UserRepository;
import io.efremov.rococo.exception.UserNotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserService userService;

  @Test
  void findByUsername_existingUser_returnsUser() {
    UserEntity entity = createUser("john");
    when(userRepository.findByUserName("john")).thenReturn(Optional.of(entity));

    UserEntity result = userService.findByUsername("john");

    assertThat(result.getUserName()).isEqualTo("john");
  }

  private UserEntity createUser(String username) {
    UserEntity entity = new UserEntity();
    entity.setId(UUID.randomUUID());
    entity.setUserName(username);
    return entity;
  }

  @Test
  void findByUsername_nonExisting_throwsUserNotFoundException() {
    when(userRepository.findByUserName("unknown")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.findByUsername("unknown"))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("unknown");
  }

  @Test
  void createUserIfNotExists_newUser_createsUser() {
    when(userRepository.existsByUserName("newuser")).thenReturn(false);
    UserEntity saved = createUser("newuser");
    when(userRepository.save(any(UserEntity.class))).thenReturn(saved);

    UserEntity result = userService.createUserIfNotExists("newuser");

    assertThat(result.getUserName()).isEqualTo("newuser");
    verify(userRepository).save(any(UserEntity.class));
  }

  @Test
  void createUserIfNotExists_existingUser_skipsCreation() {
    UserEntity existing = createUser("existinguser");
    when(userRepository.existsByUserName("existinguser")).thenReturn(true);
    when(userRepository.findByUserName("existinguser")).thenReturn(Optional.of(existing));

    UserEntity result = userService.createUserIfNotExists("existinguser");

    assertThat(result.getUserName()).isEqualTo("existinguser");
    verify(userRepository, never()).save(any());
  }

  @Test
  void updateUser_existingUser_updatesFields() {
    UserEntity entity = createUser("alice");
    when(userRepository.findByUserName("alice")).thenReturn(Optional.of(entity));
    when(userRepository.save(any(UserEntity.class))).thenReturn(entity);

    UserEntity result = userService.updateUser("alice", "Alice", "Smith", "");

    assertThat(result.getFirstName()).isEqualTo("Alice");
    assertThat(result.getLastName()).isEqualTo("Smith");
  }

  @Test
  void createUserIfNotExists_blankUsername_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> userService.createUserIfNotExists("   "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("username");
  }

  @Test
  void createUserIfNotExists_tooLongUsername_throwsIllegalArgumentException() {
    String longUsername = "u".repeat(51);
    assertThatThrownBy(() -> userService.createUserIfNotExists(longUsername))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("username");
  }

  @Test
  void updateUser_blankUsername_throwsUserNotFoundException() {
    when(userRepository.findByUserName("  ")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.updateUser("  ", "", "", ""))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void updateUser_nonExistentUsername_throwsUserNotFoundException() {
    String longUsername = "u".repeat(51);
    when(userRepository.findByUserName(longUsername)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.updateUser(longUsername, "", "", ""))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void updateUser_firstNameTooLong_throwsIllegalArgumentException() {
    UserEntity entity = createUser("alice");
    when(userRepository.findByUserName("alice")).thenReturn(Optional.of(entity));
    String longName = "n".repeat(256);

    assertThatThrownBy(() -> userService.updateUser("alice", longName, "", ""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("firstName");
  }

  @Test
  void updateUser_lastNameTooLong_throwsIllegalArgumentException() {
    UserEntity entity = createUser("alice");
    when(userRepository.findByUserName("alice")).thenReturn(Optional.of(entity));
    String longName = "n".repeat(256);

    assertThatThrownBy(() -> userService.updateUser("alice", "", longName, ""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("lastName");
  }
}
