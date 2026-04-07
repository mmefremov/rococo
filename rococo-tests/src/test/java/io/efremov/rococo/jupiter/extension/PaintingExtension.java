package io.efremov.rococo.jupiter.extension;


import io.efremov.rococo.jupiter.annotation.AnyPainting;
import io.efremov.rococo.model.PaintingInfoResponse;
import io.efremov.rococo.provider.PaintingProvider;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;

public class PaintingExtension implements ParameterResolver {

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return parameterContext.getParameter().getType().isAssignableFrom(PaintingInfoResponse.class);
  }

  @Override
  public PaintingInfoResponse resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    AuthenticationExtension.setToken();
    if (AnnotationSupport.findAnnotation(extensionContext.getRequiredTestMethod(), AnyPainting.class).isPresent()) {
      return PaintingProvider.getAnyPainting();
    }
    return PaintingProvider.getNewPainting();
  }
}
