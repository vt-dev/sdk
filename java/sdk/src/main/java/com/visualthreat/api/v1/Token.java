package com.visualthreat.api.v1;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.visualthreat.api.v1.Utils.decodeJson;

@Value
@Slf4j
public class Token {
  private final String secret;

  public static Token parse(final String data) {
    final Map decoded = decodeJson(data, Map.class);
    if (decoded != null) {
      final String token = decoded.get("access_token").toString();
      return new Token(token);
    }

    return null;
  }

  public String getSecret() {
    return "Bearer " + secret;
  }
}
