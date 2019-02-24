package com.visualthreat.api.tests;

import com.visualthreat.api.VTCloud;
import com.visualthreat.api.data.CANFrame;
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
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class XCPModifyMemory extends AbstractScenario {

  private static final byte[] xcpConnectQueryPayload = new byte[]{(byte) 0xFF, 0x00, 0x00, 0x00, 0x00,
      0x00, 0x00, 0x00};
  private Map<Integer, Set<Integer>> ecuIds = new HashMap<>();


  public XCPModifyMemory(VTCloud cloud, TestPoints testPoints) {
    super(cloud, testPoints);
  }

  @Override
  public void run() {

    // Get the ecu id and corresponding response id
    ecuIds = readInPredefinedIDOrServices("ecuIDs");
    final CANResponseFilter filter = CANResponseFilter.filterIds(MIN_ID, MAX_ID);
    for (Integer id : ecuIds.keySet()) {
      log.info(String.format("Starts testing ECU=0x%X", id));
      try {
        for(DiagnosticSession session : sessionList){
          sendXCPClearMemoryTraffic(id, session,filter);
        }
      } catch (final IOException ex) {
        log.error("XCP Modify Memory failed", ex);
      }
    }
  }

  private void sendXCPClearMemoryTraffic(
      int requestId, DiagnosticSession session,CANResponseFilter filter) throws IOException {
    final Collection<Request> requests = new ArrayList<>();
    requests.add(this.enterSession(requestId, session));
    // Modify memory with random clear ranges for 50 times
    for (int i = 0; i <= 50; i++) {
      // First need to check xcp connection ready or not
      if(isXcpRequestSendingSuccess(requests, filter, requestId, ecuIds.get(requestId),
          xcpConnectQueryPayload,true)){
        //Send Program Start packet before Program Clear
        byte[] byteXCPProgramStart = {(byte)0xD2, 0, 0, 0, 0, 0, 0, 0};
        requests.add(createRequest(requestId, byteXCPProgramStart));
        // Send program clear memory packets
        byte[] byteXCPClearMemory = {(byte)0xD1, 0, 0, 0, 0, 0, 0, 0};
        Random rn = new Random();
        for(int j = 4; j < 8; j++){
          byteXCPClearMemory[j] = (byte) (rn.nextInt(256) & 0xFF);
        }
        requests.add(createRequest(requestId, byteXCPClearMemory));
        // send traffic
        final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
      }
    }
  }
}
