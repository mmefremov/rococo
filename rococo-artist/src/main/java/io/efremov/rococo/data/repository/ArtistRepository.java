package io.efremov.rococo.data.repository;

import io.efremov.rococo.data.ArtistEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistRepository extends JpaRepository<ArtistEntity, UUID> {

  Page<ArtistEntity> findAllByNameContainingIgnoreCase(String name, Pageable pageable);

  boolean existsByNameIgnoreCase(String name);

  boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
}
