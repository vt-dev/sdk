package com.visualthreat.api.tests.common;

import static com.visualthreat.api.tests.common.TestCategory.FLOODING;
import static com.visualthreat.api.tests.common.TestCategory.FUZZING;
import static com.visualthreat.api.tests.common.TestCategory.INTERNAL;
import static com.visualthreat.api.tests.common.TestCategory.OTHER;
import static com.visualthreat.api.tests.common.TestCategory.REVERSING;
import static com.visualthreat.api.tests.common.TestCategory.SYSTEM_SCAN;
import static com.visualthreat.api.tests.common.TestCategory.TAMPERING;
import static com.visualthreat.api.tests.common.TestParameters.DUMP_READ_MEMORY;
import static com.visualthreat.api.tests.common.TestParameters.ERASE_MEMORY_ONLY;
import static com.visualthreat.api.tests.common.TestParameters.MODIFY_VIN_ONLY;
import static com.visualthreat.api.tests.common.TestParameters.REQUEST_ID;
import static com.visualthreat.api.tests.common.TestParameters.RESPONSE_ID;
import static com.visualthreat.api.tests.common.TestParameters.RESPONSE_WAIT_TIME;
import static com.visualthreat.api.tests.common.TestParameters.USE_DISCOVERED_IDS;

import java.util.Arrays;
import java.util.Collections;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TestPoints {
  UDS_DISCOVER_IDS(new TestPoint(
      "uds-discover-ids",
      "CAN Bus System Probing",
      "",
      SYSTEM_SCAN,
      Arrays.asList(RESPONSE_WAIT_TIME.withDefault(300)),
      Collections.emptyList(), 21)),

  UDS_DISCOVER_SERVICES(new TestPoint(
      "uds-discover-service",
      "ECU Scan",
      "",
      SYSTEM_SCAN,
      Collections.singletonList(RESPONSE_WAIT_TIME.withDefault(300)),
      Collections.singletonList(UDS_DISCOVER_IDS.id()), 22)),

  UDS_DISCOVER_SUBFUNCTIONS(new TestPoint(
      "uds-discover-subfunctions",
      "ECU Deep Scan",
      "",
      SYSTEM_SCAN,
      Arrays.asList(RESPONSE_WAIT_TIME.withDefault(16)),
      Collections.singletonList(UDS_DISCOVER_SERVICES.id()), 23)),

  CAN_FUZZ(new TestPoint(
      "ecu-uds-traffic-fuzzing",
      "ECU Traffic Fuzzing",
      "",
      FUZZING,
      Arrays.asList(
          RESPONSE_WAIT_TIME.withDefault(25)),
      Collections.singletonList(UDS_DISCOVER_IDS.id()), 61)),

  SNIFF(new TestPoint(
      "sniff",
      "Sniff",
      "Sniff",
      INTERNAL,
      Arrays.asList(
          RESPONSE_WAIT_TIME.withDefault(25)),
      Collections.emptyList(), TestPoint.DEFAULT_SORT_ORDER)),

  CAN_TRAFFIC_RESPONSE_RATE(new TestPoint(
      "can-traffic-response-rate",
      "Traffic Handling",
      "",
      FLOODING,
      Arrays.asList(
          RESPONSE_WAIT_TIME.withDefault(25),
          REQUEST_ID,
          RESPONSE_ID),
      Collections.singletonList(UDS_DISCOVER_IDS.id()), 81)),

  DOS_FLOOD(new TestPoint(
      "dos-flood",
      "DOS Attack",
      "",
      FLOODING,
      Arrays.asList(
          RESPONSE_WAIT_TIME.withDefault(25),
          REQUEST_ID),
      Collections.singletonList(UDS_DISCOVER_IDS.id()), 82)),

  SCAN_ROUTINE_CONTROL_VULNERABILITIES(new TestPoint(
      "scan-routine-control-vulnerabilities",
      "Scan Remote Invocation Vulnerabilities",
      "",
      TAMPERING,
      Arrays.asList(
          // TODO: session change
          RESPONSE_WAIT_TIME.withDefault(25),
          ERASE_MEMORY_ONLY),
      Collections.singletonList(UDS_DISCOVER_SERVICES.id()), 41)),

  SCAN_UDS_SERVICE_VULNERABILITIES(new TestPoint(
      "scan-uds-service-vulnerabilities",
      "Scan Privilege Elevation",
      "",
      TAMPERING,
      Arrays.asList(
          RESPONSE_WAIT_TIME.withDefault(25)),
      Collections.singletonList(UDS_DISCOVER_SERVICES.id()), 42)),

  SCAN_UDS_SUB_FUNCTION_VULNERABILITIES(new TestPoint(
      "scan-uds-sub-function-vulnerabilities",
      "Scan Parameter Tampering",
      "",
      TAMPERING,
      Arrays.asList(
          RESPONSE_WAIT_TIME.withDefault(25)),
      Collections.singletonList(UDS_DISCOVER_SUBFUNCTIONS.id()), 43)),

  SECURITY_ACCESS(new TestPoint(
      "security-access",
      "ECU Security Scan",
      "",
      SYSTEM_SCAN,
      Arrays.asList(
          RESPONSE_WAIT_TIME.withDefault(25),
          REQUEST_ID,
          RESPONSE_ID),
      Collections.singletonList(UDS_DISCOVER_SERVICES.id()), 24)),

  SIMULATE_REPROGRAMMING(new TestPoint(
      "simulate-reprogramming",
      "Simulate Reprogramming Attack",
      "",
      TAMPERING,
      Arrays.asList(RESPONSE_WAIT_TIME.withDefault(25)),
      Collections.singletonList(UDS_DISCOVER_SERVICES.id()), 44)),

  VARY_DLC(new TestPoint(
      "vary-dlc",
      "CAN Frame Underflow Overflow Attack",
      "",
      OTHER,
      Arrays.asList(
          RESPONSE_WAIT_TIME.withDefault(25)),
      Collections.singletonList(UDS_DISCOVER_IDS.id()), 201)),

  WRITE_DATA_BY_IDENTIFIER(new TestPoint(
      "write-data-by-identifier",
      "Modify ECU Data Attempt",
      "",
      TAMPERING,
      Arrays.asList(
          RESPONSE_WAIT_TIME.withDefault(25),
          MODIFY_VIN_ONLY),
      Collections.singletonList(UDS_DISCOVER_SERVICES.id()), 45)),

  WRITE_MEMORY_ADDRESS(new TestPoint(
      "write-memory-address",
      "Modify ECU Memory Attempt",
      "",
      TAMPERING,
      Arrays.asList(
          RESPONSE_WAIT_TIME.withDefault(30)),
      Collections.singletonList(UDS_DISCOVER_SERVICES.id()), 46)),

  READ_MEMORY_ADDRESS(new TestPoint(
      "read-memory-address",
      "Dump ECU Memory",
      "",
      REVERSING,
      Arrays.asList(
          RESPONSE_WAIT_TIME.withDefault(55),
          REQUEST_ID,
          RESPONSE_ID,
          DUMP_READ_MEMORY),
      Collections.singletonList(UDS_DISCOVER_SERVICES.id()), 48)),

  MANIPULATE_COMMUNICATION(new TestPoint(
      "ecu-communication-control",
      "Manipulate ECU Communication",
      "",
      REVERSING,
      Arrays.asList(
          RESPONSE_WAIT_TIME.withDefault(25),
          REQUEST_ID,
          RESPONSE_ID,
          USE_DISCOVERED_IDS),
      Collections.singletonList(UDS_DISCOVER_SERVICES.id()), 50));

  @Getter
  private final TestPoint testPoint;

  public String id() {
    return testPoint.getId();
  }

  public static TestPoints getEnum(final String value) {
    for (final TestPoints testPoint : TestPoints.values()) {
      if (testPoint.id().equalsIgnoreCase(value)) {
        return testPoint;
      }
    }

    return null;
  }

  public static TestPoints getByName(final String name) {
    for (final TestPoints testPoint : TestPoints.values()) {
      if (testPoint.getTestPoint().getName().equalsIgnoreCase(name)) {
        return testPoint;
      }
    }
    return null;
  }
}
