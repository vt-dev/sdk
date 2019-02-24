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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("unchecked")
@Slf4j
public class GMLANReadFailureRecord extends AbstractScenario {

  private static final int GMLAN_READ_FAILURE_RECORD_SERVICE = 0x12;
  private Map<Integer, Set<Integer>> ecuIDs;

  public GMLANReadFailureRecord(VTCloud cloud, TestPoints testPoints) {
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
        for(DiagnosticSession session : sessionList){
          sendReadFailureRecordTraffic(id, session,filter);
        }
      } catch (final IOException ex) {
        log.error("GMLAN Read Failure Record failed", ex);
      }
    }
  }

  private void sendReadFailureRecordTraffic(
      int requestId, DiagnosticSession session,CANResponseFilter filter) throws IOException {
    log.info(String.format("Starts testing ECU=0x%X", requestId));
    final Collection<Request> requests = new ArrayList<>();
    requests.add(this.enterSession(requestId, session));
    // Loop through 1-4 as combination for data identifier
    for (int dataIdentifier = 1; dataIdentifier <= 4; dataIdentifier++) {
      requests.addAll(createTraffic(requestId, dataIdentifier));
    }
    // send traffic
    final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
  }

  private List<Request> createTraffic(int requestId, int dataIdentifier) {
    final byte[] byteSfReadDataByIdentifierRequest = {0, 0, 0, 0, 0, 0, 0, 0};

    /* Transmit Single Frame */
    /* Bit 4-7: 0x0 because it is a Single Frame (ISO-TP) */
    /* Bit 0-3: 0x2 because UDS Frame length is 2 */
    byteSfReadDataByIdentifierRequest[0] = 0x04;
    /* ReadMemoryByAddress Service ID */
    byteSfReadDataByIdentifierRequest[1] = GMLAN_READ_FAILURE_RECORD_SERVICE;
    /* dataIdentifier */
    byteSfReadDataByIdentifierRequest[2] = (byte) dataIdentifier;
    List<Request> list = new LinkedList<>();
    for (int i = 0; i <= 0x2FF; i++) {
      byteSfReadDataByIdentifierRequest[3] = (byte) (i / 0x100);
      byteSfReadDataByIdentifierRequest[4] = (byte) (i % 0x100);
      list.add(createRequest(requestId, byteSfReadDataByIdentifierRequest));
    }
    return list;
  }
}
