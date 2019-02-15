package com.visualthreat.api.data;

import java.util.Optional;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * Data class.
 * Filters byte values of CAN Frames.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
@Value
public final class ByteArrayFilter {
  private final Optional<Byte> byte0;
  private final Optional<Byte> byte1;
  private final Optional<Byte> byte2;
  private final Optional<Byte> byte3;
  private final Optional<Byte> byte4;
  private final Optional<Byte> byte5;
  private final Optional<Byte> byte6;
  private final Optional<Byte> byte7;

  @Data
  @Accessors(fluent = true)
  @RequiredArgsConstructor(staticName = "newBuilder")
  public static class Builder {
    private Byte byte0 = null;
    private Byte byte1 = null;
    private Byte byte2 = null;
    private Byte byte3 = null;
    private Byte byte4 = null;
    private Byte byte5 = null;
    private Byte byte6 = null;
    private Byte byte7 = null;

    public ByteArrayFilter build() {
      return new ByteArrayFilter(
          Optional.ofNullable(byte0), Optional.ofNullable(byte1),
          Optional.ofNullable(byte2), Optional.ofNullable(byte3),
          Optional.ofNullable(byte4), Optional.ofNullable(byte5),
          Optional.ofNullable(byte6), Optional.ofNullable(byte7));
    }
  }
}
