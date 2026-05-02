package io.efremov.rococo.config;


import org.jspecify.annotations.NonNull;

public interface Config {

  @NonNull
  static Config getInstance() {
    return "docker".equals(System.getProperty("test.env"))
        ? DockerConfig.INSTANCE
        : LocalConfig.INSTANCE;
  }

  @NonNull
  String frontUrl();

  @NonNull
  String authUrl();

  @NonNull
  String artistUrl();

  @NonNull
  String gatewayUrl();

  @NonNull
  String geoUrl();

  @NonNull
  String museumUrl();

  @NonNull
  String paintingUrl();

  @NonNull
  String userdataUrl();

  @NonNull
  String dbHost();

  int dbPort();

  default int artistPort() {
    return 8096;
  }

  default int geoPort() {
    return 8094;
  }

  default int museumPort() {
    return 8098;
  }

  default int paintingPort() {
    return 8100;
  }

  default int userdataPort() {
    return 8092;
  }
}
