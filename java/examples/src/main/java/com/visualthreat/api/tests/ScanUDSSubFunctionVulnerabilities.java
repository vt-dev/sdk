package com.visualthreat.api.tests;

import com.visualthreat.api.VTCloud;
import com.visualthreat.api.data.CANResponseFilter;
import com.visualthreat.api.data.Request;
import com.visualthreat.api.data.Response;
import com.visualthreat.api.tests.common.TestConst.DiagnosticSession;
import com.visualthreat.api.tests.common.TestPoints;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScanUDSSubFunctionVulnerabilities extends AbstractScenario {
  private Map<Integer, List<int[]>> ecuIDServicesAndSubFunctionIDs;

  public ScanUDSSubFunctionVulnerabilities(VTCloud cloud, TestPoints testPoint) {
    super(cloud, testPoint);
  }

  @Override
  public void run() {
    // Get the ecu id and corresponding response id
    ecuIDServicesAndSubFunctionIDs = readInPredefinedSubFunctions("ecuSubFunctions");
    final CANResponseFilter filter = CANResponseFilter.filterIds(MIN_ID, MAX_ID);
    for (Integer requestID : ecuIDServicesAndSubFunctionIDs.keySet()) {
      log.info(String.format("Starts testing ECU=0x%X", requestID));
      for(int requestId : ecuIDServicesAndSubFunctionIDs.keySet()){
        sendScanUDSSubFunctionTraffic(requestId, filter);
      }
    }
  }

  private void sendScanUDSSubFunctionTraffic(int requestId, CANResponseFilter filter){
    log.info(String.format("Starts testing ECU=0x%X", requestId));
    try {
      for(DiagnosticSession session : sessionList){
        sendTrafficForSingleService(requestId, filter, ecuIDServicesAndSubFunctionIDs.get(requestId),
            DEFAULT_MIN_PAYLOAD_LENGTH, DEFAULT_MAX_PAYLOAD_LENGTH, session);
      }
    } catch (Exception e){
      log.error("Sending ScanUDFSubFunction failed!");
    }
  }

  private void sendTrafficForSingleService(Integer requestId,
      CANResponseFilter filter,
      List<int[]> servicesAndSubFunctions,
      int payLoadMinLength,
      int payLoadMaxLength,
      DiagnosticSession session) {
    Collection<Request> requests = new ArrayList<>();
    requests.add(this.enterSession(requestId, session));
    for(int[] servicesAndSubFunction : servicesAndSubFunctions){
      List<Byte> subFunctionBytes = new ArrayList<>();
      subFunctionBytes.add((byte) servicesAndSubFunction[1]);
      subFunctionBytes.add((byte) servicesAndSubFunction[2]);
      requests.addAll(prepareAndSendTrafficForSingleService(requestId, filter,servicesAndSubFunction[0],
          subFunctionBytes,payLoadMinLength,payLoadMaxLength, getResponseWaitTime(testPoints)));
    }
    // send traffic
    final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
    // logs
    exportLogToConsole(responses);

  }

  private Map<Integer, List<int[]>> readInPredefinedSubFunctions(String fileName){
    InputStream in = getClass().getResourceAsStream("/" +fileName);
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    Map<Integer, List<int[]>> map = new HashMap<>();
    String line;
    try {
      while ((line = reader.readLine()) != null) {
        int[] serviceAndSubFunc = new int[3];
        String[] idAndSubFuncs = line.split(Pattern.quote(":"));
        // get request id
        String[] ids = idAndSubFuncs[0].split(Pattern.quote("x"));
        int requestID = Integer.parseInt(ids[1], 16);
        // get service id
        String[] rspIDs = idAndSubFuncs[1].split(Pattern.quote("x"));
        serviceAndSubFunc[0] = Integer.parseInt(rspIDs[1], 16);

        // get subfunction id
        String[] sub1 = idAndSubFuncs[2].split(Pattern.quote("x"));
        serviceAndSubFunc[1] = Integer.parseInt(sub1[1], 16);
        String[] sub2 = idAndSubFuncs[3].split(Pattern.quote("x"));
        serviceAndSubFunc[2] = Integer.parseInt(sub2[1], 16);

        if(map.containsKey(requestID)){
          List<int[]> list = map.get(requestID);
          list.add(serviceAndSubFunc);
          map.put(requestID, list);
        }else {
          List<int[]> list = new LinkedList<>();
          list.add(serviceAndSubFunc);
          map.put(requestID, list);
        }
      }
    } catch (IOException e) {
      log.error("Can't load ECU ids for ScanECUServices!");
    }
    return map;
  }
}
