package io.efremov.rococo.service;

import io.efremov.rococo.common.grpc.GrpcServiceTemplate;
import io.efremov.rococo.common.util.ByteStringCodec;
import io.efremov.rococo.data.UserEntity;
import io.efremov.rococo.grpc.UpdateUserRequest;
import io.efremov.rococo.grpc.UserRequest;
import io.efremov.rococo.grpc.UserResponse;
import io.efremov.rococo.grpc.UserdataServiceGrpc;
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
    GrpcServiceTemplate.unary(responseObserver, () -> {
      UserEntity user = userService.findByUsername(request.getUsername());
      return toResponse(user);
    });
  }

  @Override
  public void updateUser(UpdateUserRequest request, StreamObserver<UserResponse> responseObserver) {
    log.info("gRPC updateUser called for username: {}", request.getUsername());
    GrpcServiceTemplate.unary(responseObserver, () -> {
      UserEntity user = userService.updateUser(
          request.getUsername(),
          request.getFirstname(),
          request.getLastname(),
          request.getAvatar()
      );
      return toResponse(user);
    });
  }

  private UserResponse toResponse(UserEntity user) {
    return UserResponse.newBuilder()
        .setId(user.getId().toString())
        .setUsername(user.getUserName())
        .setFirstname(Objects.requireNonNullElse(user.getFirstName(), ""))
        .setLastname(Objects.requireNonNullElse(user.getLastName(), ""))
        .setAvatar(user.getAvatar() != null ? ByteStringCodec.decode(user.getAvatar()) : "")
        .build();
  }
}
