package io.efremov.rococo.jupiter.extension;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class TestMethodContextExtension implements BeforeEachCallback, AfterEachCallback {

  private static final ThreadLocal<ExtensionContext> ctxStore = new ThreadLocal<>();

  public static ExtensionContext context() {
    return ctxStore.get();
  }

  @Override
  public void beforeEach(ExtensionContext context) {
    ctxStore.set(context);
  }

  @Override
  public void afterEach(ExtensionContext context) {
    ctxStore.remove();
  }
}
