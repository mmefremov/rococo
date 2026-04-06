package io.efremov.rococo.controller;

import io.efremov.rococo.model.SessionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/session")
public class SessionController {

  @GetMapping
  public SessionResponse getSession(Authentication authentication) {
    log.debug("GET /api/session, authenticated={}", authentication != null && authentication.isAuthenticated());
    if (authentication instanceof JwtAuthenticationToken jwt) {
      return new SessionResponse(
          jwt.getName(),
          jwt.getToken().getIssuedAt(),
          jwt.getToken().getExpiresAt()
      );
    }
    return new SessionResponse(null, null, null);
  }
}
