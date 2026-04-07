package io.efremov.rococo.data.core;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

public class ByteArrayLengthSerializer extends JsonSerializer<byte[]> {

  @Override
  public void serialize(byte[] value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeString(value.length + " bytes");
  }
}
