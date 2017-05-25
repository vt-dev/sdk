package com.visualthreat.api.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;

import java.util.Collection;

/**
 * Data class.
 * Contains device name, device owner, current availability, your time slots.
 */
@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class Device {
  public static final String TEST_DEVICE = "test-device";

  private final String deviceId;
  private final String name;
  private final Collection<TimeSlot> timeSlots;

  public boolean isAvailable() {
    final long now = System.currentTimeMillis();
    if (timeSlots == null) {
      return true;
    }

    for (final TimeSlot ts : timeSlots) {
      if (now >= ts.getStart() && now < ts.getStart() + ts.getDuration()) {
        return true;
      }
    }

    return false;
  }
}
