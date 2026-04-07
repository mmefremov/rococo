package io.efremov.rococo.config;

import org.jspecify.annotations.NonNull;

enum LocalConfig implements Config {
  INSTANCE;

  @Override
  public @NonNull String frontUrl() {
    return "http://localhost:3000/";
  }

  @Override
  public @NonNull String authUrl() {
    return "http://localhost:9000/";
  }

  @Override
  public @NonNull String artistUrl() {
    return "localhost";
  }

  @Override
  public @NonNull String gatewayUrl() {
    return "http://localhost:8080/";
  }

  @Override
  public @NonNull String geoUrl() {
    return "localhost";
  }

  @Override
  public @NonNull String museumUrl() {
    return "localhost";
  }

  @Override
  public @NonNull String paintingUrl() {
    return "localhost";
  }

  @Override
  public @NonNull String userdataUrl() {
    return "localhost";
  }

  @Override
  public @NonNull String dbHost() {
    return "localhost";
  }

  @Override
  public int dbPort() {
    return 6432;
  }
}
