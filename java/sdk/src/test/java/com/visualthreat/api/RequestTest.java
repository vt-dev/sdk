package com.visualthreat.api;

import com.visualthreat.api.data.Request;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;


@DisplayName("Request test")
public class RequestTest {

  @DisplayName("Without correct id builder should throw exception")
  @Test
  public void requestBuilderException() {
    final Request.Builder builder = Request.Builder.newBuilder();
    assertThrows(IllegalArgumentException.class, builder::build);
  }
}
