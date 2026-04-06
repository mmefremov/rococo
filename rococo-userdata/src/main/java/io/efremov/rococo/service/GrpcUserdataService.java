package io.efremov.rococo.service;

import io.efremov.rococo.data.UserEntity;
import io.efremov.rococo.exception.UserNotFoundException;
import io.efremov.rococo.grpc.UpdateUserRequest;
import io.efremov.rococo.grpc.UserRequest;
import io.efremov.rococo.grpc.UserResponse;
import io.efremov.rococo.grpc.UserdataServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class GrpcUserdataService extends UserdataServiceGrpc.UserdataServiceImplBase {

  private final UserService userService;

  @Override
  public void getUser(UserRequest request, StreamObserver<UserResponse> responseObserver) {
    log.info("gRPC getUser called for username: {}", request.getUsername());
    try {
      UserEntity user = userService.findByUsername(request.getUsername());
      responseObserver.onNext(toResponse(user));
      responseObserver.onCompleted();
    } catch (UserNotFoundException e) {
      log.warn("User not found: {}", request.getUsername());
      responseObserver.onError(Status.NOT_FOUND
          .withDescription(e.getMessage())
          .asRuntimeException());
    } catch (Exception e) {
      log.error("Unexpected error in getUser for username: {}", request.getUsername(), e);
      responseObserver.onError(Status.INTERNAL
          .withDescription("Internal server error")
          .asRuntimeException());
    }
  }

  @Override
  public void updateUser(UpdateUserRequest request, StreamObserver<UserResponse> responseObserver) {
    log.info("gRPC updateUser called for username: {}", request.getUsername());
    try {
      UserEntity user = userService.updateUser(
          request.getUsername(),
          request.getFirstname(),
          request.getLastname(),
          request.getAvatar()
      );
      responseObserver.onNext(toResponse(user));
      responseObserver.onCompleted();
    } catch (UserNotFoundException e) {
      log.warn("User not found for update: {}", request.getUsername());
      responseObserver.onError(Status.NOT_FOUND
          .withDescription(e.getMessage())
          .asRuntimeException());
    } catch (IllegalArgumentException e) {
      log.warn("Invalid argument in updateUser: {}", e.getMessage());
      responseObserver.onError(Status.INVALID_ARGUMENT
          .withDescription(e.getMessage())
          .asRuntimeException());
    } catch (Exception e) {
      log.error("Unexpected error in updateUser for username: {}", request.getUsername(), e);
      responseObserver.onError(Status.INTERNAL
          .withDescription("Internal server error")
          .asRuntimeException());
    }
  }

  private UserResponse toResponse(UserEntity user) {
    return UserResponse.newBuilder()
        .setId(user.getId().toString())
        .setUsername(user.getUserName())
        .setFirstname(Objects.requireNonNullElse(user.getFirstName(), ""))
        .setLastname(Objects.requireNonNullElse(user.getLastName(), ""))
        .setAvatar(user.getAvatar() != null ? new String(user.getAvatar()) : "")
        .build();
  }
}
