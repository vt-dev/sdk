package com.visualthreat.api;


import com.visualthreat.api.data.Device;
import com.visualthreat.api.data.TimeSlot;
import com.visualthreat.api.v1.Utils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Device test")
public class DeviceTest {

  @DisplayName("Device should be extracted from JSON")
  @Test
  public void deviceShouldBeParsedFromString() {
    final String deviceId = "deviceId";
    final String deviceName = "name";

    final String deviceJson = String.format("{\"deviceId\":\"%s\",\"name\":\"%s\",\"timeSlots\":[{\"start\":123,\"duration\":123}]}",
        deviceId, deviceName);
    final Device device = Utils.decodeJson(deviceJson, Device.class);
    assertNotNull(device, "Device parse error");
    assertEquals(device.getDeviceId(), deviceId, "Device id parse error");
    assertEquals(device.getName(), deviceName, "Device name parse error");
    assertFalse(device.getTimeSlots().isEmpty(), "TimeSlots parse error");
  }

  @DisplayName("Device availability should be counted correct. Correct interval")
  @Test
  public void deviceAvailabilityHit() {
    final long now = System.currentTimeMillis();
    final long start = now - 10000L;
    final long duration = 20000L;
    final TimeSlot timeSlot = new TimeSlot(start, duration);
    final Device device = new Device("id", "name", Collections.singletonList(timeSlot));
    assertTrue(device.isAvailable(), "Availability count error");
  }

  @DisplayName("Device availability should be counted correct. Incorrect interval")
  @Test
  public void deviceAvailabilityMiss() {
    final long now = System.currentTimeMillis();
    final long start = now - 10000L;
    final long duration = 5000L;
    final TimeSlot timeSlot = new TimeSlot(start, duration);
    final Device device = new Device("id", "name", Collections.singletonList(timeSlot));
    assertFalse(device.isAvailable(), "Availability count error");
  }
}
