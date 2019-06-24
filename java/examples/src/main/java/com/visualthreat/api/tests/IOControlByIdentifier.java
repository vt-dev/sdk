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
public class IOControlByIdentifier extends AbstractScenario {
  private static final int INPUT_OUTPUT_CONTROL_BY_IDENTIFIER = 0x2F;
  private static final int[] INPUT_OUTPUT_CONTROL_TYPE = {0, 1, 2, 3, 7};
  private static final int FIRST_DID_LENGTH = 20;
  private static final int SECOND_DID_LENGTH = 20;

  private static final int DEFAULT_MIN_PAYLOAD_LENGTH = 5;
  private static final int DEFAULT_MAX_PAYLOAD_LENGTH = 6;

  private Map<Integer, Set<Integer>> ecuIds = new HashMap<>();

  public IOControlByIdentifier(VTCloud cloud, TestPoints testPoints) {
    super(cloud, testPoints);
  }

  @Override
  public void run() {
    try {
      // Get the ecu id and corresponding response id
      ecuIds = readInPredefinedIDOrServices("ecuIDs");
      final CANResponseFilter filter = CANResponseFilter.filterIds(MIN_ID, MAX_ID);
      for (Integer id : ecuIds.keySet()) {
        log.info(String.format("Starts testing ECU=0x%X", id));
        try {
          for (DiagnosticSession session : sessionList) {
            sendWriteDataByIdentifierTraffic(id, session, filter);
          }
        } catch (IOException ex) {
          log.error("Manipulate ECU Function Attempt Test failed", ex);
        }
      }
    } catch (Exception e) {
      log.error("Test failed with exception", e);
    }
  }

  private void sendWriteDataByIdentifierTraffic(
      Integer requestId, DiagnosticSession session, CANResponseFilter filter)
      throws IOException {
    // data identifier has two bytes, which is first did and second did
    // for IO_CONTROL, the values should be in one of INPUT_OUTPUT_CONTROL_TYPE
    final Collection<Request> requests = new ArrayList<>();
    requests.add(this.enterSession(requestId, session));
    List<Request> entries = null;
    for (int firstDID = 0; firstDID < FIRST_DID_LENGTH; firstDID++) {
      for (int secondDID = 0; secondDID < SECOND_DID_LENGTH; secondDID++) {
        for (int controlType : INPUT_OUTPUT_CONTROL_TYPE) {
          List<Byte> subFunctionBytes = new ArrayList<>();
          subFunctionBytes.add((byte) firstDID);
          subFunctionBytes.add((byte) secondDID);
          subFunctionBytes.add((byte) controlType);
          prepareAndSendTrafficForSingleService(requestId, filter, INPUT_OUTPUT_CONTROL_BY_IDENTIFIER,
              subFunctionBytes, DEFAULT_MIN_PAYLOAD_LENGTH, DEFAULT_MAX_PAYLOAD_LENGTH, getResponseWaitTime(testPoints));
          requests.addAll(entries);
        }
      }
    }
    // send traffic
    final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
  }
}
