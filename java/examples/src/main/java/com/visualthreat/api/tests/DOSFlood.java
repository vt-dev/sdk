package com.visualthreat.api.tests;

import com.visualthreat.api.VTCloud;
import com.visualthreat.api.data.CANResponseFilter;
import com.visualthreat.api.data.Request;
import com.visualthreat.api.data.Response;
import com.visualthreat.api.tests.common.TestPoints;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class DOSFlood extends AbstractScenario {
  private static int MAX_CAN_FRAME_COUNT = 5000;
  private static int MAX_CAN_FRAME_COUNT_PER_ROUND = 2000;

  private Map<Integer, Set<Integer>> ecuIDs;

  public DOSFlood(VTCloud cloud, TestPoints testPoint) {
    super(cloud, testPoint);
  }

  @Override
  public void run() {
    // Get the ecu id and corresponding response id
    ecuIDs = readInPredefinedIDOrServices("ecuIDs");

    final CANResponseFilter filter = CANResponseFilter.filterIds(MIN_ID, MAX_ID);
    final Collection<Request> requests = new ArrayList<>();
    for (Integer requestId : ecuIDs.keySet()) {
      byte[] data = new byte[]{0x02, 0x3E, (byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00};
      int totalSent = MAX_CAN_FRAME_COUNT;
      while (totalSent / MAX_CAN_FRAME_COUNT_PER_ROUND > 0) {
        totalSent = totalSent - MAX_CAN_FRAME_COUNT_PER_ROUND;
        for (int i = 0; i < MAX_CAN_FRAME_COUNT_PER_ROUND; i++) {
          requests.add(Request.Builder.newBuilder()
              .id(requestId)
              .data(data)
              .waitTime(getResponseWaitTime(testPoints))
              .build());
        }
        final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
        // logs
        exportLogToConsole(responses);
      }
    }
  }
}
