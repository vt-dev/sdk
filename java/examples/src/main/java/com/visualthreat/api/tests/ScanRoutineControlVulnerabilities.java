package com.visualthreat.api.tests;

import com.visualthreat.api.VTCloud;
import com.visualthreat.api.data.CANResponseFilter;
import com.visualthreat.api.data.Request;
import com.visualthreat.api.data.Response;
import com.visualthreat.api.tests.common.TestConst.DiagnosticSession;
import com.visualthreat.api.tests.common.TestPoints;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScanRoutineControlVulnerabilities extends AbstractScenario {
  private Map<Integer, Set<Integer>> ecuIDAndServicesIDs;
  private static final int ROUTINE_CONTROL_SERVICE = 0x31;


  public ScanRoutineControlVulnerabilities(VTCloud cloud, TestPoints testPoint) {
    super(cloud, testPoint);
  }

  @Override
  public void run() {
    // Get the ecu id and corresponding response id
    ecuIDAndServicesIDs = readInPredefinedIDOrServices("ecuServices");
    final CANResponseFilter filter = CANResponseFilter.filterIds(MIN_ID, MAX_ID);

    for (Integer requestID : ecuIDAndServicesIDs.keySet()) {
      log.info(String.format("Starts testing ECU=0x%X", requestID));
      modifyByRoutineControl(requestID, filter);
    }
  }

  private void modifyByRoutineControl(Integer requestId, CANResponseFilter filter) {
    // start to send traffic
    eraseMemoryAttempt(requestId, filter);

    List<Byte> subFunctionBytes = new ArrayList<>(8);
    subFunctionBytes.add((byte)0x0);
    subFunctionBytes.add((byte)0x0);
    subFunctionBytes.add((byte)0x0);
    subFunctionBytes.set(0,((byte)0x01));
    for(DiagnosticSession session : sessionList){
      sendTrafficForMultipleServices(requestId, filter, subFunctionBytes,
          DEFAULT_MIN_PAYLOAD_LENGTH, DEFAULT_MAX_PAYLOAD_LENGTH, session);
    }
  }

  private void eraseMemoryAttempt(int requestId, CANResponseFilter filter) {
    // starts erase memory attempt
    final Collection<Request> requests = new ArrayList<>();
    requests.add(Request.Builder.newBuilder()
        .id(requestId)
        .data(new byte[]{0x10, 0x0C, (byte) 0x31, 0x01, (byte) 0xFF, 0x0, 0x0, 0x01})
        .waitTime(getResponseWaitTime(testPoints))
        .build());

    requests.add(Request.Builder.newBuilder()
        .id(requestId)
        .data(new byte[]{0x21, 0x0, 0x0, 0x0, 0x07, 0x0, 0x0, 0x0})
        .waitTime(getResponseWaitTime(testPoints))
        .build());

    List<Byte> subFunctionBytes = new ArrayList<>();
    subFunctionBytes.add((byte) 0x01);
    subFunctionBytes.add((byte) 0xFF);
    subFunctionBytes.add((byte) 0x00);
    List<Request> entries =
        fuzzCurrentSubFunctionWithVaryPayLoadLength(requestId, 0x31, subFunctionBytes,
            4, 24, getResponseWaitTime(testPoints));
    requests.addAll(entries);
    // send traffic
    final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
    // logs
    exportLogToConsole(responses);
  }

  private void sendTrafficForMultipleServices(Integer requestId, CANResponseFilter filter,
      List<Byte> subFunctionBytes, int payLoadMinLength, int payLoadMaxLength, DiagnosticSession session) {
    final Collection<Request> requests = new ArrayList<>();
    requests.add(this.enterSession(requestId, session));
    List<Request> entries = null;
    for (int j = 0; j < 256; j++) {
      subFunctionBytes.set(1, (byte) (j & 0xFF));
      for(int k = 0; k < 2; k++) {
        subFunctionBytes.set(2, (byte) (k & 0xFF));
        entries = prepareAndSendTrafficForSingleService(requestId, filter, ROUTINE_CONTROL_SERVICE,
            subFunctionBytes, payLoadMinLength, payLoadMaxLength, getResponseWaitTime(testPoints));
      }
      requests.addAll(entries);
    }
    // send traffic
    final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
    // logs
    exportLogToConsole(responses);
  }

}
