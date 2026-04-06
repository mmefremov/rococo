package io.efremov.rococo.service;

import io.efremov.rococo.grpc.UpdateUserRequest;
import io.efremov.rococo.grpc.UserRequest;
import io.efremov.rococo.grpc.UserResponse;
import io.efremov.rococo.grpc.UserdataServiceGrpc;
import io.efremov.rococo.model.UpdateUserInfoRequest;
import io.efremov.rococo.model.UserInfoResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GrpcUserdataClient {

  @GrpcClient("userdata")
  private UserdataServiceGrpc.UserdataServiceBlockingStub userdataStub;

  public UserInfoResponse getUser(String username) {
    log.debug("Calling userdata service: getUser username={}", username);
    UserResponse response = userdataStub.getUser(
        UserRequest.newBuilder().setUsername(username).build()
    );
    return toModel(response);
  }

  private UserInfoResponse toModel(UserResponse response) {
    return new UserInfoResponse(
        UUID.fromString(response.getId()),
        response.getUsername(),
        toNull(response.getFirstname()),
        toNull(response.getLastname()),
        toNull(response.getAvatar())
    );
  }

  private String toNull(String value) {
    return value.isEmpty() ? null : value;
  }

  public UserInfoResponse updateUser(String username, @Valid UpdateUserInfoRequest request) {
    log.debug("Calling userdata service: updateUser username={}", username);
    var builder = UpdateUserRequest.newBuilder()
        .setUsername(username);
    if (request.firstname() != null) {
      builder.setFirstname(request.firstname());
    }
    if (request.lastname() != null) {
      builder.setLastname(request.lastname());
    }
    if (request.avatar() != null) {
      builder.setAvatar(request.avatar());
    }
    var response = userdataStub.updateUser(builder.build());
    return toModel(response);
  }
}
