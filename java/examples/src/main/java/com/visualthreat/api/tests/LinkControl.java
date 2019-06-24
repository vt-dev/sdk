package com.visualthreat.api.tests;

import com.visualthreat.api.VTCloud;
import com.visualthreat.api.data.CANResponseFilter;
import com.visualthreat.api.data.Request;
import com.visualthreat.api.data.Response;
import com.visualthreat.api.tests.common.TestConst.DiagnosticSession;
import com.visualthreat.api.tests.common.TestPoints;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class LinkControl extends AbstractScenario {

  private static byte[] test1RequestPacket = new byte[]{0x03, (byte) 0x87, 0x01, 0x05, 0, 0, 0, 0};
  private static byte[] test2RequestPacket = new byte[]{0x05, (byte) 0x87, 0x02, 0x02, 0x49, (byte) 0xF0, 0, 0};
  private Map<Integer, Set<Integer>> ecuIds = new HashMap<>();


  public LinkControl(VTCloud cloud, TestPoints testPoints) {
    super(cloud, testPoints);
  }

  @Override
  public void run() {
    Set<Integer> responseIds = new HashSet<>();
    // Get the ecu id and corresponding response id
    ecuIds = readInPredefinedIDOrServices("ecuIDs");
    final CANResponseFilter filter = CANResponseFilter.filterIds(MIN_ID, MAX_ID);

    for (Integer id : ecuIds.keySet()) {
      for (DiagnosticSession session : sessionList) {
        sendLinkControlTraffic(id, session, filter);
      }
    }
  }

  private void sendLinkControlTraffic(
      Integer requestId, DiagnosticSession session, CANResponseFilter filter) {

    final Collection<Request> requests = new ArrayList<>();
    requests.add(this.enterSession(requestId, session));
    // Test1: Transition baud rate to fixed baud rate
    // Step 1: send request to verify if all criteria are met for a baud rate switch
    requests.add(createRequest(requestId, test1RequestPacket));

    // Test 2: Transition baud rate to specific baud rate
    // Step 1: send request to verify if all criteria are met for a baud rate switch
    requests.add(createRequest(requestId, test2RequestPacket));
    // send traffic
    final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
  }
}
