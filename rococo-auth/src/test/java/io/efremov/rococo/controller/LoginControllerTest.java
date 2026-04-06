package io.efremov.rococo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

  private static final String FRONT_URI = "http://localhost:3000";

  private LoginController loginController;

  @BeforeEach
  void setUp() {
    loginController = new LoginController(FRONT_URI);
  }

  @Test
  @DisplayName("GET /login - должна возвращать login view, если сессия содержит redirect_uri")
  void login_withOAuthSession_returnsLoginView() {
    MockHttpSession session = new MockHttpSession();
    DefaultSavedRequest savedRequest = mock(DefaultSavedRequest.class);
    when(savedRequest.getRequestURI()).thenReturn("/oauth2/authorize");
    when(savedRequest.getParameterValues("redirect_uri")).thenReturn(new String[]{"http://localhost:3000/callback"});
    session.setAttribute("SPRING_SECURITY_SAVED_REQUEST", savedRequest);

    String viewName = loginController.login(session);

    assertThat(viewName).isEqualTo("login");
  }

  @Test
  @DisplayName("GET /login - должна делать редирект на frontUri, если сессия не содержит redirect_uri")
  void login_withoutOAuthSession_redirectsToFront() {
    MockHttpSession session = new MockHttpSession();

    String viewName = loginController.login(session);

    assertThat(viewName).isEqualTo("redirect:" + FRONT_URI);
  }

  @Test
  @DisplayName("GET /login - должна делать редирект, если savedRequest имеет другой URI")
  void login_withDifferentUri_redirectsToFront() {
    MockHttpSession session = new MockHttpSession();
    DefaultSavedRequest savedRequest = mock(DefaultSavedRequest.class);
    when(savedRequest.getRequestURI()).thenReturn("/api/something");
    session.setAttribute("SPRING_SECURITY_SAVED_REQUEST", savedRequest);

    String viewName = loginController.login(session);

    assertThat(viewName).isEqualTo("redirect:" + FRONT_URI);
  }

  @Test
  @DisplayName("GET /login - должна делать редирект, если redirect_uri не содержит frontUri")
  void login_withDifferentRedirectUri_redirectsToFront() {
    MockHttpSession session = new MockHttpSession();
    DefaultSavedRequest savedRequest = mock(DefaultSavedRequest.class);
    when(savedRequest.getRequestURI()).thenReturn("/oauth2/authorize");
    when(savedRequest.getParameterValues("redirect_uri")).thenReturn(new String[]{"http://other-domain.com/callback"});
    session.setAttribute("SPRING_SECURITY_SAVED_REQUEST", savedRequest);

    String viewName = loginController.login(session);

    assertThat(viewName).isEqualTo("redirect:" + FRONT_URI);
  }

  @Test
  @DisplayName("GET /login - должна делать редирект, если savedRequest равен null")
  void login_withNullSavedRequest_redirectsToFront() {
    MockHttpSession session = new MockHttpSession();
    session.setAttribute("SPRING_SECURITY_SAVED_REQUEST", null);

    String viewName = loginController.login(session);

    assertThat(viewName).isEqualTo("redirect:" + FRONT_URI);
  }

  @Test
  @DisplayName("GET / - должна возвращать login view для неаутентифицированного пользователя")
  void root_withoutAuthentication_returnsLoginView() {
    String viewName = loginController.root(null);

    assertThat(viewName).isEqualTo("login");
  }

  @Test
  @DisplayName("GET / - должна делать редирект на frontUri для аутентифицированного пользователя")
  void root_withAuthentication_redirectsToFront() {
    var authentication = mock(org.springframework.security.core.Authentication.class);
    when(authentication.isAuthenticated()).thenReturn(true);

    String viewName = loginController.root(authentication);

    assertThat(viewName).isEqualTo("redirect:" + FRONT_URI);
  }

  @Test
  @DisplayName("GET / - должна возвращать login view для не-authenticated authentication объекта")
  void root_withNotAuthenticatedAuthentication_returnsLoginView() {
    var authentication = mock(org.springframework.security.core.Authentication.class);
    when(authentication.isAuthenticated()).thenReturn(false);

    String viewName = loginController.root(authentication);

    assertThat(viewName).isEqualTo("login");
  }
}
