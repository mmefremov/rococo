package io.efremov.rococo.data.repository;

import io.efremov.rococo.data.MuseumEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MuseumRepository extends JpaRepository<MuseumEntity, UUID> {

  Page<MuseumEntity> findAllByTitleContainingIgnoreCase(String title, Pageable pageable);

  boolean existsByTitleIgnoreCaseAndCityIgnoreCase(String title, String city);

  boolean existsByTitleIgnoreCaseAndCityIgnoreCaseAndIdNot(String title, String city, UUID id);
}
