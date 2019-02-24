package com.visualthreat.api.tests;

import com.visualthreat.api.VTCloud;
import com.visualthreat.api.data.CANResponseFilter;
import com.visualthreat.api.data.Request;
import com.visualthreat.api.data.Response;
import com.visualthreat.api.tests.common.TestConst.DiagnosticSession;
import com.visualthreat.api.tests.common.TestPoints;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class XCPGetCommInfo extends AbstractScenario {

  private static final int XCP_GET_COMM_MODE = 0xFB;
  private Map<Integer, Set<Integer>> ecuIds = new HashMap<>();


  public XCPGetCommInfo(VTCloud cloud, TestPoints testPoints) {
    super(cloud, testPoints);
  }

  @Override
  public void run(){

    // Get the ecu id and corresponding response id
    ecuIds = readInPredefinedIDOrServices("ecuIDs");
    final CANResponseFilter filter = CANResponseFilter.filterIds(MIN_ID, MAX_ID);
    for (Integer id : ecuIds.keySet()) {
      try {
        for(DiagnosticSession session : sessionList){
          sendXCPGetCommModeTraffic(id, session,filter);
        }
      } catch (final IOException ex) {
        log.error("XCP Get Communication Mode failed", ex);
      }
    }
  }

  private void sendXCPGetCommModeTraffic(
      int requestId, DiagnosticSession session,CANResponseFilter filter) throws IOException {
    log.info(String.format("Starts testing ECU=0x%X", requestId));
    final Collection<Request> requests = new ArrayList<>();
    requests.add(this.enterSession(requestId, session));
    byte[] byteXCPGetCommMode = {(byte) XCP_GET_COMM_MODE, 0, 0, 0, 0, 0, 0, 0};
    requests.add(createRequest(requestId, byteXCPGetCommMode));
    // send traffic
    final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
  }
}
