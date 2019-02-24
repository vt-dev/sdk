package com.visualthreat.api.tests;

import com.visualthreat.api.VTCloud;
import com.visualthreat.api.data.CANFrame;
import com.visualthreat.api.data.CANResponseFilter;
import com.visualthreat.api.data.Request;
import com.visualthreat.api.data.Response;
import com.visualthreat.api.tests.common.TestPoints;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("unchecked")
@Slf4j
public class XCPIdsDiscovery extends AbstractScenario {

  public static final int MIN_XCP_CAN_ID = 0x100;
  public static final int MAX_XCP_CAN_ID = 0x7ff;

  private static final byte[] xcpQueryPayload = new byte[]{(byte) 0xFF, 0x00, 0x00, 0x00, 0x00,
      0x00, 0x00, 0x00};
  final Collection<String> ecuIDs = new HashSet<>();

  public static HashMap<Integer, String[]> XCP_ERROR_CODES = new HashMap(){
    {
      put(0x00, new String[]{"ERR_CMD_SYNC", "Command processor synchronisation."});
      put(0x10, new String[]{"ERR_CMD_BUSY", "Command was not executed."});
      put(0x11, new String[]{"ERR_DAQ_ACTIVE", "Command rejected because DAQ is running."});
      put(0x12, new String[]{"ERR_PGM_ACTIVE", "Command rejected because PGM is running."});
      put(0x20,
          new String[]{"ERR_CMD_UNKNOWN", "Unknown command or not implemented optional command."});
      put(0x21, new String[]{"ERR_CMD_SYNTAX", "Command syntax invalid."});
      put(0x22, new String[]{"ERR_OUT_OF_RANGE",
          "Command syntax valid but command parameter(s) out of range."});
      put(0x23, new String[]{"ERR_WRITE_PROTECTED", "The memory location is write protected."});
      put(0x24, new String[]{"ERR_ACCESS_DENIED", "The memory location is not accessible."});
      put(0x25, new String[]{"ERR_ACCESS_LOCKED", "Access denied, Seed & Key is required."});
      put(0x26, new String[]{"ERR_PAGE_NOT_VALID", "Selected page not available."});
      put(0x27, new String[]{"ERR_MODE_NOT_VALID", "Selected page mode not available."});
      put(0x28, new String[]{"ERR_SEGMENT_NOT_VALID", "Selected segment not valid."});
      put(0x29, new String[]{"ERR_SEQUENCE", "Sequence error."});
      put(0x2A, new String[]{"ERR_DAQ_CONFIG", "DAQ configuration not valid."});
      put(0x30, new String[]{"ERR_MEMORY_OVERFLOW", "Memory overflow error."});
      put(0x31, new String[]{"ERR_GENERIC", "Generic error."});
      put(0x32,
          new String[]{"ERR_VERIFY", "The slave internal program verify routine detects an error."});
    }
  };

  public static HashMap<Integer, String> XCP_COMMAND_CODES = new HashMap() {
    {
      put(0xFF, "CONNECT");
      put(0xFE, "DISCONNECT");
      put(0xFD, "GET_STATUS");
      put(0xFC, "SYNCH");
      put(0xFB, "GET_COMM_MODE_INFO");
      put(0xFA, "GET_ID");
      put(0xF9, "SET_REQUEST");
      put(0xF8, "GET_SEED");
      put(0xF7, "UNLOCK");
      put(0xF6, "SET_MTA");
      put(0xF5, "UPLOAD");
      put(0xF4, "SHORT_UPLOAD");
      put(0xF3, "BUILD_CHECKSUM");
      put(0xF2, "TRANSPORT_LAYER_CMD");
      put(0xF1, "USER_CMD");
      put(0xF0, "DOWNLOAD");
      put(0xEF, "DOWNLOAD_NEXT");
      put(0xEE, "DOWNLOAD_MAX");
      put(0xED, "SHORT_DOWNLOAD");
      put(0xEC, "MODIFY_BITS");
      put(0xEB, "SET_CAL_PAGE");
      put(0xEA, "GET_CAL_PAGE");
      put(0xE9, "GET_PAG_PROCESSOR_INFO");
      put(0xE8, "GET_SEGMENT_INFO");
      put(0xE7, "GET_PAGE_INFO");
      put(0xE6, "SET_SEGMENT_MODE");
      put(0xE5, "GET_SEGMENT_MODE");
      put(0xE4, "COPY_CAL_PAGE");
      put(0xE3, "CLEAR_DAQ_LIST");
      put(0xE2, "SET_DAQ_PTR");
      put(0xE1, "WRITE_DAQ");
      put(0xE0, "SET_DAQ_LIST_MODE");
      put(0xDF, "GET_DAQ_LIST_MODE");
      put(0xDE, "START_STOP_DAQ_LIST");
      put(0xDD, "START_STOP_SYNCH");
      put(0xDC, "GET_DAQ_CLOCK");
      put(0xDB, "READ_DAQ");
      put(0xDA, "GET_DAQ_PROCESSOR_INFO");
      put(0xD9, "GET_DAQ_RESOLUTION_INFO");
      put(0xD8, "GET_DAQ_LIST_INFO");
      put(0xD7, "GET_DAQ_EVENT_INFO");
      put(0xD6, "FREE_DAQ");
      put(0xD5, "ALLOC_DAQ");
      put(0xD4, "ALLOC_ODT");
      put(0xD3, "ALLOC_ODT_ENTRY");
      put(0xD2, "PROGRAM_START");
      put(0xD1, "PROGRAM_CLEAR");
      put(0xD0, "PROGRAM");
      put(0xCF, "PROGRAM_RESET");
      put(0xCE, "GET_PGM_PROCESSOR_INFO");
      put(0xCD, "GET_SECTOR_INFO");
      put(0xCC, "PROGRAM_PREPARE");
      put(0xCB, "PROGRAM_FORMAT");
      put(0xCA, "PROGRAM_NEXT");
      put(0xC9, "PROGRAM_MAX");
      put(0xC8, "PROGRAM_VERIFY");
    }
  };

  public XCPIdsDiscovery(VTCloud cloud, TestPoints testPoints) {
    super(cloud, testPoints);
  }

  @Override
  public void run() {
    sendECUDiscoveryTraffic();
  }


  /**
   * send UDS traffic ID in range [0x600, 0x7FF]
   */
  private void sendECUDiscoveryTraffic() {
    final Collection<Request> requests = new ArrayList<>();
    final CANResponseFilter filter = CANResponseFilter.filterIds(MIN_ID, MAX_ID);
    for (int i = MIN_XCP_CAN_ID; i <= MAX_XCP_CAN_ID; i++) {
      requests.add(createRequest(i, xcpQueryPayload));
    }
    // send traffic
    final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
    while (responses.hasNext()) {
      final Response response = responses.next();
      logRequestFrame(response.getRequest());
      final Iterator<CANFrame> frames = response.getResponses();
      while (frames.hasNext()) {
        final CANFrame frame = frames.next();
        logResponseFrame(frame);
        if (frame.getData()[0] == (byte) 0xFF && frame.getData()[3] == 0x08) {
          ecuIDs.add(String.format("\t0x%x -> 0x%x", response.getRequest().getId(), frame.getId()));
        }
      }
    }
    log.info("Found {} ECU ID pairs", ecuIDs.size());
    for (final String s : ecuIDs) {
      log.info(s);
    }
  }
}
