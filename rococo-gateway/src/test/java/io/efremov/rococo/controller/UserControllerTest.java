package io.efremov.rococo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.efremov.rococo.exception.GrpcExceptionHandler;
import io.efremov.rococo.model.UpdateUserInfoRequest;
import io.efremov.rococo.model.UserInfoResponse;
import io.efremov.rococo.service.GrpcUserdataClient;
import io.grpc.Status;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

  @Mock
  private GrpcUserdataClient userdataClient;
  @InjectMocks
  private UserController controller;

  private final Jwt jwt = Jwt.withTokenValue("token").header("alg", "RS256").subject("user")
      .claim("preferred_username", "another-user").build();
  private final UserInfoResponse user = new UserInfoResponse(UUID.randomUUID(), "user", "Jean", "Monet", "image");
  private final ObjectMapper objectMapper = new ObjectMapper();
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(new GrpcExceptionHandler())
        .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
        .build();
    SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void getUser_usesJwtSubject() throws Exception {
    when(userdataClient.getUser("user")).thenReturn(user);

    var result = mockMvc.perform(get("/api/user")).andReturn();

    assertThat(result.getResponse().getStatus()).as("HTTP status").isEqualTo(200);
    assertThat(objectMapper.readValue(result.getResponse().getContentAsString(), UserInfoResponse.class))
        .as("current user").isEqualTo(user);
  }

  @Test
  void updateUser_usesJwtSubjectAndRequestFields() throws Exception {
    var request = new UpdateUserInfoRequest("Jean", "Monet", "image");
    when(userdataClient.updateUser("user", request)).thenReturn(user);

    var result = mockMvc.perform(patch("/api/user").contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))).andReturn();

    assertThat(result.getResponse().getStatus()).as("HTTP status").isEqualTo(200);
    assertThat(objectMapper.readValue(result.getResponse().getContentAsString(), UserInfoResponse.class))
        .as("updated current user").isEqualTo(user);
  }

  @Test
  void getUser_grpcNotFound_returnsNotFound() throws Exception {
    when(userdataClient.getUser("user"))
        .thenThrow(Status.NOT_FOUND.withDescription("User not found").asRuntimeException());

    var result = mockMvc.perform(get("/api/user")).andReturn();

    assertThat(result.getResponse().getStatus()).as("HTTP status").isEqualTo(404);
    assertThat(objectMapper.readTree(result.getResponse().getContentAsString()).path("error").asText())
        .as("error description").isEqualTo("User not found");
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidRequests")
  void updateUser_invalidBody_returnsBadRequestWithoutCallingService(
      String scenario, UpdateUserInfoRequest request, String invalidField) throws Exception {
    var result = mockMvc.perform(patch("/api/user").contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))).andReturn();

    assertThat(result.getResponse().getStatus()).as("HTTP status: %s", scenario).isEqualTo(400);
    assertThat(result.getResolvedException()).as("validation exception")
        .isInstanceOf(MethodArgumentNotValidException.class);
    var exception = (MethodArgumentNotValidException) result.getResolvedException();
    assertThat(exception.getBindingResult().getFieldErrors()).as("invalid fields")
        .extracting(error -> error.getField()).containsExactly(invalidField);
    verifyNoInteractions(userdataClient);
  }

  @Test
  void updateUser_avatarLongerThan255Characters_returnsUpdatedUser() throws Exception {
    String avatar = "data:image/png;base64," + "a".repeat(256);
    var request = new UpdateUserInfoRequest("Jean", "Monet", avatar);
    var expected = new UserInfoResponse(user.id(), "user", "Jean", "Monet", avatar);
    when(userdataClient.updateUser("user", request)).thenReturn(expected);

    var result = mockMvc.perform(patch("/api/user").contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))).andReturn();

    assertThat(result.getResponse().getStatus()).as("HTTP status").isEqualTo(200);
    assertThat(objectMapper.readValue(result.getResponse().getContentAsString(), UserInfoResponse.class))
        .as("updated user with long avatar").isEqualTo(expected);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("optionalFieldRequests")
  void updateUser_optionalFields_returnsUpdatedUser(String scenario, UpdateUserInfoRequest request) throws Exception {
    var expected = new UserInfoResponse(user.id(), "user", request.firstname(), request.lastname(), request.avatar());
    when(userdataClient.updateUser("user", request)).thenReturn(expected);

    var result = mockMvc.perform(patch("/api/user").contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))).andReturn();

    assertThat(result.getResponse().getStatus()).as("HTTP status: %s", scenario).isEqualTo(200);
    assertThat(objectMapper.readValue(result.getResponse().getContentAsString(), UserInfoResponse.class))
        .as("updated user: %s", scenario).isEqualTo(expected);
    verify(userdataClient).updateUser("user", request);
  }

  private static Stream<Arguments> invalidRequests() {
    return Stream.of(
        Arguments.of("long firstname", new UpdateUserInfoRequest("a".repeat(256), "Monet", "image"), "firstname"),
        Arguments.of("long lastname", new UpdateUserInfoRequest("Jean", "a".repeat(256), "image"), "lastname")
    );
  }

  private static Stream<Arguments> optionalFieldRequests() {
    return Stream.of(
        Arguments.of("null firstname", new UpdateUserInfoRequest(null, "Monet", "image")),
        Arguments.of("empty firstname", new UpdateUserInfoRequest("", "Monet", "image")),
        Arguments.of("blank firstname", new UpdateUserInfoRequest(" ", "Monet", "image")),
        Arguments.of("null lastname", new UpdateUserInfoRequest("Jean", null, "image")),
        Arguments.of("empty lastname", new UpdateUserInfoRequest("Jean", "", "image")),
        Arguments.of("blank lastname", new UpdateUserInfoRequest("Jean", " ", "image")),
        Arguments.of("null avatar", new UpdateUserInfoRequest("Jean", "Monet", null)),
        Arguments.of("empty avatar", new UpdateUserInfoRequest("Jean", "Monet", "")),
        Arguments.of("blank avatar", new UpdateUserInfoRequest("Jean", "Monet", " "))
    );
  }
}
