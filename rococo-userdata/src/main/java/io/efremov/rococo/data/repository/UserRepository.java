package io.efremov.rococo.data.repository;

import io.efremov.rococo.data.UserEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

  Optional<UserEntity> findByUserName(String userName);

  boolean existsByUserName(String userName);
}
