package io.efremov.rococo.service;

import io.efremov.rococo.common.util.ByteStringCodec;
import io.efremov.rococo.common.validation.FieldValidator;
import io.efremov.rococo.data.ArtistEntity;
import io.efremov.rococo.data.repository.ArtistRepository;
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
    FieldValidator.requireNotBlank(name, "name");
    FieldValidator.requireLength(name, "name", 3, 255);
    FieldValidator.requireNotBlank(biography, "biography");
    FieldValidator.requireLength(biography, "biography", 10, 2000);
    FieldValidator.requireNotBlank(photoBase64, "photo");
    FieldValidator.requireMaxByteSize(photoBase64, "photo", 1_048_576);
  }

  @Transactional
  public @NonNull ArtistEntity create(@NonNull String name, @NonNull String biography, @NonNull String photoBase64) {
    validateArtistFields(name, biography, photoBase64);
    if (artistRepository.existsByNameIgnoreCase(name)) {
      log.warn("Artist with name '{}' already exists", name);
      throw new DataIntegrityViolationException("Artist name already taken: " + name);
    }
    log.info("Creating artist: {}", name);
    ArtistEntity entity = new ArtistEntity();
    entity.setName(name);
    entity.setBiography(biography);
    entity.setPhoto(ByteStringCodec.encode(photoBase64));
    return artistRepository.save(entity);
  }

  @Transactional
  public @NonNull ArtistEntity update(@NonNull UUID id, @NonNull String name,
      @NonNull String biography, @NonNull String photoBase64) {
    ArtistEntity entity = artistRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Artist", id));
    validateArtistFields(name, biography, photoBase64);
    if (artistRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
      log.warn("Artist with name '{}' already exists", name);
      throw new DataIntegrityViolationException("Artist name already taken: " + name);
    }
    log.info("Updating artist id: {}", id);
    entity.setName(name);
    entity.setBiography(biography);
    entity.setPhoto(ByteStringCodec.encode(photoBase64));
    return artistRepository.save(entity);
  }

  @Transactional(readOnly = true)
  public @NonNull List<ArtistEntity> findAllByIds(@NonNull Collection<UUID> ids) {
    log.debug("Fetching artists by ids, count={}", ids.size());
    if (ids.isEmpty()) return List.of();
    return artistRepository.findAllById(ids);
  }

  @Transactional(readOnly = true)
  public @NonNull ArtistEntity findById(@NonNull UUID id) {
    log.debug("Fetching artist by id: {}", id);
    return artistRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Artist", id));
  }
}
