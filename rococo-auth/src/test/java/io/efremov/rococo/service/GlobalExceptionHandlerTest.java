package io.efremov.rococo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GlobalExceptionHandlerTest {

  private static final String FRONT_URI = "http://localhost:3000";

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private Model model;

  private GlobalExceptionHandler globalExceptionHandler;

  @BeforeEach
  void setUp() {
    globalExceptionHandler = new GlobalExceptionHandler(FRONT_URI);
  }

  @Test
  @DisplayName("Должен обрабатывать NoResourceFoundException как 404")
  void handleAnyException_noResourceFound_returns404() {
    NoResourceFoundException exception = new NoResourceFoundException(HttpMethod.GET, "/api/nonexistent");
    when(request.getRequestURI()).thenReturn("/api/nonexistent");
    when(response.getStatus()).thenReturn(HttpServletResponse.SC_OK);

    String viewName = globalExceptionHandler.handleAnyException(exception, request, response, model);

    assertThat(viewName).isEqualTo("error");
    verify(model).addAttribute("status", HttpServletResponse.SC_NOT_FOUND);
    verify(model).addAttribute("error", HttpStatus.NOT_FOUND.getReasonPhrase());
    verify(model).addAttribute("message", exception.getMessage());
    verify(model).addAttribute("path", "/api/nonexistent");
    verify(model).addAttribute("frontUri", FRONT_URI);
    verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
  }

  @Test
  @DisplayName("Должен обрабатывать общее исключение с кодом ошибки из запроса")
  void handleAnyException_withErrorCodeAttribute_usesStatusCodeFromRequest() {
    Exception exception = new RuntimeException("Test exception");
    when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(HttpServletResponse.SC_BAD_REQUEST);
    when(request.getRequestURI()).thenReturn("/api/test");

    String viewName = globalExceptionHandler.handleAnyException(exception, request, response, model);

    assertThat(viewName).isEqualTo("error");
    verify(model).addAttribute("status", HttpServletResponse.SC_BAD_REQUEST);
    verify(model).addAttribute("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
    verify(model).addAttribute("message", "Test exception");
    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
  }

  @Test
  @DisplayName("Должен обрабатывать общее исключение со статусом из response")
  void handleAnyException_withOkStatus_defaultsTo500() {
    Exception exception = new RuntimeException("Internal error");
    when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(null);
    when(response.getStatus()).thenReturn(HttpServletResponse.SC_OK);
    when(request.getRequestURI()).thenReturn("/api/error");

    String viewName = globalExceptionHandler.handleAnyException(exception, request, response, model);

    assertThat(viewName).isEqualTo("error");
    verify(model).addAttribute("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    verify(model).addAttribute("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
    verify(response).setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
  }

  @Test
  @DisplayName("Должен обрабатывать исключение с неизвестным статусом как 500")
  void handleAnyException_withUnknownStatus_defaultsTo500() {
    Exception exception = new RuntimeException("Unknown error");
    when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(999);
    when(response.getStatus()).thenReturn(999);
    when(request.getRequestURI()).thenReturn("/api/error");

    String viewName = globalExceptionHandler.handleAnyException(exception, request, response, model);

    assertThat(viewName).isEqualTo("error");
    verify(model).addAttribute("status", 999);
    verify(model).addAttribute("error", "Internal Server Error");
  }

  @Test
  @DisplayName("Должен добавлять все необходимые атрибуты в модель")
  void handleAnyException_addsAllAttributesToModel() {
    Exception exception = new IllegalStateException("State error");
    when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(HttpServletResponse.SC_CONFLICT);
    when(request.getRequestURI()).thenReturn("/api/resource");

    String viewName = globalExceptionHandler.handleAnyException(exception, request, response, model);

    assertThat(viewName).isEqualTo("error");
    verify(model).addAttribute("status", HttpServletResponse.SC_CONFLICT);
    verify(model).addAttribute("error", HttpStatus.CONFLICT.getReasonPhrase());
    verify(model).addAttribute("message", "State error");
    verify(model).addAttribute("path", "/api/resource");
    verify(model).addAttribute("frontUri", FRONT_URI);
  }

  @Test
  @DisplayName("Должен обрабатывать 401 Unauthorized")
  void handleAnyException_unauthorized_returns401() {
    Exception exception = new RuntimeException("Unauthorized");
    when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(HttpServletResponse.SC_UNAUTHORIZED);
    when(request.getRequestURI()).thenReturn("/api/protected");

    String viewName = globalExceptionHandler.handleAnyException(exception, request, response, model);

    assertThat(viewName).isEqualTo("error");
    verify(model).addAttribute("status", HttpServletResponse.SC_UNAUTHORIZED);
    verify(model).addAttribute("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }

  @Test
  @DisplayName("Должен обрабатывать 403 Forbidden")
  void handleAnyException_forbidden_returns403() {
    Exception exception = new RuntimeException("Forbidden");
    when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(HttpServletResponse.SC_FORBIDDEN);
    when(request.getRequestURI()).thenReturn("/api/admin");

    String viewName = globalExceptionHandler.handleAnyException(exception, request, response, model);

    assertThat(viewName).isEqualTo("error");
    verify(model).addAttribute("status", HttpServletResponse.SC_FORBIDDEN);
    verify(model).addAttribute("error", HttpStatus.FORBIDDEN.getReasonPhrase());
    verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
  }
}
