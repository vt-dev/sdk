package com.visualthreat.api.tests;

import com.visualthreat.api.VTCloud;
import com.visualthreat.api.data.CANFrame;
import com.visualthreat.api.data.CANResponseFilter;
import com.visualthreat.api.data.Request;
import com.visualthreat.api.data.Response;
import com.visualthreat.api.tests.common.TestConst;
import com.visualthreat.api.tests.common.TestConst.DiagnosticSession;
import com.visualthreat.api.tests.common.TestParameter;
import com.visualthreat.api.tests.common.TestParameters;
import com.visualthreat.api.tests.common.TestPoints;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Slf4j
abstract public class AbstractScenario {

  @Getter
  protected final VTCloud cloud;

  @Getter
  protected final TestPoints testPoints;
  private static final int REQUEST_TIMEOUT_INTERVAL = 5;

  public static final byte[] ENTER_PROG_SESSION =
      new byte[]{0x02, 0x10, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00};
  private static final byte[] GENERIC_PAYLOAD =
      new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

  protected static final ArrayList<DiagnosticSession> sessionList =
      new ArrayList<>(Arrays.asList(DiagnosticSession.PROGRAMMING, DiagnosticSession.EXTENDED));

  protected static final int MIN_ID = 0x7df;
  protected static final int MAX_ID = 0x7ff;
  protected static final int DEFAULT_MIN_PAYLOAD_LENGTH = 1;
  protected static final int DEFAULT_MAX_PAYLOAD_LENGTH = 8;

  abstract public void run();

  protected int getResponseWaitTime(TestPoints testPoints) {
    List<TestParameter> testParameters = testPoints.getTestPoint().getParams();
    for (TestParameter testParameter : testParameters) {
      if (testParameter.equals(TestParameters.RESPONSE_WAIT_TIME)) {
        return (int) testParameter.getDefaultValue();
      }
    }
    return 25;
  }


  /**
   * Read in the ecu id and ecu services from resource folder
   *
   * @param fileName
   * @return
   */
  protected Map<Integer, Set<Integer>> readInPredefinedIDOrServices(String fileName) {
    InputStream in = getClass().getResourceAsStream("/" + fileName);
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    Map<Integer, Set<Integer>> map = new HashMap<>();
    String line;
    try {
      while ((line = reader.readLine()) != null) {
        String[] idAndRspIds = line.split(Pattern.quote(":"));
        String[] ids = idAndRspIds[0].split(Pattern.quote("x"));
        String[] rspIDs = idAndRspIds[1].split(Pattern.quote("x"));
        int requestID = Integer.parseInt(ids[1], 16);
        int responseID = Integer.parseInt(rspIDs[1], 16);
        if (map.containsKey(requestID)) {
          Set<Integer> rspSet = map.get(requestID);
          rspSet.add(responseID);
          map.put(requestID, rspSet);
        } else {
          Set<Integer> set = new HashSet<>();
          set.add(responseID);
          map.put(requestID, set);
        }
      }
    } catch (IOException e) {
      log.error("Can't load ECU ids for ScanECUServices!");
    }
    return map;
  }

  protected static void logRequestFrame(final CANFrame frame) {
    log.info("===>{}", frame);
  }

  protected static void logResponseFrame(final CANFrame frame) {
    log.info("<==={}", frame);
  }

  protected static void exportLogToConsole(
      Iterator<Response> logsIterator) {

    CANFrame requestEntry = null;
    while (logsIterator.hasNext()) {
      Response entry = logsIterator.next();
      requestEntry = entry.getRequest();
      logRequestFrame(requestEntry);
      Iterator<CANFrame> responses = entry.getResponses();
      while (responses.hasNext()) {
        CANFrame response = responses.next();
        logResponseFrame(response);
      }
    }
  }

  protected static byte[] getGenericPayLoad() {
    return Arrays.copyOf(GENERIC_PAYLOAD, GENERIC_PAYLOAD.length);
  }

  public static CANFrame fuzzCurrentSubFunction(
      int requestId, int serviceId, List<Byte> subFunctionBytes, int length) {

    Random rn = new Random();
    int offset = 0;

    byte[] curPayLoad = AbstractScenario.getGenericPayLoad();
    if (length >= 8) {
      curPayLoad[0] = (byte) 0x10;
      curPayLoad[1] = (byte) (length & 0xFF);
      curPayLoad[0] = (byte) (curPayLoad[0] + (byte) ((length >> 8) & 0x0F));
      offset = 1;
    } else {
      curPayLoad[0] = (byte) (length & 0xFF);
      offset = 0;
    }
    curPayLoad[1 + offset] = (byte) serviceId;
    int bytePos = 2;
    for (Byte funcByte : subFunctionBytes) {
      curPayLoad[bytePos + offset] = funcByte;
      bytePos++;
    }
    for (int j = bytePos + offset; j < 8; j++) {
      curPayLoad[j] = (byte) (rn.nextInt(256) & 0xFF);
    }
    CANFrame packet = new CANFrame(requestId, curPayLoad);

    return packet;
  }

