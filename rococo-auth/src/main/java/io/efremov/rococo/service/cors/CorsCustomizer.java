package io.efremov.rococo.service.cors;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

@Component
public class CorsCustomizer {

  private final String frontUri;
  private final String authUri;
  private final List<String> additionalOrigins;

  @Autowired
  public CorsCustomizer(@Value("${rococo-front.base-uri}") String frontUri,
      @Value("${rococo-auth.base-uri}") String authUri,
      @Value("${rococo-front.additional-origins:}") List<String> additionalOrigins) {
    this.frontUri = frontUri;
    this.authUri = authUri;
    this.additionalOrigins = additionalOrigins;
  }

  public void corsCustomizer(@Nonnull HttpSecurity http) throws Exception {
    http.cors(customizer());
  }

  Customizer<CorsConfigurer<HttpSecurity>> customizer() {
    return c -> c.configurationSource(corsConfigurationSource());
  }

  CorsConfigurationSource corsConfigurationSource() {
    return request -> {
      CorsConfiguration cc = new CorsConfiguration();
      cc.setAllowCredentials(true);
      cc.setAllowedOrigins(new ArrayList<>(allowedOrigins()));
      cc.setAllowedHeaders(List.of("*"));
      cc.setAllowedMethods(List.of("*"));
      return cc;
    };
  }

  public Set<String> allowedOrigins() {
    Set<String> origins = new HashSet<>(Set.of(frontUri, authUri));
    Optional.ofNullable(additionalOrigins).stream()
        .flatMap(Collection::stream)
        .filter(Objects::nonNull)
        .filter(StringUtils::hasText)
        .forEach(origins::add);
    return origins;
  }
}
