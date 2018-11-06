package com.visualthreat.api.tests;

import com.visualthreat.api.VTCloud;
import com.visualthreat.api.data.CANResponseFilter;
import com.visualthreat.api.data.Request;
import com.visualthreat.api.data.Response;
import com.visualthreat.api.tests.common.TestPoints;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ECUSubFunctionsDiscovery extends AbstractScenario {
  private Map<Integer, Set<Integer>> ecuIDAndServicesIDs;
  private static final byte[] udsServiceQueryPayload = new byte[]{0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

  public ECUSubFunctionsDiscovery(VTCloud cloud, TestPoints testPoint) {
    super(cloud, testPoint);
  }

  @Override
  public void run() {
    // Get the ecu id and corresponding response id
    ecuIDAndServicesIDs = readInPredefinedIDOrServices("ecuServices");
    final CANResponseFilter filter = CANResponseFilter.filterIds(MIN_ID, MAX_ID);
    discoverServiceSubFunctions(ecuIDAndServicesIDs, filter);
  }

  /**
   * Discover Subfunctions of Supported Services
   */
  private void discoverServiceSubFunctions(
      Map<Integer, Set<Integer>> ecuIDAndServicesIDs, CANResponseFilter filter) {
    for (Integer reqId : ecuIDAndServicesIDs.keySet()) {
      Random rn = new Random();
      Set<Integer> serviceIDs = ecuIDAndServicesIDs.get(reqId);
      for (int serviceId : serviceIDs) {
        // make sure serviceId is not negative
        serviceId = serviceId & 0xFF;
        final Collection<Request> requests = new ArrayList<>();
        byte[] curPayLoad = Arrays.copyOf(udsServiceQueryPayload, udsServiceQueryPayload.length);
        curPayLoad[0] = 0x04;
        curPayLoad[1] = (byte)(serviceId & 0xFF);
        for (int j = 0; j < 256; j++) {
          curPayLoad[2] = (byte) (j & 0xFF);
          for (int k = 0; k < 5; k++) {
            curPayLoad[3] = (byte) (k & 0xFF);
            for (int bytePos = 4; bytePos < 8; bytePos++) {
              curPayLoad[bytePos] = (byte) (rn.nextInt(256) & 0xFF);
            }
            requests.add(Request.Builder.newBuilder()
                .id(reqId)
                .data(curPayLoad)
                .waitTime(getResponseWaitTime(testPoints))
                .build());
          }
        }
        // send traffic
        final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
        // logs
        exportLogToConsole(responses);
      }
    }
  }
}
