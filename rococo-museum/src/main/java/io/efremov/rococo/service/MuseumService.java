package io.efremov.rococo.service;

import io.efremov.rococo.common.util.ByteStringCodec;
import io.efremov.rococo.common.validation.FieldValidator;
import io.efremov.rococo.data.MuseumEntity;
import io.efremov.rococo.data.repository.MuseumRepository;
import io.efremov.rococo.common.exception.EntityNotFoundException;
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
      throw new DataIntegrityViolationException("Museum title already taken: " + title);
    }
    log.info("Creating museum: {}", title);
    MuseumEntity entity = new MuseumEntity();
    entity.setTitle(title);
    entity.setDescription(description);
    entity.setCity(city);
    entity.setCountryId(countryId);
    entity.setPhoto(ByteStringCodec.encode(photoBase64));
    return museumRepository.save(entity);
  }

  @Transactional
  public @NonNull MuseumEntity update(@NonNull UUID id, @NonNull String title,
      @NonNull String description, @NonNull String photoBase64, @NonNull String city, @NonNull UUID countryId) {
    MuseumEntity entity = museumRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Museum", id));
    validateMuseumFields(title, description, photoBase64, city, countryId);
    if (museumRepository.existsByTitleIgnoreCaseAndCityIgnoreCaseAndIdNot(title, city, id)) {
      log.warn("Museum with title '{}' in city '{}' already exists", title, city);
      throw new DataIntegrityViolationException("Museum title already taken: " + title);
    }
    log.info("Updating museum id: {}", id);
    entity.setTitle(title);
    entity.setDescription(description);
    entity.setCity(city);
    entity.setCountryId(countryId);
    entity.setPhoto(ByteStringCodec.encode(photoBase64));
    return museumRepository.save(entity);
  }

  @Transactional(readOnly = true)
  public @NonNull MuseumEntity findById(@NonNull UUID id) {
    log.debug("Fetching museum by id: {}", id);
    return museumRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Museum", id));
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
    FieldValidator.requireNotBlank(title, "title");
    FieldValidator.requireLength(title, "title", 3, 255);
    FieldValidator.requireNotBlank(description, "description");
    FieldValidator.requireLength(description, "description", 10, 1000);
    FieldValidator.requireNotBlank(city, "city");
    FieldValidator.requireLength(city, "city", 3, 255);
    FieldValidator.requireNotBlank(photoBase64, "photo");
    FieldValidator.requireMaxByteSize(photoBase64, "photo", 1_048_576);
    FieldValidator.requireNotNull(countryId, "countryId");
  }
}
