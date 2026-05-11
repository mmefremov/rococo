package io.efremov.rococo.common.util;

import java.nio.charset.StandardCharsets;
import org.jspecify.annotations.NonNull;

public final class ByteStringCodec {

  private ByteStringCodec() {}

  public static byte @NonNull [] encode(@NonNull String value) {
    return value.getBytes(StandardCharsets.UTF_8);
  }

  public static @NonNull String decode(byte @NonNull [] bytes) {
    return new String(bytes, StandardCharsets.UTF_8);
  }
}