  public List<Request> fuzzCurrentSubFunctionWithVaryPayLoadLength(
      int requestId, int serviceId, List<Byte> subFunctionBytes,
      int payLoadMinLength, int payLoadMaxLength, int rspWaitTime) {

    final List<Request> requests = new ArrayList<>();
    Random rn = new Random();

    for (int i = payLoadMinLength; i < payLoadMaxLength; i++) {
      int offset = 0;
      byte[] curPayLoad = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};

      if (i >= 8) {
        curPayLoad[0] = (byte) 0x10;
        curPayLoad[0] = (byte) (curPayLoad[0] + (byte) ((i >> 8) & 0x0F));
        curPayLoad[1] = (byte) (i & 0xFF);
        offset = 1;
      } else {
        curPayLoad[0] = (byte) (i & 0xFF);
        offset = 0;
      }
      curPayLoad[1 + offset] = (byte) serviceId;
      int bytePos = 2;
      for (Byte funcByte : subFunctionBytes) {
        curPayLoad[bytePos + offset] = funcByte;
        bytePos++;
      }
      // when payLoadLength == 4
      if (i == 4 && (bytePos + offset) <= 4) {
        // add 5ae29fc4
        curPayLoad[bytePos + offset] = (byte) 0x5a;
        curPayLoad[bytePos + offset + 1] = (byte) 0xe2;
        curPayLoad[bytePos + offset + 2] = (byte) 0x9f;
        curPayLoad[bytePos + offset + 3] = (byte) 0xc4;
      } else {
        for (int j = bytePos + offset; j < 8; j++) {
          curPayLoad[j] = (byte) (rn.nextInt(256) & 0xFF);
        }
      }
      requests.add(Request.Builder.newBuilder()
          .id(requestId)
          .data(curPayLoad)
          .waitTime(rspWaitTime)
          .build());

      final byte[] byteFlowControlTraffic = new byte[]{0x30, 0, 0, 0, 0, 0, 0, 0};
      //flow control traffic
      if (i >= 8) {
        requests.add(Request.Builder.newBuilder()
            .id(requestId)
            .data(byteFlowControlTraffic)
            .waitTime(rspWaitTime)
            .build());
      }
      // Adding remaining consecutive frames
      requests.addAll(getRemainingPayload(requestId, i - 6, rspWaitTime));
    }
    return requests;
  }

  protected static List<Request> getRemainingPayload(int reqId, int payLoadSize, int rspWaitTime) {
    final List<Request> requests = new ArrayList<>();
    if (payLoadSize <= 0) {
      return requests;
    }
    int remainingSize = payLoadSize;
    int index = 0x21;
    Random rn = new Random();
    do {
      byte[] curPayLoad = getGenericPayLoad();
      curPayLoad[0] = (byte) (index & 0xFF);
      for (int bytePos = 1; bytePos <= Math.min(7, remainingSize); bytePos++) {
        curPayLoad[bytePos] = (byte) (rn.nextInt(256) & 0xFF);
      }
      remainingSize = remainingSize - 7;
      index++;
      if (index >= 0x30) {
        index = 0x20;
      }
      requests.add(Request.Builder.newBuilder()
          .id(reqId)
          .data(curPayLoad)
          .waitTime(rspWaitTime)
          .build());
    } while (remainingSize > 0);

    return requests;
  }

  protected List<Request> prepareAndSendTrafficForSingleService(Integer requestId, CANResponseFilter filter,
                                                                int service_type,
                                                                List<Byte> subFunctionBytes,
                                                                int payLoadMinLength,
                                                                int payLoadMaxLength,
                                                                int rspWaitTime) {
    return fuzzCurrentSubFunctionWithVaryPayLoadLength(requestId, service_type,
        subFunctionBytes, payLoadMinLength, payLoadMaxLength, rspWaitTime);
  }

  protected Request enterSession(Integer requestId, TestConst.DiagnosticSession session) {
    byte[] sessionData = Arrays.copyOf(ENTER_PROG_SESSION, ENTER_PROG_SESSION.length);
    sessionData[2] = session.getSessionValue();
    return Request.Builder.newBuilder()
        .id(requestId)
        .data(sessionData)
        .waitTime(getResponseWaitTime(testPoints))
        .build();
  }

  public boolean isXcpRequestSendingSuccess(Collection<Request> requests, CANResponseFilter filter,
                                            Integer requestId, Set<Integer> responseIds,
                                            byte[] payload, boolean checkSendingOnly) {
    boolean connectSuccess = false;
    long sendingTime = System.currentTimeMillis() / 1000;
    while (!connectSuccess && (System.currentTimeMillis() / 1000 - sendingTime < REQUEST_TIMEOUT_INTERVAL)) {
      requests.add(createRequest(requestId, payload));
      // send traffic
      final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
      // analyze logs
      while (responses.hasNext()) {
        final Response response = responses.next();
        logRequestFrame(response.getRequest());
        final Iterator<CANFrame> frames = response.getResponses();
        while (frames.hasNext()) {
          final CANFrame frame = frames.next();
          if (responseIds.contains(frame.getId())) {
            if (checkSendingOnly) {
              connectSuccess = true;
              break;
            } else {
              byte[] data = frame.getData();
              // Check whether the XCP Service is implemented or not
              if ((data[0] & 0xFF) == 0xFF
                  || ((data[0] & 0xFF) == 0xFE && (data[1] & 0xFF) != 0x20)) {
                connectSuccess = true;
                break;
              }
            }
          }
        }
      }
    }
    return connectSuccess;
  }

  protected Request createRequest(int requestId, byte[] hackRPM) {
    Request request = null;
    request = Request.Builder.newBuilder()
        .id(requestId)
        .data(hackRPM)
        .waitTime(getResponseWaitTime(testPoints))
        .build();
    return request;
  }
}
