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
public class GMLANWriteIdentifier extends AbstractScenario {

  private static final int GMLAN_WRITE_IDENTIFIER_SERVICE = 0x3B;

  private static final int DEFAULT_MIN_PAYLOAD_LENGTH = 3;
  private static final int DEFAULT_MAX_PAYLOAD_LENGTH = 32;
  private Map<Integer, Set<Integer>> ecuIDs;


  public GMLANWriteIdentifier(VTCloud cloud, TestPoints testPoints) {
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
          sendWriteIdentifierTraffic(id, session, filter);
        }
      } catch (final IOException ex) {
        log.error("GMLAN Write Identifier failed", ex);
      }
    }
  }

  private void sendWriteIdentifierTraffic(
      int requestId, DiagnosticSession session, CANResponseFilter filter) throws IOException {
    final Collection<Request> requests = new ArrayList<>();
    requests.add(this.enterSession(requestId, session));
    List<Request> entries = null;
    for (int dataIdentifier = 0; dataIdentifier <= 0xFF; dataIdentifier++) {
      List<Byte> subFuncBytes = new LinkedList<>();
      subFuncBytes.add((byte) dataIdentifier);
      entries = prepareAndSendTrafficForSingleService(requestId, filter, GMLAN_WRITE_IDENTIFIER_SERVICE,
          subFuncBytes, DEFAULT_MIN_PAYLOAD_LENGTH, DEFAULT_MAX_PAYLOAD_LENGTH, getResponseWaitTime(testPoints));
      requests.addAll(entries);
    }

    // send traffic
    final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
  }
}
