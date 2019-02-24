package com.visualthreat.api.tests;

import com.visualthreat.api.VTCloud;
import com.visualthreat.api.data.CANResponseFilter;
import com.visualthreat.api.data.Request;
import com.visualthreat.api.tests.common.TestConst.DiagnosticSession;
import com.visualthreat.api.tests.common.TestPoints;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class XCPServicesDiscovery extends AbstractScenario {
  public static final String UNKNOWN_SERVICE_NAME = "UNKNOWN_SERVICE";

  private static final List MANDATORY_SERVICES = Arrays.asList(0xFF,0xFE);
  private static final byte[] xcpConnectQueryPayload = new byte[]{(byte) 0xFF, 0x00, 0x00, 0x00, 0x00,
      0x00, 0x00, 0x00};
  private Map<Integer, Set<Integer>> ecuIds = new HashMap<>();

  public XCPServicesDiscovery(VTCloud cloud, TestPoints testPoints) {
    super(cloud, testPoints);
  }

  @Override
  public void run() {
    log.info("Starting discovering XCP supported services");
    // Get the ecu id and corresponding response id
    ecuIds = readInPredefinedIDOrServices("ecuIDs");
    final CANResponseFilter filter = CANResponseFilter.filterIds(MIN_ID, MAX_ID);
    for(DiagnosticSession session : sessionList){
      discoverXCPServices(session,filter);
    }
  }

  private void discoverXCPServices(DiagnosticSession session,CANResponseFilter filter){
    log.info("Total XCP ECU IDs: {}", ecuIds.size());
    Map<Integer, List<String>> supportedService = new HashMap<>();

    for (Integer reqId : ecuIds.keySet()) {
      log.info("XCP ECU ID: {}", reqId);

      // Get responseIds
      Set<Integer> responseIds = ecuIds.get(reqId);
      final Collection<Request> requests = new ArrayList<>();
      requests.add(this.enterSession(reqId, session));
      // send traffic
      // XCP has lists of commands it has, we only need to loop through
      // this list, and find the supported command
      for(Integer serviceId : XCPIdsDiscovery.XCP_COMMAND_CODES.keySet()){
        // Skip the CONNECT and DISCONNECT services
        if(MANDATORY_SERVICES.contains(serviceId & 0xFF)){
          continue;
        }
        // Only send service discovery request when connect request sending successfully
        if(isXcpRequestSendingSuccess(requests, filter, reqId, responseIds, xcpConnectQueryPayload, true)){
          byte[] curPayLoad = Arrays.copyOf(xcpConnectQueryPayload, xcpConnectQueryPayload.length);
          curPayLoad[0] = (byte) (serviceId & 0xFF);
          // Test whether the service is supported or not first
          if(isXcpRequestSendingSuccess(requests, filter, reqId, responseIds, curPayLoad, false)){
            String serviceName = getXCPServiceName(serviceId);
            if (supportedService.containsKey(reqId)) {
              supportedService.get(reqId).add(String.format("Service:%s, Service ID:0x%x", serviceName, curPayLoad[0]));
            } else {
              supportedService.put(reqId, new ArrayList<>());
              supportedService.get(reqId).add(String.format("Service:%s, Service ID:0x%x", serviceName, curPayLoad[0]));
            }
          }
        }
      }
      log.info(String.format("Finish sending traffic for ECU=0x%X", reqId));
    }

    // print out result
    log.info("\n ============== Supported XCP Service =================>");
    for (Integer id : supportedService.keySet()) {
      List<String> services = supportedService.get(id);
      services.forEach(entry -> log.info(entry));
    }
  }

  private static String getXCPServiceName(Integer key) {
    String serviceName = XCPIdsDiscovery.XCP_COMMAND_CODES.get(key);
    if (serviceName == null) {
      serviceName = UNKNOWN_SERVICE_NAME;
    }
    return serviceName;
  }
}
