package io.efremov.rococo.data.repository;

import io.efremov.rococo.data.CountryEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<CountryEntity, UUID> {

  Page<CountryEntity> findAll(Pageable pageable);
}
