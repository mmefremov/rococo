package io.efremov.rococo.controller;

import io.efremov.rococo.model.UpdateUserInfoRequest;
import io.efremov.rococo.model.UserInfoResponse;
import io.efremov.rococo.service.GrpcUserdataClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

  private final GrpcUserdataClient userdataClient;

  @GetMapping
  public UserInfoResponse getUser(@AuthenticationPrincipal Jwt jwt) {
    String username = jwt.getSubject();
    log.info("GET /api/user for username={}", username);
    return userdataClient.getUser(username);
  }

  @PatchMapping
  public UserInfoResponse updateUser(@AuthenticationPrincipal Jwt jwt,
      @RequestBody UpdateUserInfoRequest request) {
    String username = jwt.getSubject();
    log.info("PATCH /api/user for username={}", username);
    return userdataClient.updateUser(username, request);
  }
}
