package com.visualthreat.api.data;

import lombok.Data;

/**
 * Data class.
 * Represents CAN Frame.
 */
@Data
public class CANFrame {
  private static final int TO_UNSIGNED_BYTE = 0xFF;

  private final long timestamp;
  private final int id;
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
    StringBuilder sb = new StringBuilder();
    sb.append("data=[");
    for(int i = 0; i < data.length; i++){
      sb.append(String.format("0x%s,", Integer.toHexString(data[i] & TO_UNSIGNED_BYTE)));
    }
    sb.deleteCharAt(sb.length() - 1);
    sb.append("])");
    return String.format("CANFrame(timestamp=%d, id=0x%s, %s",
        timestamp, Integer.toHexString(id), sb.toString());
  }
}
