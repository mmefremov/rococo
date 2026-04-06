package io.efremov.rococo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.efremov.rococo.data.Authority;
import io.efremov.rococo.data.AuthorityEntity;
import io.efremov.rococo.data.UserEntity;
import io.efremov.rococo.data.repository.UserRepository;
import io.efremov.rococo.kafka.UserRegisteredProducer;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private UserRegisteredProducer userRegisteredProducer;

  @InjectMocks
  private UserService userService;

  @Test
  void registerUser_validCredentials_savesUserAndSendsKafkaEvent() {
    when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
    UserEntity savedUser = new UserEntity();
    savedUser.setId(UUID.randomUUID());
    savedUser.setUsername("testuser");
    when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

    String result = userService.registerUser("testuser", "password123");

    assertThat(result).isEqualTo("testuser");
    verify(userRepository).save(any(UserEntity.class));
    verify(userRegisteredProducer).sendUserRegisteredEvent("testuser");
  }

  @Test
  void registerUser_encodesPassword() {
    when(passwordEncoder.encode("rawPassword")).thenReturn("bcrypt_hash");
    UserEntity savedUser = new UserEntity();
    savedUser.setUsername("user");
    when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

    userService.registerUser("user", "rawPassword");

    ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
    verify(userRepository).save(captor.capture());
    assertThat(captor.getValue().getPassword()).isEqualTo("bcrypt_hash");
  }

  @Test
  void registerUser_setsAllAccountFlags() {
    when(passwordEncoder.encode(anyString())).thenReturn("encoded");
    UserEntity savedUser = new UserEntity();
    savedUser.setUsername("user");
    when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

    userService.registerUser("user", "password");

    ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
    verify(userRepository).save(captor.capture());
    UserEntity entity = captor.getValue();
    assertThat(entity.isEnabled()).isTrue();
    assertThat(entity.isAccountNonExpired()).isTrue();
    assertThat(entity.isCredentialsNonExpired()).isTrue();
    assertThat(entity.isAccountNonLocked()).isTrue();
  }

  @Test
  void registerUser_assignsReadAndWriteAuthorities() {
    when(passwordEncoder.encode(anyString())).thenReturn("encoded");
    UserEntity savedUser = new UserEntity();
    savedUser.setUsername("user");
    when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

    userService.registerUser("user", "password");

    ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
    verify(userRepository).save(captor.capture());
    assertThat(captor.getValue().getAuthorities()).hasSize(2);
  }

  @Test
  @DisplayName("Должен выбрасывать DataIntegrityViolationException, если пользователь уже существует")
  void registerUser_duplicateUsername_throwsDataIntegrityViolationException() {
    when(userRepository.findByUsername("existingUser")).thenReturn(Optional.of(new UserEntity()));

    assertThatThrownBy(() -> userService.registerUser("existingUser", "password"))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("existingUser");

    verify(userRepository, never()).save(any());
    verify(userRegisteredProducer, never()).sendUserRegisteredEvent(anyString());
  }

  @Test
  @DisplayName("Должен проверять существование пользователя перед регистрацией")
  void registerUser_checksIfUserExistsBeforeRegistration() {
    when(userRepository.findByUsername("newUser")).thenReturn(Optional.empty());
    when(passwordEncoder.encode(anyString())).thenReturn("encoded");
    UserEntity savedUser = new UserEntity();
    savedUser.setUsername("newUser");
    when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

    userService.registerUser("newUser", "password");

    verify(userRepository).findByUsername("newUser");
  }

  @Test
  @DisplayName("Должен назначать Authority")
  void registerUser_assignsCorrectAuthorities() {
    when(passwordEncoder.encode(anyString())).thenReturn("encoded");
    UserEntity savedUser = new UserEntity();
    savedUser.setUsername("user");
    when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

    userService.registerUser("user", "password");

    ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
    verify(userRepository).save(captor.capture());
    var authorities = captor.getValue().getAuthorities();
    assertThat(authorities).hasSize(2);
    assertThat(authorities).extracting(AuthorityEntity::getAuthority)
        .containsExactlyInAnyOrder(Authority.read, Authority.write);
  }
}
