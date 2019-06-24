package com.visualthreat.api.tests;

import com.visualthreat.api.VTCloud;
import com.visualthreat.api.data.CANFrame;
import com.visualthreat.api.data.CANResponseFilter;
import com.visualthreat.api.data.Request;
import com.visualthreat.api.data.Response;
import com.visualthreat.api.tests.common.TestPoints;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public final class Fuzzing extends AbstractScenario {
  private static final Random rnd = new SecureRandom();
  private static final List<CANFrame> frames = new LinkedList<>();
  private static final long WAIT_TIME = 30;
  // Number of fuzz iterations
  private static final int ITER_COUNT = 10;
  private static int count = 0;

  public Fuzzing(VTCloud cloud, TestPoints testPoints) {
    super(cloud, testPoints);
  }

  /**
   * Generating random Request, with random data, data length and random id.
   */
  private static Request generateRequest() {
    final int frameId = rnd.nextInt(0x800);
    final int dataLength = rnd.nextInt(8) + 1;
    final byte[] frameData = new byte[dataLength];
    rnd.nextBytes(frameData);
    return new Request(new CANFrame(frameId, frameData), WAIT_TIME);
  }

  private void makeRequest() {
    log.info("Iteration # " + count);
    try {
      // Send requests to device and wait for a response
      final Collection<Request> requests = IntStream.range(0, 20)
          .mapToObj(i -> generateRequest())
          .collect(Collectors.toList());
      // get responses with IDs between 0x0 and 0x300 (inclusively) only
      final Iterator<Response> responses = cloud.sendCANFrames(
          requests, CANResponseFilter.filterIds(0, 0x300));
      while (responses.hasNext()) {
        final Response response = responses.next();
        logRequestFrame(response.getRequest());
        handleResponse(response.getResponses());
      }
    } catch (final Exception e) {
      log.error("Something went wrong", e);
      cloud.close();
    }
  }

  /**
   * Counting simple stats from all responses.
   * Count unique ids, and count of concrete id.
   */
  private static void handleData(final List<CANFrame> canFrames) {
    final Map<Integer, Long> ids = canFrames.stream().collect(
        Collectors.groupingBy(CANFrame::getId, Collectors.counting()));
    final int uniqueIDs = ids.size();
    log.info("Amount of unique IDs: " + uniqueIDs);
    ids.forEach((id, num) ->
        log.info("ID 0x" + Integer.toHexString(id) + ": " + num + " responses"));
  }

  private static void handleResponse(Iterator<CANFrame> responses) {
    while (responses.hasNext()) {
      final CANFrame frame = responses.next();
      logResponseFrame(frame);
      frames.add(frame);
    }
  }

  @Override
  public void run() {
    // open a connection to cloud
    for (count = 0; count < ITER_COUNT; count++) {
      makeRequest();
    }
    // handle collected frames to handler
    handleData(frames);
    cloud.close();
  }
}
