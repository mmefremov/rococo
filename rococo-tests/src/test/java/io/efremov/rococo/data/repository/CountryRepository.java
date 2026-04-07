package io.efremov.rococo.data.repository;

import io.efremov.rococo.data.Database;
import io.efremov.rococo.data.entity.CountryEntity;
import io.qameta.allure.Step;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;

public class CountryRepository {

  @Step("Find country entity by id")
  public CountryEntity findById(UUID id) {
    try (EntityManager manager = Database.GEO.getManager()) {
      return manager.find(CountryEntity.class, id);
    }
  }

  @Step("Find any country entity")
  public CountryEntity findAny() {
    try (EntityManager manager = Database.GEO.getManager()) {
      String query = "FROM CountryEntity ORDER BY RANDOM()";
      return manager.createQuery(query, CountryEntity.class)
          .setMaxResults(1)
          .getSingleResult();
    }
  }

  @Step("Find all countries entities")
  public List<CountryEntity> finAll(int page, int size) {
    try (EntityManager manager = Database.GEO.getManager()) {
      String query = "FROM CountryEntity ORDER BY name";
      return manager.createQuery(query, CountryEntity.class)
          .setFirstResult(page * size)
          .setMaxResults(size)
          .getResultList();
    }
  }

  @Step("Count countries entities")
  public long countAll() {
    try (EntityManager manager = Database.GEO.getManager()) {
      String query = "SELECT COUNT (*) FROM CountryEntity";
      return manager.createQuery(query, Long.class)
          .getSingleResult();
    }
  }
}
