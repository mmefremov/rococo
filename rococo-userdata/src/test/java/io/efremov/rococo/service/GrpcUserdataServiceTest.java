package io.efremov.rococo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.efremov.rococo.data.UserEntity;
import io.efremov.rococo.exception.UserNotFoundException;
import io.efremov.rococo.grpc.UpdateUserRequest;
import io.efremov.rococo.grpc.UserRequest;
import io.efremov.rococo.grpc.UserResponse;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GrpcUserdataServiceTest {

  @Mock
  private UserService userService;

  @Mock
  private StreamObserver<UserResponse> responseObserver;

  @InjectMocks
  private GrpcUserdataService grpcUserdataService;

  @Test
  void getUser_existingUser_sendsResponse() {
    UserEntity entity = createUser("alice");
    when(userService.findByUsername("alice")).thenReturn(entity);

    grpcUserdataService.getUser(
        UserRequest.newBuilder().setUsername("alice").build(),
        responseObserver
    );

    ArgumentCaptor<UserResponse> captor = ArgumentCaptor.forClass(UserResponse.class);
    verify(responseObserver).onNext(captor.capture());
    verify(responseObserver).onCompleted();
    verifyNoMoreInteractions(responseObserver);

    assertThat(captor.getValue().getUsername()).isEqualTo("alice");
    assertThat(captor.getValue().getId()).isEqualTo(entity.getId().toString());
  }

  @Test
  void getUser_notFound_sendsNotFoundError() {
    when(userService.findByUsername("unknown")).thenThrow(new UserNotFoundException("unknown"));

    grpcUserdataService.getUser(
        UserRequest.newBuilder().setUsername("unknown").build(),
        responseObserver
    );

    ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
    verify(responseObserver).onError(captor.capture());

    StatusRuntimeException ex = (StatusRuntimeException) captor.getValue();
    assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND);
  }

  @Test
  void updateUser_existingUser_sendsResponse() {
    UserEntity entity = createUser("bob");
    entity.setFirstName("Bob");
    when(userService.updateUser(eq("bob"), any(), any(), any())).thenReturn(entity);

    grpcUserdataService.updateUser(
        UpdateUserRequest.newBuilder()
            .setUsername("bob")
            .setFirstname("Bob")
            .setLastname("")
            .setAvatar("")
            .build(),
        responseObserver
    );

    ArgumentCaptor<UserResponse> captor = ArgumentCaptor.forClass(UserResponse.class);
    verify(responseObserver).onNext(captor.capture());
    verify(responseObserver).onCompleted();
    assertThat(captor.getValue().getUsername()).isEqualTo("bob");
  }

  @Test
  void updateUser_notFound_sendsNotFoundError() {
    when(userService.updateUser(eq("ghost"), any(), any(), any()))
        .thenThrow(new UserNotFoundException("ghost"));

    grpcUserdataService.updateUser(
        UpdateUserRequest.newBuilder().setUsername("ghost").build(),
        responseObserver
    );

    ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
    verify(responseObserver).onError(captor.capture());

    StatusRuntimeException ex = (StatusRuntimeException) captor.getValue();
    assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND);
  }

  @Test
  void updateUser_invalidUsername_sendsInvalidArgumentError() {
    when(userService.updateUser(eq("u".repeat(51)), any(), any(), any()))
        .thenThrow(new IllegalArgumentException("username: must not exceed 50 characters"));

    grpcUserdataService.updateUser(
        UpdateUserRequest.newBuilder().setUsername("u".repeat(51)).build(),
        responseObserver
    );

    ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
    verify(responseObserver).onError(captor.capture());

    StatusRuntimeException ex = (StatusRuntimeException) captor.getValue();
    assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
    assertThat(ex.getStatus().getDescription()).contains("username");
  }

  private UserEntity createUser(String username) {
    UserEntity entity = new UserEntity();
    entity.setId(UUID.randomUUID());
    entity.setUserName(username);
    return entity;
  }
}
