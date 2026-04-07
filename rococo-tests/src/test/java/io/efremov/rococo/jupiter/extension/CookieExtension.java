package io.efremov.rococo.jupiter.extension;

import io.efremov.rococo.api.core.ThreadSafeCookieStore;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class CookieExtension implements AfterTestExecutionCallback {

  @Override
  public void afterTestExecution(ExtensionContext context) {
    ThreadSafeCookieStore.INSTANCE.removeAll();
  }
}
