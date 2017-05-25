package com.visualthreat.api.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * Data class, represents CAN Request.
 * Contains collection of CAN frames and time to wait for any of them.
 */
@Value
@RequiredArgsConstructor
public class Request {
  /**
   * Default value for waitTime = 35 ms.
   */
  private static final int DEFAULT_WAIT_TIME = 35;

  /**
   * CAN frame, will be send to a device.
   */
  private final CANFrame canFrame;
  /**
   * Time to wait. Device will return responses collected in wait time.
   */
  private final long waitTime;

  @Data(staticConstructor = "newBuilder")
  @Accessors(fluent = true)
  public static class Builder {
    private int id = -1;
    private byte[] data = {};
    private long waitTime = DEFAULT_WAIT_TIME;

    public Request build() throws IllegalArgumentException {
      if (id < 0) {
        throw new IllegalArgumentException("Set positive ID for CAN Frame");
      }
      return new Request(new CANFrame(id, data), waitTime);
    }
  }
}
