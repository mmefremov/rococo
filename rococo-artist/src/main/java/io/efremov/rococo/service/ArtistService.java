package io.efremov.rococo.service;

import io.efremov.rococo.data.ArtistEntity;
import io.efremov.rococo.data.repository.ArtistRepository;
import io.efremov.rococo.exception.ArtistNotFoundException;
import java.nio.charset.StandardCharsets;
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
public class ArtistService {

  private final ArtistRepository artistRepository;

  @Transactional(readOnly = true)
  public @NonNull Page<ArtistEntity> findAll(@Nullable String name, @NonNull Pageable pageable) {
    if (name != null && !name.isBlank()) {
      log.debug("Searching artists by name: {}", name);
      return artistRepository.findAllByNameContainingIgnoreCase(name, pageable);
    }
    log.debug("Fetching all artists, page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
    return artistRepository.findAll(pageable);
  }

  private static void validateArtistFields(String name, String biography, String photoBase64) {
    if (name == null || name.trim().isBlank()) {
      throw new IllegalArgumentException("name: must not be blank");
    }
    if (name.length() < 3 || name.length() > 255) {
      throw new IllegalArgumentException("name: must be from 3 to 255 characters");
    }
    if (biography == null || biography.trim().isBlank()) {
      throw new IllegalArgumentException("biography: must not be blank");
    }
    if (biography.length() < 10 || biography.length() > 2000) {
      throw new IllegalArgumentException("biography: must be from 10 to 2000 characters");
    }
    if (photoBase64 == null || photoBase64.isBlank()) {
      throw new IllegalArgumentException("photo: must not be blank");
    }
    if (photoBase64.length() > 1_048_576) {
      throw new IllegalArgumentException("photo: must not exceed 1 MB");
    }
  }

  @Transactional
  public @NonNull ArtistEntity create(@NonNull String name, @NonNull String biography, @NonNull String photoBase64) {
    validateArtistFields(name, biography, photoBase64);
    if (artistRepository.existsByNameIgnoreCase(name)) {
      log.warn("Artist with name '{}' already exists", name);
      throw new DataIntegrityViolationException("Artist with name '" + name + "' already exists");
    }
    log.info("Creating artist: {}", name);
    ArtistEntity entity = new ArtistEntity();
    entity.setName(name);
    entity.setBiography(biography);
    entity.setPhoto(photoBase64.getBytes(StandardCharsets.UTF_8));
    return artistRepository.save(entity);
  }

  @Transactional
  public @NonNull ArtistEntity update(@NonNull UUID id, @NonNull String name,
      @NonNull String biography, @NonNull String photoBase64) {
    ArtistEntity entity = findById(id);
    validateArtistFields(name, biography, photoBase64);
    if (artistRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
      log.warn("Artist with name '{}' already exists", name);
      throw new DataIntegrityViolationException("Artist with name '" + name + "' already exists");
    }
    log.info("Updating artist id: {}", id);
    entity.setName(name);
    entity.setBiography(biography);
    entity.setPhoto(photoBase64.getBytes(StandardCharsets.UTF_8));
    return artistRepository.save(entity);
  }

  @Transactional(readOnly = true)
  public @NonNull List<ArtistEntity> findAllByIds(@NonNull List<UUID> ids) {
    log.debug("Fetching artists by ids, count={}", ids.size());
    if (ids.isEmpty()) return List.of();
    return artistRepository.findAllById(ids);
  }

  @Transactional(readOnly = true)
  public @NonNull ArtistEntity findById(@NonNull UUID id) {
    log.debug("Fetching artist by id: {}", id);
    return artistRepository.findById(id)
        .orElseThrow(() -> new ArtistNotFoundException(id));
  }
}
