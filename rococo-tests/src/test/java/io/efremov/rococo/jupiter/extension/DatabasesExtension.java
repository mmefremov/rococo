package io.efremov.rococo.jupiter.extension;

import io.efremov.rococo.data.Database;

public class DatabasesExtension implements SuiteExtension {

  @Override
  public void afterSuite() {
    Database.closeAllFactories();
  }
}
