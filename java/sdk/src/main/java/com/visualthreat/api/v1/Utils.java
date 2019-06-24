package com.visualthreat.api.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public final class Utils {
  public static final ObjectMapper json = new ObjectMapper();

  static {
    json.findAndRegisterModules();
  }

  public static <T> T decodeJson(final String data, final Class<T> classTag) {
    try {
      return json.readValue(data, classTag);
    } catch (final IOException e) {
      log.warn("Can't decode JSON:\n" + data, e);
      return null;
    }
  }

  public static String encodeJson(final Object obj) {
    try {
      return json.writeValueAsString(obj);
    } catch (final JsonProcessingException e) {
      log.warn("Can't encode to JSON:\n" + obj.toString(), e);
      return null;
    }
  }
}
