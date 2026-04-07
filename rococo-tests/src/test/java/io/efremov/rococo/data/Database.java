package io.efremov.rococo.data;

import io.efremov.rococo.config.Config;
import io.efremov.rococo.data.entity.ArtistEntity;
import io.efremov.rococo.data.entity.CountryEntity;
import io.efremov.rococo.data.entity.MuseumEntity;
import io.efremov.rococo.data.entity.PaintingEntity;
import io.efremov.rococo.data.entity.UserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;

public enum Database {

  ARTIST("rococo_artist", ArtistEntity.class),
  MUSEUM("rococo_museum", MuseumEntity.class),
  PAINTING("rococo_painting", PaintingEntity.class),
  USERDATA("rococo_userdata", UserEntity.class),
  GEO("rococo_geo", CountryEntity.class);

  private static final Config CONFIG = Config.getInstance();
  private static final String URL_TEMPLATE =
      "jdbc:p6spy:postgresql://" + CONFIG.dbHost() + ":" + CONFIG.dbPort() + "/%s";
  private static final Map<Database, EntityManagerFactory> FACTORIES = new ConcurrentHashMap<>();

  private final String dbName;
  private final Class<?>[] entityClasses;

  Database(String dbName, Class<?>... entityClasses) {
    this.dbName = dbName;
    this.entityClasses = entityClasses;
  }

  public EntityManager getManager() {
    return FACTORIES.computeIfAbsent(this, Database::buildFactory)
        .createEntityManager();
  }

  private EntityManagerFactory buildFactory() {
    Configuration config = new Configuration();
    config.setProperty(AvailableSettings.JAKARTA_JDBC_URL, URL_TEMPLATE.formatted(dbName));
    config.setProperty(AvailableSettings.JAKARTA_JDBC_USER, "postgres");
    config.setProperty(AvailableSettings.JAKARTA_JDBC_PASSWORD, "secret");
    config.setProperty(AvailableSettings.JAKARTA_JDBC_DRIVER, "com.p6spy.engine.spy.P6SpyDriver");
    config.setProperty(AvailableSettings.HBM2DDL_AUTO, "none");
    config.setProperty(AvailableSettings.SHOW_SQL, "false");
    Arrays.stream(entityClasses).forEach(config::addAnnotatedClass);
    return config.buildSessionFactory();
  }

  public static void closeAllFactories() {
    FACTORIES.values().forEach(EntityManagerFactory::close);
  }
}
