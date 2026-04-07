package io.efremov.rococo.data.repository;

import io.efremov.rococo.data.Database;
import io.efremov.rococo.data.entity.PaintingEntity;
import io.qameta.allure.Step;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.List;
import java.util.UUID;

public class PaintingRepository {

  @Step("Find painting entity by id")
  public PaintingEntity findById(UUID id) {
    try (EntityManager manager = Database.PAINTING.getManager()) {
      return manager.find(PaintingEntity.class, id);
    }
  }

  @Step("Find any painting entity")
  public PaintingEntity findAny() {
    try (EntityManager manager = Database.PAINTING.getManager()) {
      String query = "FROM PaintingEntity ORDER BY RANDOM()";
      return manager.createQuery(query, PaintingEntity.class)
          .setMaxResults(1)
          .getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  @Step("Find painting entities list by title")
  public List<PaintingEntity> finAllByTitle(int page, int size, String title) {
    try (EntityManager manager = Database.PAINTING.getManager()) {
      String query = """
          FROM PaintingEntity
          WHERE STR(:title) IS NULL OR title ILIKE CONCAT('%', STR(:title), '%')
          ORDER BY title
          """;
      return manager.createQuery(query, PaintingEntity.class)
          .setFirstResult(page * size)
          .setMaxResults(size)
          .setParameter("title", title)
          .getResultList();
    }
  }

  @Step("Count painting entities by title")
  public long countAllByTitle(String title) {
    try (EntityManager manager = Database.PAINTING.getManager()) {
      String query = """
          SELECT COUNT (*)
          FROM PaintingEntity
          WHERE STR(:title) IS NULL OR title ILIKE CONCAT('%', STR(:title), '%')
          """;
      return manager.createQuery(query, Long.class)
          .setParameter("title", title)
          .getSingleResult();
    }
  }

  @Step("Find painting entities list by artist id")
  public List<PaintingEntity> finAllByArtistId(int page, int size, UUID artistId) {
    try (EntityManager manager = Database.PAINTING.getManager()) {
      String query = """
          FROM PaintingEntity
          WHERE artistId = :artistId
          ORDER BY title
          """;
      return manager.createQuery(query, PaintingEntity.class)
          .setFirstResult(page * size)
          .setMaxResults(size)
          .setParameter("artistId", artistId)
          .getResultList();
    }
  }

  @Step("Count painting entities by artist id")
  public long countAllByArtistId(UUID artistId) {
    try (EntityManager manager = Database.PAINTING.getManager()) {
      String query = """
          SELECT COUNT (*)
          FROM PaintingEntity
          WHERE artistId = :artistId
          """;
      return manager.createQuery(query, Long.class)
          .setParameter("artistId", artistId)
          .getSingleResult();
    }
  }
}
