package io.efremov.rococo.data.repository;

import io.efremov.rococo.data.Database;
import io.efremov.rococo.data.entity.UserEntity;
import io.qameta.allure.Step;
import jakarta.persistence.EntityManager;

public class UserRepository {

  @Step("Find user entity by username")
  public UserEntity findByUsername(String username) {
    try (EntityManager manager = Database.USERDATA.getManager()) {
      return manager.createQuery(
              "SELECT u FROM UserEntity u WHERE u.userName = :username", UserEntity.class)
          .setParameter("username", username)
          .getSingleResult();
    }
  }
}
