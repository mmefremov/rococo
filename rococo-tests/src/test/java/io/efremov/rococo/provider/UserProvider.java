package io.efremov.rococo.provider;

import static io.efremov.rococo.config.Constants.DEFAULT_PASSWORD;

import io.efremov.rococo.data.entity.UserEntity;
import io.efremov.rococo.data.repository.UserRepository;
import io.efremov.rococo.jupiter.extension.AuthenticationExtension;
import io.efremov.rococo.model.UpdateUserInfoRequest;
import io.efremov.rococo.model.UserInfoResponse;
import io.efremov.rococo.service.GatewayApiClient;
import io.efremov.rococo.util.RandomDataUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.instancio.Instancio;
import org.instancio.Model;
import org.instancio.Select;
import org.jspecify.annotations.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserProvider {

  public static UserInfoResponse getAnyUser() {
    UserEntity entity = new UserRepository().findAnyUser();
    if (entity != null) {
      String username = entity.getUserName();
      String token = AuthProvider.getRegisteredUserAuthToken(username, DEFAULT_PASSWORD);
      AuthenticationExtension.setToken(token);
      return new GatewayApiClient().getUser().body();
    }
    return getNewUser();
  }

  public static UserInfoResponse getNewUser() {
    String token = AuthProvider.getNewUserAuthToken();
    AuthenticationExtension.setToken(token);
    return new GatewayApiClient().getUser().body();
  }

  public static @NonNull UpdateUserInfoRequest getUpdateUserRequest() {
    return Instancio.create(getUpdateUserRequestModel());
  }

  public static @NonNull Model<UpdateUserInfoRequest> getUpdateUserRequestModel() {
    return Instancio.of(UpdateUserInfoRequest.class)
        .generate(Select.field(UpdateUserInfoRequest::firstname),
            gen -> gen.oneOf(RandomDataUtils.randomFirstName(), null))
        .generate(Select.field(UpdateUserInfoRequest::lastname),
            gen -> gen.oneOf(RandomDataUtils.randomLastName(), null))
        .generate(Select.field(UpdateUserInfoRequest::avatar),
            gen -> gen.oneOf(RandomDataUtils.randomPhoto(), null))
        .toModel();
  }
}
