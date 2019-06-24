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
public class SimulateReprogramming extends AbstractScenario {
  private Map<Integer, Set<Integer>> ecuIDs;
  private static final int NUM_OF_DOWNLOAD_ATTEMPTS = 256;

  public SimulateReprogramming(VTCloud cloud, TestPoints testPoint) {
    super(cloud, testPoint);
  }

  @Override
  public void run() {
    // Get the ecu id and corresponding response id
    ecuIDs = readInPredefinedIDOrServices("ecuIDs");
    final CANResponseFilter filter = CANResponseFilter.filterIds(MIN_ID, MAX_ID);
    for (Integer requestId : ecuIDs.keySet()) {
      try {
        log.info(String.format("Starts testing ECU=0x%X", requestId));
        sendSimulatedReprogrammingTraffic(requestId, filter);
      } catch (IOException e) {
        log.error("SimulateReprogrammingTest failed", e);
      }
    }
  }

  private void sendSimulatedReprogrammingTraffic(Integer requestId, CANResponseFilter filter)
      throws IOException {
    Random rn = new Random();
    byte dataSize = 0x20;
    int randomAddress = 0x0;
    try {
      // send request download
      Collection<Request> requests = new ArrayList<>();
      requests.add(enterSession(requestId, DiagnosticSession.PROGRAMMING));
      for (int tries = 0; tries < NUM_OF_DOWNLOAD_ATTEMPTS; tries++) {
        byte addr1 = (byte) (randomAddress >> 16 & 0xFF);
        byte addr2 = (byte) (randomAddress >> 8 & 0xFF);
        byte addr3 = (byte) (randomAddress & 0xFF);

        byte[] requestDownload1 = new byte[]{0x10, 0x0B, 0x34, 0x0, 0x44, 0x0, addr1, addr2};
        requests.add(Request.Builder.newBuilder()
            .id(requestId)
            .data(requestDownload1)
            .waitTime(getResponseWaitTime(testPoints))
            .build()
        );
        byte[] requestDownload2 = new byte[]{0x21, addr3, 0x0, 0x0, 0x0, dataSize, 0x0, 0x0};
        requests.add(Request.Builder.newBuilder()
            .id(requestId)
            .data(requestDownload2)
            .waitTime(getResponseWaitTime(testPoints))
            .build()
        );
        byte[] transferData1 = new byte[]{0x10, (byte) (dataSize + 2), 0x36, 0x01, 0x0D, 0x0, 0x3,
            0x12};
        requests.add(Request.Builder.newBuilder()
            .id(requestId)
            .data(transferData1)
            .waitTime(getResponseWaitTime(testPoints))
            .build()
        );
        int index = 1;
        do {
          byte[] transferData = getGenericPayLoad();
          if (index > 15) {
            index = 0;
          }
          transferData[0] = (byte) (0x20 + index);
          for (int i = 1; i < 8; i++) {
            transferData[i] = (byte) (rn.nextInt(128) & 0xFF);
          }
          requests.add(Request.Builder.newBuilder()
              .id(requestId)
              .data(transferData)
              .waitTime(getResponseWaitTime(testPoints))
              .build()
          );
          index++;
        } while (((index - 1) * 7) < dataSize);

        byte[] requestTransferExit = new byte[]{0x1, 0x37, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0};
        requests.add(Request.Builder.newBuilder()
            .id(requestId)
            .data(requestTransferExit)
            .waitTime(getResponseWaitTime(testPoints))
            .build()
        );
        // some car models need to call routine control
        byte[] routineControl = new byte[]{0x4, 0x31, 0x1, 0x3, 0x4, 0x0, 0x0, 0x0};
        requests.add(Request.Builder.newBuilder()
            .id(requestId)
            .data(routineControl)
            .waitTime(getResponseWaitTime(testPoints))
            .build()
        );
        routineControl[3] = (byte) (rn.nextInt(256) & 0xFF);
        routineControl[4] = (byte) (rn.nextInt(64) & 0xFF);
        requests.add(Request.Builder.newBuilder()
            .id(requestId)
            .data(routineControl)
            .waitTime(getResponseWaitTime(testPoints))
            .build()
        );
        randomAddress = rn.nextInt(0xFFF - dataSize);
      }
      // send traffic
      final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
      // logs
      exportLogToConsole(responses);
    } catch (Exception e) {
      log.error("Sending SimulateReprogramming failed!");
    }
  }
}
