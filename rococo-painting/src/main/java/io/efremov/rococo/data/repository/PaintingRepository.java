package io.efremov.rococo.data.repository;

import io.efremov.rococo.data.PaintingEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaintingRepository extends JpaRepository<PaintingEntity, UUID> {

  Page<PaintingEntity> findAllByTitleContainingIgnoreCase(String title, Pageable pageable);

  Page<PaintingEntity> findAllByArtistId(UUID artistId, Pageable pageable);

  boolean existsByTitleIgnoreCaseAndArtistId(String title, UUID artistId);

  boolean existsByTitleIgnoreCaseAndArtistIdAndIdNot(String title, UUID artistId, UUID id);
}
