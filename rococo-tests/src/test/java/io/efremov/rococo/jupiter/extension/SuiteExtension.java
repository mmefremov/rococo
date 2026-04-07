package io.efremov.rococo.jupiter.extension;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public interface SuiteExtension extends BeforeAllCallback {

  @Override
  default void beforeAll(ExtensionContext context) {
    final ExtensionContext rootContext = context.getRoot();
    rootContext.getStore(ExtensionContext.Namespace.GLOBAL)
        .getOrComputeIfAbsent(this.getClass(), key -> {
              beforeSuite(rootContext);
              return (AutoCloseable) this::afterSuite;
            }
        );
  }

  default void beforeSuite(ExtensionContext context) {
  }

  default void afterSuite() {
  }
}
