package io.efremov.rococo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.efremov.rococo.grpc.UpdateUserRequest;
import io.efremov.rococo.grpc.UserRequest;
import io.efremov.rococo.grpc.UserResponse;
import io.efremov.rococo.grpc.UserdataServiceGrpc;
import io.efremov.rococo.model.UpdateUserInfoRequest;
import io.efremov.rococo.model.UserInfoResponse;
import io.grpc.Status;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GrpcUserdataClientTest {

  @Mock
  private UserdataServiceGrpc.UserdataServiceBlockingStub userdataStub;
  @InjectMocks
  private GrpcUserdataClient client;

  private final UUID id = UUID.randomUUID();

  @Test
  void getUser_existingUsername_mapsAllFields() {
    when(userdataStub.getUser(UserRequest.newBuilder().setUsername("user").build()))
        .thenReturn(user().setFirstname("Jean").setLastname("Monet").setAvatar("image").build());

    assertThat(client.getUser("user")).as("user")
        .isEqualTo(new UserInfoResponse(id, "user", "Jean", "Monet", "image"));
  }

  @Test
  void getUser_emptyOptionalFields_returnsNulls() {
    when(userdataStub.getUser(any())).thenReturn(user().build());

    assertThat(client.getUser("user")).as("user without profile")
        .isEqualTo(new UserInfoResponse(id, "user", null, null, null));
  }

  @Test
  void getUser_whitespaceFields_preservesWhitespace() {
    when(userdataStub.getUser(any())).thenReturn(user().setFirstname(" ").build());

    assertThat(client.getUser("user").firstname()).as("firstname").isEqualTo(" ");
  }

  @Test
  void updateUser_allFields_mapsRequestAndResponse() {
    when(userdataStub.updateUser(UpdateUserRequest.newBuilder().setUsername("user")
        .setFirstname("Jean").setLastname("Monet").setAvatar("image").build()))
        .thenReturn(user().setFirstname("Jean").setLastname("Monet").setAvatar("image").build());

    assertThat(client.updateUser("user", new UpdateUserInfoRequest("Jean", "Monet", "image")))
        .as("updated user").isEqualTo(new UserInfoResponse(id, "user", "Jean", "Monet", "image"));
  }

  @Test
  void updateUser_nullFields_omitsOptionalFields() {
    when(userdataStub.updateUser(UpdateUserRequest.newBuilder().setUsername("user").build()))
        .thenReturn(user().build());

    assertThat(client.updateUser("user", new UpdateUserInfoRequest(null, null, null)))
        .as("updated user").isEqualTo(new UserInfoResponse(id, "user", null, null, null));
  }

  @Test
  void updateUser_emptyFields_sendsExplicitEmptyValues() {
    when(userdataStub.updateUser(UpdateUserRequest.newBuilder().setUsername("user")
        .setFirstname("").setLastname("").setAvatar("").build())).thenReturn(user().build());

    assertThat(client.updateUser("user", new UpdateUserInfoRequest("", "", "")))
        .as("cleared profile").isEqualTo(new UserInfoResponse(id, "user", null, null, null));
  }

  @Test
  void getUser_rpcFailure_propagatesException() {
    var error = Status.NOT_FOUND.asRuntimeException();
    when(userdataStub.getUser(any())).thenThrow(error);

    assertThatThrownBy(() -> client.getUser("missing")).as("RPC failure").isSameAs(error);
  }

  private UserResponse.Builder user() {
    return UserResponse.newBuilder().setId(id.toString()).setUsername("user");
  }
}
