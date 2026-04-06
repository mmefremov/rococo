package io.efremov.rococo.service;

import io.efremov.rococo.data.PaintingEntity;
import io.efremov.rococo.data.repository.PaintingRepository;
import io.efremov.rococo.exception.PaintingNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
public class PaintingService {

  private final PaintingRepository paintingRepository;

  public static @NonNull String encodeContent(byte[] content) {
    if (content == null || content.length == 0) {
      return "";
    }
    return Base64.getEncoder().encodeToString(content);
  }

  @Transactional(readOnly = true)
  public @NonNull Page<PaintingEntity> findAll(@Nullable String title, @NonNull Pageable pageable) {
    if (title != null && !title.isBlank()) {
      log.debug("Searching paintings by title: {}", title);
      return paintingRepository.findAllByTitleContainingIgnoreCase(title, pageable);
    }
    log.debug("Fetching all paintings, page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
    return paintingRepository.findAll(pageable);
  }

  @Transactional(readOnly = true)
  public @NonNull Page<PaintingEntity> findByArtistId(@NonNull UUID artistId, @NonNull Pageable pageable) {
    log.debug("Fetching paintings by artist id: {}", artistId);
    return paintingRepository.findAllByArtistId(artistId, pageable);
  }

  @Transactional
  public @NonNull PaintingEntity create(@NonNull String title, @NonNull String description,
      @NonNull String contentBase64, @NonNull UUID artistId, @Nullable UUID museumId) {
    validatePaintingFields(title, description, contentBase64, artistId);
    if (paintingRepository.existsByTitleIgnoreCaseAndArtistId(title, artistId)) {
      log.warn("Painting with title '{}' for artist '{}' already exists", title, artistId);
      throw new DataIntegrityViolationException("Painting with title '" + title + "' for artist already exists");
    }
    log.info("Creating painting: {}", title);
    var entity = new PaintingEntity();
    entity.setTitle(title);
    entity.setDescription(description);
    entity.setArtistId(artistId);
    entity.setMuseumId(museumId);
    entity.setContent(contentBase64.getBytes(StandardCharsets.UTF_8));
    return paintingRepository.save(entity);
  }

  @Transactional
  public @NonNull PaintingEntity update(@NonNull UUID id, @NonNull String title, @NonNull String description,
      @NonNull String contentBase64, @NonNull UUID artistId, @Nullable UUID museumId) {
    PaintingEntity entity = findById(id);
    validatePaintingFields(title, description, contentBase64, artistId);
    if (paintingRepository.existsByTitleIgnoreCaseAndArtistIdAndIdNot(title, artistId, id)) {
      log.warn("Painting with title '{}' for artist '{}' already exists", title, artistId);
      throw new DataIntegrityViolationException("Painting with title '" + title + "' for artist already exists");
    }
    log.info("Updating painting id: {}", id);
    entity.setTitle(title);
    entity.setDescription(description);
    entity.setArtistId(artistId);
    entity.setMuseumId(museumId);
    entity.setContent(contentBase64.getBytes(StandardCharsets.UTF_8));
    return paintingRepository.save(entity);
  }

  @Transactional(readOnly = true)
  public @NonNull PaintingEntity findById(@NonNull UUID id) {
    log.debug("Fetching painting by id: {}", id);
    return paintingRepository.findById(id)
        .orElseThrow(() -> new PaintingNotFoundException(id));
  }

  @Transactional(readOnly = true)
  public @NonNull List<PaintingEntity> findAllByIds(@NonNull Collection<UUID> ids) {
    if (ids.isEmpty()) {
      return List.of();
    }
    log.debug("Fetching paintings by ids, count={}", ids.size());
    return paintingRepository.findAllById(ids);
  }

  private static void validatePaintingFields(String title, String description, String contentBase64, UUID artistId) {
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
    if (contentBase64 == null || contentBase64.trim().isBlank()) {
      throw new IllegalArgumentException("content: must not be blank");
    }
    if (contentBase64.length() > 1_048_576) {
      throw new IllegalArgumentException("content: must not exceed 1 MB");
    }
    if (artistId == null) {
      throw new IllegalArgumentException("artistId: must not be null");
    }
  }
}
