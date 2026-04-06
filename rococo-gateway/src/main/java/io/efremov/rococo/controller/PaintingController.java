package io.efremov.rococo.controller;

import io.efremov.rococo.model.CreatePaintingInfoRequest;
import io.efremov.rococo.model.PageResponse;
import io.efremov.rococo.model.PaintingInfoResponse;
import io.efremov.rococo.model.UpdatePaintingInfoRequest;
import io.efremov.rococo.service.GrpcPaintingClient;
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
@RequestMapping("/api/painting")
@RequiredArgsConstructor
public class PaintingController {

  private final GrpcPaintingClient paintingClient;

  @GetMapping
  public PageResponse<PaintingInfoResponse> getAllPaintings(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "9") int size,
      @RequestParam(required = false) String title) {
    log.info("GET /api/painting page={}, size={}, title={}", page, size, title);
    return paintingClient.getAllPaintings(page, size, title);
  }

  @GetMapping("/{id}")
  public PaintingInfoResponse getPaintingById(@PathVariable UUID id) {
    log.info("GET /api/painting/{}", id);
    return paintingClient.getPaintingById(id);
  }

  @GetMapping("/author/{artistId}")
  public PageResponse<PaintingInfoResponse> getPaintingsByArtist(
      @PathVariable UUID artistId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "9") int size) {
    log.info("GET /api/painting/author/{} page={}, size={}", artistId, page, size);
    return paintingClient.getPaintingsByArtist(artistId, page, size);
  }

  @PostMapping
  public PaintingInfoResponse createPainting(@Valid @RequestBody CreatePaintingInfoRequest request) {
    log.info("POST /api/painting title={}", request.title());
    return paintingClient.createPainting(request);
  }

  @PatchMapping
  public PaintingInfoResponse updatePainting(@Valid @RequestBody UpdatePaintingInfoRequest request) {
    log.info("PATCH /api/painting id={}", request.id());
    return paintingClient.updatePainting(request);
  }
}
