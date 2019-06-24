package com.visualthreat.api.tests;

import com.visualthreat.api.VTCloud;
import com.visualthreat.api.data.CANResponseFilter;
import com.visualthreat.api.data.Request;
import com.visualthreat.api.data.Response;
import com.visualthreat.api.tests.common.TestConst.DiagnosticSession;
import com.visualthreat.api.tests.common.TestPoints;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class VaryDLC extends AbstractScenario {
  private Map<Integer, Set<Integer>> ecuIDs;
  private final static int MAX_FRAME_NUM = 500;

  public VaryDLC(VTCloud cloud, TestPoints testPoint) {
    super(cloud, testPoint);
  }

  @Override
  public void run() {
    // Get the ecu id and corresponding response id
    ecuIDs = readInPredefinedIDOrServices("ecuIDs");
    final CANResponseFilter filter = CANResponseFilter.filterIds(MIN_ID, MAX_ID);
    for (final Integer requestId : ecuIDs.keySet()) {
      log.info(String.format("Starts testing ECU=0x%X", requestId));
      sendTestTrafficToECU(requestId, filter);
    }
  }

  private void sendTestTrafficToECU(final int requestId, CANResponseFilter filter) {
    final Random rn = new Random();
    Collection<Request> requests = new ArrayList<>();
    requests.add(enterSession(requestId, DiagnosticSession.PROGRAMMING));
    for (int i = 0; i < MAX_FRAME_NUM; i++) {
      final int dataLen = rn.nextInt(10) + 1;
      final byte[] data = new byte[dataLen];
      for (int j = 0; j < dataLen; j++) {
        data[j] = (byte) (0xFF);
      }
      requests.add(Request.Builder.newBuilder()
          .id(requestId)
          .data(data)
          .waitTime(getResponseWaitTime(testPoints))
          .build()
      );
    }
    // send traffic
    final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
    // logs
    exportLogToConsole(responses);
  }
}
