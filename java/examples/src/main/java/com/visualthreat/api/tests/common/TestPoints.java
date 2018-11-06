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
      "Scan all CAN buses inside vehicle and detect all connected ECUs and its ID values. " +
          "In this way, the CAN bus topology and the ECU list are collected",
      SYSTEM_SCAN,
      Arrays.asList(RESPONSE_WAIT_TIME.withDefault(300)),
      Collections.emptyList(), 21)),

  UDS_DISCOVER_SERVICES(new TestPoint(
      "uds-discover-service",
      "ECU Scan",
      "For each ECU found collected in Step \"CAN Bus System Probing\", " +
          "ECU Scan will emulate all pre-defined test points by simulating attacks onto the ECU",
      SYSTEM_SCAN,
      Collections.singletonList(RESPONSE_WAIT_TIME.withDefault(300)),
      Collections.singletonList(UDS_DISCOVER_IDS.id()), 22)),

  UDS_DISCOVER_SUBFUNCTIONS(new TestPoint(
      "uds-discover-subfunctions",
      "ECU Deep Scan",
      "Based on the testing results in Step \"ECU Scan\", further testings are performed on ECUs. " +
          "Results are analyzed and any noted vulnerabilities, inappropriate responses are logged/flagged " +
          "for further analysis",
      SYSTEM_SCAN,
      Arrays.asList(RESPONSE_WAIT_TIME.withDefault(16)),
      Collections.singletonList(UDS_DISCOVER_SERVICES.id()), 23)),

  CAN_FUZZ(new TestPoint(
      "ecu-uds-traffic-fuzzing",
      "ECU Traffic Fuzzing",
      "Based on what we have learned from current connected CAN Bus, " +
          "we compose fuzz traffic to verify that CAN still can behave normally",
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
      "It checks minimum latency of each ECU process requests. " +
          "The longer interval means current ECU is more prone to DOS attack.",
      FLOODING,
      Arrays.asList(
          RESPONSE_WAIT_TIME.withDefault(25),
          REQUEST_ID,
          RESPONSE_ID),
      Collections.singletonList(UDS_DISCOVER_IDS.id()), 81)),

  DOS_FLOOD(new TestPoint(
      "dos-flood",
      "DOS Attack",
      "It checks how current connected CAN Bus hand denial-of-service attacks",
      FLOODING,
      Arrays.asList(
          RESPONSE_WAIT_TIME.withDefault(25),
          REQUEST_ID),
      Collections.singletonList(UDS_DISCOVER_IDS.id()), 82)),

  SCAN_ROUTINE_CONTROL_VULNERABILITIES(new TestPoint(
      "scan-routine-control-vulnerabilities",
      "Scan Remote Invocation Vulnerabilities",
      "Based on the found services in Step \"ECU Scan\", " +
          "it checks whether there are exposed security vulnerabilities of each individual ECU " +
          "which can be leveraged to achieve \"remote invocation\" functionalities",
      TAMPERING,
      Arrays.asList(
          // TODO: session change
          RESPONSE_WAIT_TIME.withDefault(25),
          ERASE_MEMORY_ONLY),
      Collections.singletonList(UDS_DISCOVER_SERVICES.id()), 41)),

  SCAN_UDS_SERVICE_VULNERABILITIES(new TestPoint(
      "scan-uds-service-vulnerabilities",
      "Scan Privilege Elevation",
      "Based on the found services in Step \"ECU Scan\", it checks " +
          "whether there are exposed security vulnerabilities of each individual ECU " +
          "which can be leveraged to gain privilege",
      TAMPERING,
      Arrays.asList(
          RESPONSE_WAIT_TIME.withDefault(25)),
      Collections.singletonList(UDS_DISCOVER_SERVICES.id()), 42)),

  SCAN_UDS_SUB_FUNCTION_VULNERABILITIES(new TestPoint(
      "scan-uds-sub-function-vulnerabilities",
      "Scan Parameter Tampering",
      "Based on the found sub-services in Step \"ECU Deep Scan\", it checks " +
          "whether there are exposed security vulnerabilities of each individual ECU " +
          "which can be invoked successfully with various random parameter combinations",
      TAMPERING,
      Arrays.asList(
          RESPONSE_WAIT_TIME.withDefault(25)),
      Collections.singletonList(UDS_DISCOVER_SUBFUNCTIONS.id()), 43)),

  SECURITY_ACCESS(new TestPoint(
      "security-access",
      "ECU Security Scan",
      "Based on the found services in Step \"ECU Scan\", " +
          "it checks whether each ECU meets requirement of access security",
      SYSTEM_SCAN,
      Arrays.asList(
          RESPONSE_WAIT_TIME.withDefault(25),
          REQUEST_ID,
          RESPONSE_ID),
      Collections.singletonList(UDS_DISCOVER_SERVICES.id()), 24)),

  SIMULATE_REPROGRAMMING(new TestPoint(
      "simulate-reprogramming",
      "Simulate Reprogramming Attack",
      "It checks whether we can re-flash ECU firmware partially or entirely",
      TAMPERING,
      Arrays.asList(RESPONSE_WAIT_TIME.withDefault(25)),
      Collections.singletonList(UDS_DISCOVER_SERVICES.id()), 44)),

  VARY_DLC(new TestPoint(
      "vary-dlc",
      "CAN Frame Underflow Overflow Attack",
      "It checks whether current connected CAN Bus can handle invalid format CAN data packet",
      OTHER,
      Arrays.asList(
          RESPONSE_WAIT_TIME.withDefault(25)),
      Collections.singletonList(UDS_DISCOVER_IDS.id()), 201)),

  WRITE_DATA_BY_IDENTIFIER(new TestPoint(
      "write-data-by-identifier",
      "Modify ECU Data Attempt",
      "Based on the found services in step \"ECU Scan\", it checks " +
          "whether there are security vulnerabilities of each individual ECU " +
          "can be invoked successfully to modify ECU configurations or settings contents",
      TAMPERING,
      Arrays.asList(
          RESPONSE_WAIT_TIME.withDefault(25),
          MODIFY_VIN_ONLY),
      Collections.singletonList(UDS_DISCOVER_SERVICES.id()), 45)),

  WRITE_MEMORY_ADDRESS(new TestPoint(
      "write-memory-address",
      "Modify ECU Memory Attempt",
      "TBased on the found services in step \"ECU Scan\", " +
          "it checks whether we can leverage those services to modify ECU firmware itself",
      TAMPERING,
      Arrays.asList(
          RESPONSE_WAIT_TIME.withDefault(30)),
      Collections.singletonList(UDS_DISCOVER_SERVICES.id()), 46)),

  READ_MEMORY_ADDRESS(new TestPoint(
      "read-memory-address",
      "Dump ECU Memory",
      "It checks whether we can read ECU firmware partially or entirely",
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
      "It checks whether we can enable or disable communication of an ECU",
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
