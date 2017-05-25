package com.visualthreat.api;

import com.visualthreat.api.v1.Token;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Token test")
public class TokenTest {
  @DisplayName("Extract token from JSON")
  @Test
  public void extractTokenFromJson() {
    final String tokenAsJson = "{\"access_token\":\"Desired token\"}";
    final Token token = Token.parse(tokenAsJson);

    assertNotNull(token, "Token parse error");
  }

  @DisplayName("Get secret from token")
  @Test
  public void getTokenSecret() {
    final String accessToken = "desired token";
    final Token token = new Token(accessToken);
    final String resultSecret = token.getSecret();
    final String expectedSecret = String.format("Bearer %s", accessToken);

    assertEquals(resultSecret, expectedSecret, "Secret extraction error");
  }
}
