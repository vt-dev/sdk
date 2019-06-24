package com.visualthreat.api.tests;

import com.visualthreat.api.VTCloud;
import com.visualthreat.api.data.CANResponseFilter;
import com.visualthreat.api.data.Request;
import com.visualthreat.api.data.Response;
import com.visualthreat.api.tests.common.TestConst.DiagnosticSession;
import com.visualthreat.api.tests.common.TestPoints;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;

@Slf4j
public class GMLANDeviceControl extends AbstractScenario {

  private static final int GMLAN_DEVICE_CONTROL_SERVICE = 0xAE;

  private static final int DEFAULT_MIN_PAYLOAD_LENGTH = 3;
  private static final int DEFAULT_MAX_PAYLOAD_LENGTH = 9;
  private Map<Integer, Set<Integer>> ecuIDs;


  public GMLANDeviceControl(VTCloud cloud, TestPoints testPoints) {
    super(cloud, testPoints);
  }

  @Override
  public void run() {
    Collection<Integer> responseIds = new HashSet<>();
    // Get the ecu id and corresponding response id
    ecuIDs = readInPredefinedIDOrServices("ecuIDs");
    final CANResponseFilter filter = CANResponseFilter.filterIds(MIN_ID, MAX_ID);

    for (Integer id : ecuIDs.keySet()) {
      try {
        for (DiagnosticSession session : sessionList) {
          sendDeviceControlTraffic(id, session, filter);
        }
      } catch (final IOException ex) {
        log.error("GMLAN Device Control failed", ex);
      }
    }
  }

  private void sendDeviceControlTraffic(
      int requestId, DiagnosticSession session, CANResponseFilter filter) throws IOException {
    log.info(String.format("Starts testing ECU=0x%X", requestId));

    final Collection<Request> requests = new ArrayList<>();
    requests.add(this.enterSession(requestId, session));
    List<Request> entries = null;
    // Fuzzing send Device control requests
    // the cpidNumber is 1-0xFE, we loop through all the combination of
    // cpidNumber
    for (int cpidNumber = 1; cpidNumber <= 0xFE; cpidNumber++) {
      List<Byte> subFuncBytes = new LinkedList<>();
      subFuncBytes.add((byte) cpidNumber);
      entries = prepareAndSendTrafficForSingleService(requestId, filter, GMLAN_DEVICE_CONTROL_SERVICE,
          subFuncBytes, DEFAULT_MIN_PAYLOAD_LENGTH, DEFAULT_MAX_PAYLOAD_LENGTH, getResponseWaitTime(testPoints));
      requests.addAll(entries);
    }

    // send traffic
    final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
  }
}
