package io.efremov.rococo.data.repository;

import io.efremov.rococo.data.Database;
import io.efremov.rococo.data.entity.MuseumEntity;
import io.qameta.allure.Step;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.List;
import java.util.UUID;

public class MuseumRepository {

  @Step("Find museum entity by id")
  public MuseumEntity findById(UUID id) {
    try (EntityManager manager = Database.MUSEUM.getManager()) {
      return manager.find(MuseumEntity.class, id);
    }
  }

  @Step("Find any museum entity")
  public MuseumEntity findAny() {
    try (EntityManager manager = Database.MUSEUM.getManager()) {
      String query = "FROM MuseumEntity ORDER BY RANDOM()";
      return manager.createQuery(query, MuseumEntity.class)
          .setMaxResults(1)
          .getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  @Step("Find museum entities list by title")
  public List<MuseumEntity> finAllByTitle(int page, int size, String title) {
    try (EntityManager manager = Database.MUSEUM.getManager()) {
      String query = """
          FROM MuseumEntity
          WHERE STR(:title) IS NULL OR title ILIKE CONCAT('%', STR(:title), '%')
          ORDER BY title
          """;
      return manager.createQuery(query, MuseumEntity.class)
          .setFirstResult(page * size)
          .setMaxResults(size)
          .setParameter("title", title)
          .getResultList();
    }
  }

  @Step("Count museum entities by title")
  public long countAllByTitle(String title) {
    try (EntityManager manager = Database.MUSEUM.getManager()) {
      String query = """
          SELECT COUNT (*)
          FROM MuseumEntity
          WHERE STR(:title) IS NULL OR title ILIKE CONCAT('%', STR(:title), '%')
          """;
      return manager.createQuery(query, Long.class)
          .setParameter("title", title)
          .getSingleResult();
    }
  }
}
