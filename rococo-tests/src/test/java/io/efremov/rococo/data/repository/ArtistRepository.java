package io.efremov.rococo.data.repository;

import io.efremov.rococo.data.Database;
import io.efremov.rococo.data.entity.ArtistEntity;
import io.qameta.allure.Step;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.List;
import java.util.UUID;

public class ArtistRepository {

  @Step("Find artist entity by id")
  public ArtistEntity findById(UUID id) {
    try (EntityManager manager = Database.ARTIST.getManager()) {
      return manager.find(ArtistEntity.class, id);
    }
  }

  @Step("Find any artist entity")
  public ArtistEntity findAny() {
    try (EntityManager manager = Database.ARTIST.getManager()) {
      String query = "FROM ArtistEntity ORDER BY RANDOM()";
      return manager.createQuery(query, ArtistEntity.class)
          .setMaxResults(1)
          .getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  @Step("Find artist entities list by name")
  public List<ArtistEntity> finAllByName(int page, int size, String name) {
    try (EntityManager manager = Database.ARTIST.getManager()) {
      String query = """
          FROM ArtistEntity
          WHERE STR(:name) IS NULL OR name ILIKE CONCAT('%', STR(:name), '%')
          ORDER BY name
          """;
      return manager.createQuery(query, ArtistEntity.class)
          .setFirstResult(page * size)
          .setMaxResults(size)
          .setParameter("name", name)
          .getResultList();
    }
  }

  @Step("Count artist entities by name")
  public long countAllByName(String name) {
    try (EntityManager manager = Database.ARTIST.getManager()) {
      String query = """
          SELECT COUNT (*)
          FROM ArtistEntity
          WHERE STR(:name) IS NULL OR name ILIKE CONCAT('%', STR(:name), '%')
          """;
      return manager.createQuery(query, Long.class)
          .setParameter("name", name)
          .getSingleResult();
    }
  }
}
