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

@SuppressWarnings("unchecked")
@Slf4j
public class ManipulateCommunication extends AbstractScenario {

  private static final int COMMUNICATION_CONTROL_SERVICE = 0x28;
  private static final byte DATA_PADDING = 0x00;
  private static final byte ENABLE_TX_RX = 0x00;
  private static final byte DISABLE_TX_ENABLE_RX = 0x01;
  private static final byte ENABLE_TX_DISABLE_RX = 0x02;
  private static final byte DISABLE_TX_RX = 0x03;
  private static final byte ENABLE_TX_DISABLE_RX_ENHANCED_ADDRESS = 0x04;
  private static final byte ENABLE_TX_RX_ENHANCED_ADDRESS = 0x05;
  private Map<Integer, Set<Integer>> ecuIds = new HashMap<>();


  private HashMap<Byte, String> COMM_CONTROL_SUB_FUNCTION = new HashMap();

  {
    COMM_CONTROL_SUB_FUNCTION.put(ENABLE_TX_RX, "Enable Transmit and Receive.");
    COMM_CONTROL_SUB_FUNCTION.put(DISABLE_TX_ENABLE_RX, "Disable Transmit and Enable Receive.");
    COMM_CONTROL_SUB_FUNCTION.put(ENABLE_TX_DISABLE_RX, "Enable Transmit and Disable Receive.");
    COMM_CONTROL_SUB_FUNCTION.put(DISABLE_TX_RX, "Disable Transmit and Receive.");
    COMM_CONTROL_SUB_FUNCTION.put(ENABLE_TX_DISABLE_RX_ENHANCED_ADDRESS,
        "Enable Transmit and Disable Receive with Enhanced Address Information.");
    COMM_CONTROL_SUB_FUNCTION.put(ENABLE_TX_RX_ENHANCED_ADDRESS,
        "Enable Transmit and Receive with Enhanced Address Information.");
  }

  public ManipulateCommunication(VTCloud cloud, TestPoints testPoints) {
    super(cloud, testPoints);
  }

  @Override
  public void run() {
    // Get the ecu id and corresponding response id
    ecuIds = readInPredefinedIDOrServices("ecuIDs");
    final CANResponseFilter filter = CANResponseFilter.filterIds(MIN_ID, MAX_ID);
    for (Integer id : ecuIds.keySet()) {
      try {
        for (DiagnosticSession session : sessionList) {
          sendCommunicationControlTraffic(id, session, filter);
        }
      } catch (final IOException ex) {
        log.error("ECU Manipulate Communication failed", ex);
      }
    }
  }

  private void sendCommunicationControlTraffic(
      int requestId, DiagnosticSession session, CANResponseFilter filter) throws IOException {
    log.info(String.format("Starts testing ECU=0x%X", requestId));
    final Collection<Request> requests = new ArrayList<>();
    requests.add(this.enterSession(requestId, session));
    List<Request> entries = null;
    // The Communication control service need two parameters
    // the controlType(subFunction) and the communicationType
    for (byte subFunction = 0; subFunction <= 5; subFunction++) {
      for (byte communicationType = 1; communicationType <= 3; communicationType++) {
        requests.add(createWriteTraffic(requestId, subFunction, communicationType));
      }
    }
    // send traffic
    final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
  }

  private Request createWriteTraffic(int requestId, final byte subFunction, final byte communicationType) {
    byte[] byteSingleFrameCommControlRequest = new byte[]{3, 0x28, 0, 0, 0, 0, 0, 0};

    /* Transmit Single Frame */
    /* Bit 4-7: 0x0 because it is a Single Frame (ISO-TP) */
    /* Bit 0-3: 0x3 because UDS Frame length is 5 if controlType is 4 or 5 */
    if ((subFunction == 4) || (subFunction == 5)) {
      byteSingleFrameCommControlRequest[0] = 0x05;
    } else {
      /* Current tested ECU response with 0x13 - incorrect format if encoded with 0x5 */
      byteSingleFrameCommControlRequest[0] = 0x03;
    }
    /* COMMUNICATION_CONTROL_SERVICE Service ID */
    byteSingleFrameCommControlRequest[1] = COMMUNICATION_CONTROL_SERVICE;
    /* subFunction */
    byteSingleFrameCommControlRequest[2] = subFunction;
    /* memorySize is 0xFF: read 255 bytes from memory */
    byteSingleFrameCommControlRequest[3] = communicationType;
    if ((subFunction == 4) || (subFunction == 5)) {
      /* This should be encoded with nodeIdentificationNumber, but Current tested ECU */
      /* response with 0x13 - incorrect format if encoded with nodeIdentificationNumber */
      byteSingleFrameCommControlRequest[4] = DATA_PADDING;
      byteSingleFrameCommControlRequest[5] = DATA_PADDING;
    }
    return createRequest(requestId, byteSingleFrameCommControlRequest);
  }
}
