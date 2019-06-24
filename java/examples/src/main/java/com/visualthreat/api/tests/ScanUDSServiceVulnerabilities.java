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
public class ScanUDSServiceVulnerabilities extends AbstractScenario {
  private Map<Integer, Set<Integer>> ecuIDAndServicesIDs;

  public ScanUDSServiceVulnerabilities(VTCloud cloud, TestPoints testPoint) {
    super(cloud, testPoint);
  }

  @Override
  public void run() {
    // Get the ecu id and corresponding response id
    ecuIDAndServicesIDs = readInPredefinedIDOrServices("ecuServices");
    final CANResponseFilter filter = CANResponseFilter.filterIds(MIN_ID, MAX_ID);
    for (Integer requestID : ecuIDAndServicesIDs.keySet()) {
      log.info(String.format("Starts testing ECU=0x%X", requestID));
      sendUDSServicesVulnerabilitiesTraffic(requestID, ecuIDAndServicesIDs.get(requestID), filter);
    }
  }

  private void sendUDSServicesVulnerabilitiesTraffic(Integer requestId, Set<Integer> serviceIDs, CANResponseFilter filter) {
    try {
      log.info("Start to send Scan UDSServiceVulnerabilities requests.\n");
      List<Byte> subFunctionBytes = new ArrayList<>();
      subFunctionBytes.add((byte) 0x0);
      sendTrafficForGivenGroupServices(requestId, filter, serviceIDs,
          subFunctionBytes, DEFAULT_MIN_PAYLOAD_LENGTH, DEFAULT_MAX_PAYLOAD_LENGTH);
    } catch (InterruptedException ignored) {
      Thread.currentThread().interrupt();
    } catch (IOException ex) {
      log.error("send UDSServiceVulnerabilities failed", ex);
    }
  }

  private void sendTrafficForGivenGroupServices(Integer requestId,
                                                CANResponseFilter filter, Set<Integer> serviceIDs,
                                                List<Byte> subFunctionBytes,
                                                int payLoadMinLength,
                                                int payLoadMaxLength) throws IOException, InterruptedException {
    Collection<Request> requests = new ArrayList<>();
    requests.add(this.enterSession(requestId, DiagnosticSession.PROGRAMMING));
    for (int serviceID : serviceIDs) {
      requests.addAll(prepareAndSendTrafficForSingleService(requestId, filter, serviceID,
          subFunctionBytes, payLoadMinLength, payLoadMaxLength, getResponseWaitTime(testPoints)));
    }
    // send traffic
    final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
    // logs
    exportLogToConsole(responses);
  }
}
