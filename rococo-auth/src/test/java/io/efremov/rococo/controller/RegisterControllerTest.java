package io.efremov.rococo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.efremov.rococo.model.RegistrationForm;
import io.efremov.rococo.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

@ExtendWith(MockitoExtension.class)
class RegisterControllerTest {

  private static final String FRONT_URI = "http://localhost:3000";

  @Mock
  private UserService userService;

  @Mock
  private Model model;

  private RegisterController registerController;

  @BeforeEach
  void setUp() {
    registerController = new RegisterController(userService, FRONT_URI);
  }

  @Test
  @DisplayName("GET /register - должна возвращать страницу регистрации с пустой формой")
  void getRegisterPage_returnsRegistrationView() {
    String viewName = registerController.getRegisterPage(model);

    assertThat(viewName).isEqualTo("register");
    verify(model).addAttribute("registrationForm", new RegistrationForm(null, null, null));
    verify(model).addAttribute("frontUri", FRONT_URI);
  }

  @Test
  @DisplayName("POST /register - успешная регистрация должна возвращать 201 и имя пользователя")
  void registerUser_validData_returnsCreated() {
    RegistrationForm form = new RegistrationForm("testuser", "password123", "password123");
    MockHttpServletResponse response = new MockHttpServletResponse();
    when(userService.registerUser("testuser", "password123")).thenReturn("testuser");

    Errors errors = new BeanPropertyBindingResult(form, "registrationForm");
    String viewName = registerController.registerUser(form, errors, model, response);

    assertThat(viewName).isEqualTo("register");
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_CREATED);
    verify(model).addAttribute("username", "testuser");
    verify(model).addAttribute("frontUri", FRONT_URI);
  }

  @Test
  @DisplayName("POST /register - при дубликате username должна возвращать 400")
  void registerUser_duplicateUsername_returnsBadRequest() {
    RegistrationForm form = new RegistrationForm("existingUser", "password123", "password123");
    MockHttpServletResponse response = new MockHttpServletResponse();
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(form, "registrationForm");
    when(model.getAttribute("org.springframework.validation.BindingResult.registrationForm"))
        .thenReturn(bindingResult);
    when(userService.registerUser(anyString(), anyString()))
        .thenThrow(new DataIntegrityViolationException("Username `existingUser` already exists"));

    Errors errors = new BeanPropertyBindingResult(form, "registrationForm");
    String viewName = registerController.registerUser(form, errors, model, response);

    assertThat(viewName).isEqualTo("register");
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    verify(model).addAttribute("frontUri", FRONT_URI);
  }

  @Test
  @DisplayName("POST /register - при ошибках валидации должна возвращать 400")
  void registerUser_validationErrors_returnsBadRequest() {
    RegistrationForm form = new RegistrationForm("", "", "");
    MockHttpServletResponse response = new MockHttpServletResponse();

    Errors errors = new BeanPropertyBindingResult(form, "registrationForm");
    errors.rejectValue("username", "NotBlank", "Username can not be blank");
    errors.rejectValue("password", "NotBlank", "Password can not be blank");

    String viewName = registerController.registerUser(form, errors, model, response);

    assertThat(viewName).isEqualTo("register");
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    verify(userService, never()).registerUser(anyString(), anyString());
  }

  @Test
  @DisplayName("POST /register - addErrorToRegistrationModel должна добавлять ошибку в модель")
  void registerUser_addsErrorToModelOnDuplicate() {
    RegistrationForm form = new RegistrationForm("existingUser", "password123", "password123");
    MockHttpServletResponse response = new MockHttpServletResponse();
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(form, "registrationForm");
    when(model.getAttribute("org.springframework.validation.BindingResult.registrationForm"))
        .thenReturn(bindingResult);
    when(userService.registerUser(anyString(), anyString()))
        .thenThrow(new DataIntegrityViolationException("Username `existingUser` already exists"));

    Errors errors = new BeanPropertyBindingResult(form, "registrationForm");
    String viewName = registerController.registerUser(form, errors, model, response);

    assertThat(viewName).isEqualTo("register");
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    assertThat(bindingResult.hasErrors()).isTrue();
    FieldError fieldError = bindingResult.getFieldError("username");
    assertThat(fieldError).isNotNull();
    assertThat(fieldError.getDefaultMessage()).contains("existingUser");
  }
}
