package io.efremov.rococo.controller;

import io.efremov.rococo.model.CountryInfoResponse;
import io.efremov.rococo.model.PageResponse;
import io.efremov.rococo.service.GrpcGeoClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/country")
@RequiredArgsConstructor
public class CountryController {

  private final GrpcGeoClient geoClient;

  @GetMapping
  public PageResponse<CountryInfoResponse> getAllCountries(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    log.info("GET /api/country page={}, size={}", page, size);
    return geoClient.getAllCountries(page, size);
  }
}
