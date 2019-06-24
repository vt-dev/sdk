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
public class GMLANReadDataByIdentifier extends AbstractScenario {

  private static final int GMLAN_READ_DATA_BY_IDENTIFIER_SERVICE = 0x1A;
  private Map<Integer, Set<Integer>> ecuIDs;

  public GMLANReadDataByIdentifier(VTCloud cloud, TestPoints testPoints) {
    super(cloud, testPoints);
  }

  @Override
  public void run() {

    Collection<Integer> responseIds = new HashSet<>();
    // Get the ecu id and corresponding response id
    ecuIDs = readInPredefinedIDOrServices("ecuIDs");
    final CANResponseFilter filter = CANResponseFilter.filterIds(MIN_ID, MAX_ID);
    for (Integer id : ecuIDs.keySet()) {
      try {
        /* Send GMLAN REad Data By Identifier traffic */
        for (DiagnosticSession session : sessionList) {
          sendReadDataByIdentifierTraffic(id, session, filter);
        }
      } catch (final IOException ex) {
        log.error("GMLAN Read Data By Identifier failed", ex);
      }
    }
  }

  private void sendReadDataByIdentifierTraffic(
      int requestId, DiagnosticSession session, CANResponseFilter filter) throws IOException {
    log.info(String.format("Starts testing ECU=0x%X", requestId));
    final Collection<Request> requests = new ArrayList<>();
    requests.add(this.enterSession(requestId, session));
    // Loop through different data identifier and check whether we can read or not
    for (int dataIdentifier = 0; dataIdentifier <= 0xFF; dataIdentifier++) {
      requests.add(createTraffic(requestId, dataIdentifier));
    }
    // send traffic
    final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
  }

  private Request createTraffic(int requestId, int dataIdentifier) {
    final byte[] byteSfReadDataByIdentifierRequest = {0, 0, 0, 0, 0, 0, 0, 0};
    /* Transmit Single Frame */
    /* Bit 4-7: 0x0 because it is a Single Frame (ISO-TP) */
    /* Bit 0-3: 0x2 because UDS Frame length is 2 */
    byteSfReadDataByIdentifierRequest[0] = 0x02;
    /* ReadMemoryByAddress Service ID */
    byteSfReadDataByIdentifierRequest[1] = (byte) GMLAN_READ_DATA_BY_IDENTIFIER_SERVICE;
    /* dataIdentifier */
    byteSfReadDataByIdentifierRequest[2] = (byte) dataIdentifier;

    Request packet = createRequest(requestId, byteSfReadDataByIdentifierRequest);
    return packet;
  }
}
