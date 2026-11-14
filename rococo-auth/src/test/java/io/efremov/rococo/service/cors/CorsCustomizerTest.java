package io.efremov.rococo.service.cors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.web.cors.CorsConfiguration;

@ExtendWith(MockitoExtension.class)
class CorsCustomizerTest {

  private static final String FRONT_URI = "http://localhost:3000";
  private static final String AUTH_URI = "http://localhost:9000";

  private CorsCustomizer corsCustomizer;

  @BeforeEach
  void setUp() {
    corsCustomizer = new CorsCustomizer(FRONT_URI, AUTH_URI);
  }

  @Test
  void allowedOrigins_returnsFrontAndAuthUris() {
    Set<String> allowedOrigins = corsCustomizer.allowedOrigins();

    assertThat(allowedOrigins).containsExactlyInAnyOrder(FRONT_URI, AUTH_URI);
  }

  @Test
  void corsConfigurationSource_returnsCorrectConfiguration() {
    MockHttpServletRequest request = new MockHttpServletRequest();

    CorsConfiguration config = corsCustomizer.corsConfigurationSource().getCorsConfiguration(request);

    assertThat(config).isNotNull();
    assertThat(config.getAllowedOrigins()).containsExactlyInAnyOrder(FRONT_URI, AUTH_URI);
    assertThat(config.getAllowCredentials()).isTrue();
    assertThat(config.getAllowedHeaders()).containsExactly("*");
    assertThat(config.getAllowedMethods()).containsExactly("*");
  }

  @Test
  void customizer_returnsCorsConfigurerCustomizer() {
    var customizer = corsCustomizer.customizer();

    assertThat(customizer).isNotNull();
  }

  @Test
  void corsCustomizer_configuresHttpSecurity() throws Exception {
    HttpSecurity httpSecurity = mock(HttpSecurity.class);
    when(httpSecurity.cors(org.mockito.ArgumentMatchers.any())).thenReturn(httpSecurity);

    corsCustomizer.corsCustomizer(httpSecurity);

    org.mockito.Mockito.verify(httpSecurity).cors(org.mockito.ArgumentMatchers.any());
  }
}
