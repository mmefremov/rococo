package io.efremov.rococo.controller;

import io.efremov.rococo.model.ArtistInfoResponse;
import io.efremov.rococo.model.CreateArtistInfoRequest;
import io.efremov.rococo.model.PageResponse;
import io.efremov.rococo.model.UpdateArtistInfoRequest;
import io.efremov.rococo.service.GrpcArtistClient;
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
@RequestMapping("/api/artist")
@RequiredArgsConstructor
public class ArtistController {

  private final GrpcArtistClient artistClient;

  @GetMapping
  public PageResponse<ArtistInfoResponse> getAllArtists(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String name) {
    log.info("GET /api/artist page={}, size={}, name={}", page, size, name);
    return artistClient.getAllArtists(page, size, name);
  }

  @GetMapping("/{id}")
  public ArtistInfoResponse getArtistById(@PathVariable UUID id) {
    log.info("GET /api/artist/{}", id);
    return artistClient.getArtistById(id);
  }

  @PostMapping
  public ArtistInfoResponse createArtist(@Valid @RequestBody CreateArtistInfoRequest request) {
    log.info("POST /api/artist name={}", request.name());
    return artistClient.createArtist(request);
  }

  @PatchMapping
  public ArtistInfoResponse updateArtist(@Valid @RequestBody UpdateArtistInfoRequest request) {
    log.info("PATCH /api/artist id={}", request.id());
    return artistClient.updateArtist(request);
  }
}
