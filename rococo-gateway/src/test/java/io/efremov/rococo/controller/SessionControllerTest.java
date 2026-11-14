package io.efremov.rococo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.efremov.rococo.model.SessionResponse;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SessionControllerTest {

  private final SessionController controller = new SessionController();
  private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @Test
  void getSession_jwtAuthentication_returnsUsernameAndTokenDates() throws Exception {
    Instant issuedAt = Instant.parse("2026-01-01T10:00:00Z");
    Instant expiresAt = Instant.parse("2026-01-01T11:00:00Z");
    Jwt jwt = Jwt.withTokenValue("token").header("alg", "RS256").subject("user")
        .issuedAt(issuedAt).expiresAt(expiresAt).build();

    var result = mockMvc.perform(get("/api/session")
        .principal(new JwtAuthenticationToken(jwt, List.of(), "user"))).andReturn();

    assertThat(result.getResponse().getStatus()).as("HTTP status").isEqualTo(200);
    assertThat(objectMapper.readValue(result.getResponse().getContentAsString(), SessionResponse.class))
        .as("JWT session").isEqualTo(new SessionResponse("user", issuedAt, expiresAt));
  }

  @Test
  void getSession_withoutAuthentication_returnsEmptySession() throws Exception {
    var result = mockMvc.perform(get("/api/session")).andReturn();

    assertThat(result.getResponse().getStatus()).as("HTTP status").isEqualTo(200);
    assertThat(objectMapper.readValue(result.getResponse().getContentAsString(), SessionResponse.class)).as(
            "anonymous session")
        .isEqualTo(new SessionResponse(null, null, null));
  }

  @Test
  void getSession_nonJwtAuthentication_returnsEmptySession() throws Exception {
    var authentication = UsernamePasswordAuthenticationToken.authenticated("user", "password", List.of());

    var result = mockMvc.perform(get("/api/session").principal(authentication)).andReturn();

    assertThat(result.getResponse().getStatus()).as("HTTP status").isEqualTo(200);
    assertThat(objectMapper.readValue(result.getResponse().getContentAsString(), SessionResponse.class)).as(
            "non-JWT session")
        .isEqualTo(new SessionResponse(null, null, null));
  }
}
