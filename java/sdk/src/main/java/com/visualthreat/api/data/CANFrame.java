package com.visualthreat.api.data;

import lombok.Getter;

/**
 * Data class.
 * Represents CAN Frame.
 */
public class CANFrame {
  private static final int TO_UNSIGNED_BYTE = 0xFF;

  @Getter
  private final long timestamp;
  @Getter
  private final int id;
  @Getter
  private final byte[] data;

  public CANFrame(final long timestamp, final int id, final byte[] data) {
    this.timestamp = timestamp;
    this.id = id;
    this.data = data;
  }

  public CANFrame(final int id, final byte[] data) {
    this(System.currentTimeMillis(), id, data);
  }

  @Override
  public String toString() {
    return String.format("CANFrame(timestamp=%d, id=0x%s, data=[0x%s, 0x%s, 0x%s, 0x%s, 0x%s, 0x%s, 0x%s, 0x%s])",
        timestamp, Integer.toHexString(id),
        Integer.toHexString(data[0] & TO_UNSIGNED_BYTE),
        Integer.toHexString(data[1] & TO_UNSIGNED_BYTE),
        Integer.toHexString(data[2] & TO_UNSIGNED_BYTE),
        Integer.toHexString(data[3] & TO_UNSIGNED_BYTE),
        Integer.toHexString(data[4] & TO_UNSIGNED_BYTE),
        Integer.toHexString(data[5] & TO_UNSIGNED_BYTE),
        Integer.toHexString(data[6] & TO_UNSIGNED_BYTE),
        Integer.toHexString(data[7] & TO_UNSIGNED_BYTE));
  }
}
