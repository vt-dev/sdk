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
public class RequestUploadTest extends AbstractScenario {

  public static final int REQUEST_UPLOAD_SERVICE = 0x35;
  private static final long ADDRESS_FULL = (long) 0xFFFFFFFFL;
  private static final long ADDRESS_100M_HEX = (long) 0x10000000L;
  private static final int ADDRESS_2M = (int) 0x200000L;
  private static final int ADDRESS_16K = (int) 0x4000L;
  private static final int DOWNLOAD_SIZE = 0x455;
  private Map<Integer, Set<Integer>> ecuIds = new HashMap<>();


  public RequestUploadTest(VTCloud cloud, TestPoints testPoints) {
    super(cloud, testPoints);
  }

  public void run() {

    // Get the ecu id and corresponding response id
    ecuIds = readInPredefinedIDOrServices("ecuIDs");
    final CANResponseFilter filter = CANResponseFilter.filterIds(MIN_ID, MAX_ID);
    for (Integer id : ecuIds.keySet()) {
      try {
        for (DiagnosticSession session : sessionList) {
          sendRequestUploadTraffic(id, session, filter);
        }
      } catch (final IOException ex) {
        log.error("ECU Dump Memory Two failed", ex);
      }
    }
  }

  private void sendRequestUploadTraffic(int requestId, DiagnosticSession session, CANResponseFilter filter) throws IOException {
    log.info(String.format("Starts testing ECU=0x%X", requestId));
    final Collection<Request> requests = new ArrayList<>();
    requests.add(this.enterSession(requestId, session));
    List<Request> entries = null;
    for (long address = (long) 0x0L; address <= (long) ADDRESS_FULL; address += ADDRESS_100M_HEX) {
      /* For first 2M, send ReadMemoryByAddress every 16k */
      for (long i = address; i < (address + ADDRESS_2M); i += ADDRESS_16K) {
        entries = createTraffic(requestId, i, DOWNLOAD_SIZE);
        requests.addAll(entries);
      }
    }
    // send traffic
    final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
  }


  private List<Request> createTraffic(int requestId, final long address, final int memorySize) {
    List<Request> result = new LinkedList<>();
    final byte[] byteFfRequestUpload = {0, 0, 0, 0, 0, 0, 0, 0};
    final byte[] byteCfRequestUpload = {0, 0, 0, 0, 0, 0, 0, 0};

    /* Transmit Multiple Frame */
    /* First Frame encode */
    /* Bit 4-7: 0x1 because it is a Multiple Frame (ISO-TP) */
    /* Bit 0-3: 0x0 because this is the MSB of UDS Frame length 0xB */
    byteFfRequestUpload[0] = 0x10;
    /* RequestUpload Service ID */
    /* Bit 0-7: 0x0B because this is the LSB of UDS Frame length 0xB */
    byteFfRequestUpload[1] = 0x0B;
    byteFfRequestUpload[2] = REQUEST_UPLOAD_SERVICE;
    /* RequestUpload dataFormatIdentifier - neither compressionMethod or encryptingMethod is used */
    byteFfRequestUpload[3] = 0x0;
    /* addressAndLengthFormatIdentifier */
    /* Bit 4-7: 0x4 because memorySize is 4 byte length */
    /* Bit 0-3: 0x4 because memoryAddress is 4 byte length */
    byteFfRequestUpload[4] = 0x44;

    /* Memory address encode */
    byteFfRequestUpload[5] = (byte) ((address & 0xFF000000) >> 24);
    byteFfRequestUpload[6] = (byte) ((address & 0x00FF0000) >> 16);
    byteFfRequestUpload[7] = (byte) ((address & 0x0000FF00) >> 8);

    /* Consecutive Frame encode */
    byteCfRequestUpload[0] = 0x21;
    /* Memory address encode - continue */
    byteCfRequestUpload[1] = (byte) (address & 0x000000FF);
    /* Memory Length encode - first bytes is always 0 */
    byteCfRequestUpload[2] = 0x0;
    byteCfRequestUpload[3] = (byte) ((memorySize & 0x00FF0000) >> 16);
    ;
    byteCfRequestUpload[4] = (byte) ((memorySize & 0x0000FF00) >> 8);
    byteCfRequestUpload[5] = (byte) (memorySize & 0x000000FF);

    result.add(createRequest(requestId, byteFfRequestUpload));
    result.add(createRequest(requestId, byteCfRequestUpload));
    return result;
  }
}
