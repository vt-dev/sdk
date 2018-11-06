package com.visualthreat.api.tests;

import com.visualthreat.api.VTCloud;
import com.visualthreat.api.data.CANResponseFilter;
import com.visualthreat.api.data.Request;
import com.visualthreat.api.data.Response;
import com.visualthreat.api.tests.common.TestConst.DiagnosticSession;
import com.visualthreat.api.tests.common.TestPoints;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WriteDataByIdentifier extends AbstractScenario {
  private Map<Integer, Set<Integer>> ecuIDs;
  private static final int WRITE_DATA_BY_IDENTIFIER_SERVICE = 0x2E;

  public WriteDataByIdentifier(VTCloud cloud, TestPoints testPoint) {
    super(cloud, testPoint);
  }

  @Override
  public void run() {
    // Get the ecu id and corresponding response id
    ecuIDs = readInPredefinedIDOrServices("ecuIDs");
    final CANResponseFilter filter = CANResponseFilter.filterIds(MIN_ID, MAX_ID);
    for (Integer requestId : ecuIDs.keySet()) {
      log.info(String.format("Starts testing ECU=0x%X", requestId));
      try {
        sendWriteDataByIdentifierTraffic(requestId, filter);
      } catch (IOException ex) {
        log.error("WriteDataByIdentifierTest failed", ex);
      }
    }
  }

  private void sendWriteDataByIdentifierTraffic(
      Integer requestId, CANResponseFilter filter) throws IOException {
    // create request
    Random rn = new Random();
    Collection<Request> requests = new ArrayList<>();
    requests.add(enterSession(requestId, DiagnosticSession.PROGRAMMING));

    List<Byte> subFunctionBytes = new ArrayList<>(8);
    subFunctionBytes.add((byte)0x0);
    subFunctionBytes.add((byte)0x0);

    // Categorize all the services and get supported request services and services need different session.
    Map<String, List<byte[]>> result = new HashMap<>();
    log.info("sending first round request to get supported services and services need different session.\n");
    requests.addAll(sendTrafficForGivenGroupServices(requestId, filter, WRITE_DATA_BY_IDENTIFIER_SERVICE,
        subFunctionBytes, DEFAULT_MIN_PAYLOAD_LENGTH,DEFAULT_MAX_PAYLOAD_LENGTH));

    subFunctionBytes.set(0, (byte) 0xF1);
    subFunctionBytes.set(1, (byte) 0x90);
    requests.addAll(sendTrafficForGivenGroupServices(requestId, filter, WRITE_DATA_BY_IDENTIFIER_SERVICE,
        subFunctionBytes, 16,30));
    // send traffic
    final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
    // logs
    exportLogToConsole(responses);
  }

  private Collection<Request> sendTrafficForGivenGroupServices(Integer requestId,
      CANResponseFilter filter, int serviceID,
      List<Byte> subFunctionBytes,
      int payLoadMinLength,
      int payLoadMaxLength) {
    Collection<Request> requests = new ArrayList<>();
    requests.addAll(prepareAndSendTrafficForSingleService(requestId, filter, serviceID,
          subFunctionBytes, payLoadMinLength, payLoadMaxLength, getResponseWaitTime(testPoints)));
    return requests;
  }
}
