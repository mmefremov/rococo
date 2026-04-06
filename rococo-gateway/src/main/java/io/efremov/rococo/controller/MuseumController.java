package io.efremov.rococo.controller;

import io.efremov.rococo.model.CreateMuseumInfoRequest;
import io.efremov.rococo.model.MuseumInfoResponse;
import io.efremov.rococo.model.PageResponse;
import io.efremov.rococo.model.UpdateMuseumInfoRequest;
import io.efremov.rococo.service.GrpcMuseumClient;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/museum")
@RequiredArgsConstructor
public class MuseumController {

  private final GrpcMuseumClient museumClient;

  @GetMapping
  public PageResponse<MuseumInfoResponse> getAllMuseums(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "9") int size,
      @RequestParam(required = false) String title) {
    log.info("GET /api/museum page={}, size={}, title={}", page, size, title);
    return museumClient.getAllMuseums(page, size, title);
  }

  @GetMapping("/{id}")
  public MuseumInfoResponse getMuseumById(@PathVariable UUID id) {
    log.info("GET /api/museum/{}", id);
    return museumClient.getMuseumById(id);
  }

  @PostMapping
  public MuseumInfoResponse createMuseum(@Valid @RequestBody CreateMuseumInfoRequest request) {
    log.info("POST /api/museum title={}", request.title());
    return museumClient.createMuseum(request);
  }

  @PatchMapping
  public MuseumInfoResponse updateMuseum(@Valid @RequestBody UpdateMuseumInfoRequest request) {
    log.info("PATCH /api/museum id={}", request.id());
    return museumClient.updateMuseum(request);
  }
}
