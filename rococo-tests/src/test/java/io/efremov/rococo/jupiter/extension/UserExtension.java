package io.efremov.rococo.jupiter.extension;


import io.efremov.rococo.jupiter.annotation.AnyUser;
import io.efremov.rococo.model.UserInfoResponse;
import io.efremov.rococo.provider.UserProvider;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;

public class UserExtension implements ParameterResolver {

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return parameterContext.getParameter().getType().isAssignableFrom(UserInfoResponse.class);
  }

  @Override
  public UserInfoResponse resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    if (AnnotationSupport.findAnnotation(extensionContext.getRequiredTestMethod(), AnyUser.class).isPresent()) {
      return UserProvider.getAnyUser();
    }
    return UserProvider.getNewUser();
  }
}
