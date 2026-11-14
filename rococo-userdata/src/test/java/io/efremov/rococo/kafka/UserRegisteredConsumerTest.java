package io.efremov.rococo.kafka;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import io.efremov.rococo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserRegisteredConsumerTest {

  @Mock
  private UserService userService;

  private UserRegisteredConsumer consumer;

  @BeforeEach
  void setUp() {
    consumer = new UserRegisteredConsumer(userService);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "\t\n"})
  void consumeUserRegistered_blankUsername_skipsMessage(String username) {
    consumer.consumeUserRegistered(username);

    verifyNoInteractions(userService);
  }

  @Test
  void consumeUserRegistered_validUsername_createsUser() {
    consumer.consumeUserRegistered("test-user");

    verify(userService).createUserIfNotExists("test-user");
  }

  @Test
  void consumeUserRegistered_serviceFailure_propagatesForErrorHandler() {
    var failure = new IllegalArgumentException("Failed to create user");
    doThrow(failure).when(userService).createUserIfNotExists("test-user");

    assertThatThrownBy(() -> consumer.consumeUserRegistered("test-user"))
        .as("service failure")
        .isSameAs(failure);
  }
}
