package io.efremov.rococo.jupiter.extension;


import io.efremov.rococo.jupiter.annotation.AnyArtist;
import io.efremov.rococo.model.ArtistInfoResponse;
import io.efremov.rococo.provider.ArtistProvider;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;

public class ArtistExtension implements ParameterResolver {

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return parameterContext.getParameter().getType().isAssignableFrom(ArtistInfoResponse.class);
  }

  @Override
  public ArtistInfoResponse resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    AuthenticationExtension.setToken();
    if (AnnotationSupport.findAnnotation(extensionContext.getRequiredTestMethod(), AnyArtist.class).isPresent()) {
      return ArtistProvider.getAnyArtist();
    }
    return ArtistProvider.getNewArtist();
  }
}
