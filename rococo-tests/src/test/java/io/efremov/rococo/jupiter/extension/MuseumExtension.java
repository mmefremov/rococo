package io.efremov.rococo.jupiter.extension;


import io.efremov.rococo.jupiter.annotation.AnyMuseum;
import io.efremov.rococo.model.MuseumInfoResponse;
import io.efremov.rococo.provider.MuseumProvider;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;

public class MuseumExtension implements ParameterResolver {

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return parameterContext.getParameter().getType().isAssignableFrom(MuseumInfoResponse.class);
  }

  @Override
  public MuseumInfoResponse resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    AuthenticationExtension.setToken();
    if (AnnotationSupport.findAnnotation(extensionContext.getRequiredTestMethod(), AnyMuseum.class).isPresent()) {
      return MuseumProvider.getAnyMuseum();
    }
    return MuseumProvider.getNewMuseum();
  }
}
