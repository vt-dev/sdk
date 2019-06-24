package com.visualthreat.api.tests;

import com.visualthreat.api.VTCloud;
import com.visualthreat.api.data.CANFrame;
import com.visualthreat.api.data.CANResponseFilter;
import com.visualthreat.api.data.Request;
import com.visualthreat.api.data.Response;
import com.visualthreat.api.tests.common.TestPoints;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class DetectResponseRate extends AbstractScenario {
  private final static int NUM_FRAMES = 25;
  private final static int MAX_DELAY = 3000;
  private Map<Integer, Set<Integer>> ecuIDs;

  public DetectResponseRate(VTCloud cloud, TestPoints testPoint) {
    super(cloud, testPoint);
  }

  @Override
  public void run() {
    // Get the ecu id and corresponding response id
    ecuIDs = readInPredefinedIDOrServices("ecuIDs");
    final CANResponseFilter filter = CANResponseFilter.filterIds(MIN_ID, MAX_ID);
    for (int requestId : ecuIDs.keySet()) {
      for (Integer respId : ecuIDs.get(requestId)) {
        testDelay(requestId, respId, getResponseWaitTime(testPoints), filter);
      }
    }
  }

  private double sendTraffic(int requestId, int responseId, int delay, CANResponseFilter filter) {
    final Collection<Request> requests = new ArrayList<>();
    int responseCnt = 0;
    byte[] data = new byte[]{0x02, 0x3E, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    for (int i = 0; i < NUM_FRAMES; i++) {
      requests.add(Request.Builder.newBuilder()
          .id(requestId)
          .data(data)
          .waitTime(delay)
          .build());
    }

    //send Traffic
    final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
    // logs
    responseCnt = exportLogToConsoles(responses, responseId);
    return (double) (responseCnt / NUM_FRAMES);
  }

  private int testDelay(int requestId, int responseId, int delay, CANResponseFilter filter) {
    int fastFactor = 1;
    double curDelay = sendTraffic(requestId, responseId, delay, filter);
    while (curDelay < 1 - 0.02) { //0.02 is allowed error
      if (delay >= 1000) {
        delay += 1000 * fastFactor;
      } else if (delay >= 100) {
        delay += 100 * fastFactor;
      } else if (delay >= 50) {
        delay += 50 * fastFactor;
      } else {
        delay += 2 * fastFactor;
      }
      if (delay >= MAX_DELAY) {
        break;
      }
      curDelay = sendTraffic(requestId, responseId, delay, filter);
      if (curDelay < 0.02) {
        fastFactor = 3;
      } else {
        fastFactor = 1;
      }
    }
    return delay;
  }

  private int exportLogToConsoles(
      Iterator<Response> logsIterator, int responseId) {
    int count = 0;
    CANFrame requestEntry = null;
    while (logsIterator.hasNext()) {
      Response entry = logsIterator.next();
      requestEntry = entry.getRequest();
      logRequestFrame(requestEntry);
      Iterator<CANFrame> responses = entry.getResponses();
      while (responses.hasNext()) {
        CANFrame response = responses.next();
        logResponseFrame(response);
        if (response.getId() == responseId) {
          count++;
        }
      }
    }
    return count;
  }
}
