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
public class CANFuzz extends AbstractScenario {
  private Map<Integer, Set<Integer>> ecuIDs;
  private static final int RANDOM_FUZZ_RUNNING_TIME = 300;
  private static final int MAX_FRAMES = 500000;

  private static Random rnd = new Random();

  public CANFuzz(VTCloud cloud, TestPoints testPoint) {
    super(cloud, testPoint);
  }

  @Override
  public void run() {
    // Get the ecu id and corresponding response id
    ecuIDs = readInPredefinedIDOrServices("ecuIDs");
    final CANResponseFilter filter = CANResponseFilter.filterIds(MIN_ID, MAX_ID);

    int responseWaitTime = getResponseWaitTime(testPoints);
    // Run 10 minutes random fuzz for ECU ID from 0x00 to 0x7FF
    randomFuzzForAllPotentialECUs(responseWaitTime, filter);

    List<Integer> canIds = new ArrayList<>(ecuIDs.keySet());
    int canIDSize = canIds.size();
    Random rn = new Random();

    int numSentRequests = 0;
    while (numSentRequests < MAX_FRAMES) {
      int requestId = canIds.get(rn.nextInt(canIDSize));
      try {
        //step 1
        numSentRequests += fixedLen(requestId, responseWaitTime, filter);
        //step 2
        numSentRequests += randomLen(requestId, responseWaitTime, filter);
        //step 3
        numSentRequests += regular(requestId, responseWaitTime, filter);
      } catch (Exception e) {
        if (e instanceof InterruptedException) {
          Thread.currentThread().interrupt();
          return;
        } else {
          log.error("CANFuzzTest Failed", e);
        }
      }
    }
  }

  /**
   * Generate can frames with fix length
   */
  private int fixedLenWithTries(int requestId, int delay, int tries, CANResponseFilter filter) {
    int MIN_POS = 0;
    int MAX_POS = 7;
    int COUNT = tries;

    final Collection<Request> requests = new ArrayList<>();
    for (int n = 0; n < COUNT; n++) {
      byte[] data = getGenericPayLoad();
      for (int i = MIN_POS; i <= MAX_POS; i++) {
        data[i] = (byte) randomInt(0, 255);
      }
      requests.add(Request.Builder.newBuilder()
          .id(requestId)
          .data(data)
          .waitTime(delay)
          .build());
    }
    final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
    // logs
    exportLogToConsole(responses);
    return COUNT;
  }

  private int fixedLen(int requestId, int delay, CANResponseFilter filter) {
    return fixedLenWithTries(requestId, delay, 256, filter);
  }

  /**
   * Generate can frames with len random
   */
  private int randomLen(int requestId, int delay, CANResponseFilter filter) {
    int MIN_LENGTH = 0;
    int MAX_LENGTH = 16;
    int COUNT = 128;

    Random rn = new Random();
    List<Byte> functionBytes = new ArrayList<>(8);

    final Collection<Request> requests = new ArrayList<>();
    for (int n = 0; n < COUNT; n++) {
      int length = randomInt(MIN_LENGTH, MAX_LENGTH);
      CANFrame entry = fuzzCurrentSubFunction(requestId, rn.nextInt(256), functionBytes, length);
      requests.add(new Request(entry, delay));
    }

    final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
    // logs
    exportLogToConsole(responses);
    return COUNT;
  }

  /**
   * Generate can frames with sequential increasing/decreasing value in byte array
   */
  private int regular(int canId, int delay, CANResponseFilter filter) {
    int MIN_POS = 0;
    int MAX_POS = 7;
    int BASE = 128;

    int totalEntries = 0;
    byte data[] = getGenericPayLoad();

    final Collection<Request> requests = new ArrayList<>();
    int upBase = BASE;
    while (upBase < 256) {
      for (int i = MIN_POS; i <= MAX_POS; i++) {
        data[i] = (byte) (upBase & 0xFF);
      }
      requests.add(Request.Builder.newBuilder()
          .id(canId)
          .data(data)
          .waitTime(delay)
          .build());
      upBase++;
      totalEntries++;
    }

    int lowBase = BASE;
    while (lowBase >= 0) {
      for (int i = MIN_POS; i <= MAX_POS; i++) {
        data[i] = (byte) (lowBase & 0xFF);
      }
      requests.add(Request.Builder.newBuilder()
          .id(canId)
          .data(data)
          .waitTime(delay)
          .build());
      lowBase--;
      totalEntries++;
    }

    //send Traffic
    final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
    // logs
    exportLogToConsole(responses);
    return totalEntries;
  }

  private void randomFuzzForAllPotentialECUs(int responseWaitTime, CANResponseFilter filter) {
    long startTime = System.currentTimeMillis();
    // Random fuzz for @RANDOM_FUZZ_RUNNING_TIME seconds
    for (int id = 0x001; id < 0x7FF; id++) {
      if (System.currentTimeMillis() > startTime + 1000 * RANDOM_FUZZ_RUNNING_TIME) {
        break;
      }
      fixedLenWithTries(id, responseWaitTime, 16, filter);
    }
  }

  public static int randomInt(int from, int to) {
    return rnd.nextInt((to - from) + 1) + from;
  }

}
