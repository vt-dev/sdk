package com.visualthreat.api.data;

import lombok.Getter;

/**
 * Data class.
 * Represents CAN Frame.
 */
public class CANFrame {
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
        Integer.toHexString(data[0]),
        Integer.toHexString(data[1]),
        Integer.toHexString(data[2]),
        Integer.toHexString(data[3]),
        Integer.toHexString(data[4]),
        Integer.toHexString(data[5]),
        Integer.toHexString(data[6]),
        Integer.toHexString(data[7]));
  }
}
