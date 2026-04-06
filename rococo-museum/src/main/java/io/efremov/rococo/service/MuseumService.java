package io.efremov.rococo.service;

import io.efremov.rococo.data.MuseumEntity;
import io.efremov.rococo.data.repository.MuseumRepository;
import io.efremov.rococo.exception.MuseumNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MuseumService {

  private final MuseumRepository museumRepository;

  @Transactional(readOnly = true)
  public @NonNull Page<MuseumEntity> findAll(@Nullable String title, @NonNull Pageable pageable) {
    if (title != null && !title.isBlank()) {
      log.debug("Searching museums by title: {}", title);
      return museumRepository.findAllByTitleContainingIgnoreCase(title, pageable);
    }
    log.debug("Fetching all museums, page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
    return museumRepository.findAll(pageable);
  }

  @Transactional
  public @NonNull MuseumEntity create(@NonNull String title, @NonNull String description,
      @NonNull String photoBase64, @NonNull String city, @NonNull UUID countryId) {
    validateMuseumFields(title, description, photoBase64, city, countryId);
    if (museumRepository.existsByTitleIgnoreCaseAndCityIgnoreCase(title, city)) {
      log.warn("Museum with title '{}' in city '{}' already exists", title, city);
      throw new DataIntegrityViolationException(
          "Museum with title '" + title + "' in city '" + city + "' already exists");
    }
    log.info("Creating museum: {}", title);
    MuseumEntity entity = new MuseumEntity();
    entity.setTitle(title);
    entity.setDescription(description);
    entity.setCity(city);
    entity.setCountryId(countryId);
    entity.setPhoto(photoBase64.getBytes(StandardCharsets.UTF_8));
    return museumRepository.save(entity);
  }

  @Transactional
  public @NonNull MuseumEntity update(@NonNull UUID id, @NonNull String title,
      @NonNull String description, @NonNull String photoBase64, @NonNull String city, @NonNull UUID countryId) {
    MuseumEntity entity = findById(id);
    validateMuseumFields(title, description, photoBase64, city, countryId);
    if (museumRepository.existsByTitleIgnoreCaseAndCityIgnoreCaseAndIdNot(title, city, id)) {
      log.warn("Museum with title '{}' in city '{}' already exists", title, city);
      throw new DataIntegrityViolationException(
          "Museum with title '" + title + "' in city '" + city + "' already exists");
    }
    log.info("Updating museum id: {}", id);
    entity.setTitle(title);
    entity.setDescription(description);
    entity.setCity(city);
    entity.setCountryId(countryId);
    entity.setPhoto(photoBase64.getBytes(StandardCharsets.UTF_8));
    return museumRepository.save(entity);
  }

  @Transactional(readOnly = true)
  public @NonNull MuseumEntity findById(@NonNull UUID id) {
    log.debug("Fetching museum by id: {}", id);
    return museumRepository.findById(id)
        .orElseThrow(() -> new MuseumNotFoundException(id));
  }

  @Transactional(readOnly = true)
  public @NonNull List<MuseumEntity> findAllByIds(@NonNull Collection<UUID> ids) {
    if (ids.isEmpty()) {
      return List.of();
    }
    log.debug("Fetching museums by ids, count={}", ids.size());
    return museumRepository.findAllById(ids);
  }

  private static void validateMuseumFields(String title, String description, String photoBase64, String city,
      UUID countryId) {
    if (title == null || title.trim().isBlank()) {
      throw new IllegalArgumentException("title: must not be blank");
    }
    if (title.length() < 3 || title.length() > 255) {
      throw new IllegalArgumentException("title: must be from 3 to 255 characters");
    }
    if (description == null || description.trim().isBlank()) {
      throw new IllegalArgumentException("description: must not be blank");
    }
    if (description.length() < 10 || description.length() > 1000) {
      throw new IllegalArgumentException("description: must be from 10 to 1000 characters");
    }
    if (city == null || city.trim().isBlank()) {
      throw new IllegalArgumentException("city: must not be blank");
    }
    if (city.length() < 3 || city.length() > 255) {
      throw new IllegalArgumentException("city: must be from 3 to 255 characters");
    }
    if (photoBase64 == null || photoBase64.trim().isBlank()) {
      throw new IllegalArgumentException("photo: must not be blank");
    }
    if (photoBase64.length() > 1_048_576) {
      throw new IllegalArgumentException("photo: must not exceed 1 MB");
    }
    if (countryId == null) {
      throw new IllegalArgumentException("countryId: must not be null");
    }
  }
}
