package io.efremov.rococo.config;

import org.jspecify.annotations.NonNull;

enum DockerConfig implements Config {
  INSTANCE;

  @Override
  public @NonNull String frontUrl() {
    return "http://rococo-client/";
  }

  @Override
  public @NonNull String authUrl() {
    return "http://rococo-auth:9000/";
  }

  @Override
  public @NonNull String artistUrl() {
    return "rococo-artist";
  }

  @Override
  public @NonNull String gatewayUrl() {
    return "http://rococo-gateway:8080/";
  }

  @Override
  public @NonNull String geoUrl() {
    return "rococo-geo";
  }

  @Override
  public @NonNull String museumUrl() {
    return "rococo-museum";
  }

  @Override
  public @NonNull String paintingUrl() {
    return "rococo-painting";
  }

  @Override
  public @NonNull String userdataUrl() {
    return "rococo-userdata";
  }

  @Override
  public @NonNull String dbHost() {
    return "rococo-postgres";
  }

  @Override
  public int dbPort() {
    return 5432;
  }
}
