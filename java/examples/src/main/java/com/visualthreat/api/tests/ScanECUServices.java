package com.visualthreat.api.tests;

import com.visualthreat.api.VTCloud;
import com.visualthreat.api.data.CANResponseFilter;
import com.visualthreat.api.data.Request;
import com.visualthreat.api.data.Response;
import com.visualthreat.api.tests.common.ECUServices;
import com.visualthreat.api.tests.common.TestPoints;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScanECUServices extends AbstractScenario{

  private Map<Integer, Set<Integer>> ecuIDs;
  private static final byte[] ECU_PAYLOAD = {0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

  public ScanECUServices(VTCloud cloud, TestPoints testPoints) {
    super(cloud, testPoints);
  }

  @Override
  public void run(){
    try {
      // Get the ecu id and corresponding response id
      ecuIDs = readInPredefinedIDOrServices("ecuIDs");
      final CANResponseFilter filter = CANResponseFilter.filterIds(MIN_ID, MAX_ID);

      final Collection<Request> requests = new ArrayList<>();
      for (int requestID : ecuIDs.keySet()) {
        byte[] curPayLoad = ECU_PAYLOAD;
        Request request = null;
        for (int i = 0; i < 256; i++) {
          curPayLoad[1] = (byte) (i & 0xFF);
          request = Request.Builder.newBuilder()
              .id(requestID)
              .data(curPayLoad)
              .waitTime(getResponseWaitTime(testPoints))
              .build();
          requests.add(request);
        }
        final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
        exportLogToConsole(responses);
      }
    } catch (final Exception e) {
      log.error("Critical Exception", e);
    }
  }
}
