package com.visualthreat.api.data;

import lombok.Value;

/**
 * Time slot.
 * Represents time interval start (inclusive) and duration.
 */
@Value
public class TimeSlot {
  private final long start;
  private final long duration;
}
