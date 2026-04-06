package io.efremov.rococo.service;

import io.efremov.rococo.data.CountryEntity;
import io.efremov.rococo.data.repository.CountryRepository;
import io.efremov.rococo.exception.CountryNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CountryService {

  private final CountryRepository countryRepository;

  @Cacheable(value = "countries", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
  @Transactional(readOnly = true)
  public @NonNull Page<CountryEntity> findAll(@NonNull Pageable pageable) {
    log.debug("Fetching countries, page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
    return countryRepository.findAll(pageable);
  }

  @Cacheable(value = "countryById", key = "#id")
  @Transactional(readOnly = true)
  public @NonNull CountryEntity findById(@NonNull UUID id) {
    log.debug("Fetching country by id: {}", id);
    return countryRepository.findById(id)
        .orElseThrow(() -> new CountryNotFoundException(id));
  }

  @Transactional(readOnly = true)
  public @NonNull List<CountryEntity> findAllByIds(@NonNull Collection<UUID> ids) {
    if (ids.isEmpty()) {
      return List.of();
    }
    log.debug("Fetching countries by ids, count={}", ids.size());
    return countryRepository.findAllById(ids);
  }
}
