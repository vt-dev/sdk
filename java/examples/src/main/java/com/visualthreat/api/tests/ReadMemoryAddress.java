package com.visualthreat.api.tests;

import com.visualthreat.api.VTCloud;
import com.visualthreat.api.data.CANResponseFilter;
import com.visualthreat.api.data.Request;
import com.visualthreat.api.data.Response;
import com.visualthreat.api.tests.common.TestParameters;
import com.visualthreat.api.tests.common.TestPoints;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;

@Slf4j
public class ReadMemoryAddress extends AbstractScenario {
  private Map<Integer, Set<Integer>> ecuIDAndServicesIDs;
  private static final int READ_DATA_BY_ADDRESS_SERVICE = 0x23;
  private static final long ADDRESS_100M_HEX = 0x10000000L;
  private static final int ADDRESS_2M = (int) 0x200000L;
  private static final int ADDRESS_16K = (int) 0x4000L;
  private static final byte MAX_READABLE_LENGTH = (byte) 0x80;

  private Long startAddress;
  private Long stopAddress;

  public ReadMemoryAddress(VTCloud cloud, TestPoints testPoint) {
    super(cloud, testPoint);
  }

  @Override
  public void run() {
    // Get the ecu id and corresponding response id
    ecuIDAndServicesIDs = readInPredefinedIDOrServices("ecuServices");
    final CANResponseFilter filter = CANResponseFilter.filterIds(MIN_ID, MAX_ID);
    this.startAddress = TestParameters.START_ADDRESS.getDefaultValue();
    this.stopAddress = TestParameters.STOP_ADDRESS.getDefaultValue();

    for (Integer requestID : ecuIDAndServicesIDs.keySet()) {
      try {
        this.sendReadMemoryByAddressTraffic(requestID, filter);
      } catch (final IOException ex) {
        log.error("ECU Dump Memory failed", ex);
      }
    }
  }

  private void sendReadMemoryByAddressTraffic(int requestId, CANResponseFilter filter) throws IOException {
    for (long address = this.startAddress; address <= this.stopAddress;
         address += ADDRESS_100M_HEX) {
      final Collection<Request> requests = new ArrayList<>();
      for (long i = address; i < (address + ADDRESS_2M); i += ADDRESS_16K) {
        createReadTraffic(requests, requestId, i, MAX_READABLE_LENGTH);
      }
      // send traffic
      final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
      // logs
      exportLogToConsole(responses);
    }
  }

  private void createReadTraffic(
      Collection<Request> requests, int requestId, final long address, final byte memorySize) {
    final byte[] byteSingleFrameReadMemoryRequest = {0, 0, 0, 0, 0, 0, 0, 0};
    byteSingleFrameReadMemoryRequest[0] = 0x07;
    byteSingleFrameReadMemoryRequest[1] = READ_DATA_BY_ADDRESS_SERVICE;
    byteSingleFrameReadMemoryRequest[2] = 0x14;
    byteSingleFrameReadMemoryRequest[7] = memorySize;

    byteSingleFrameReadMemoryRequest[3] = (byte) ((address & 0xFF000000) >> 24);
    byteSingleFrameReadMemoryRequest[4] = (byte) ((address & 0x00FF0000) >> 16);
    byteSingleFrameReadMemoryRequest[5] = (byte) ((address & 0x0000FF00) >> 8);
    byteSingleFrameReadMemoryRequest[6] = (byte) (address & 0x000000FF);

    requests.add(Request.Builder.newBuilder()
        .id(requestId)
        .data(byteSingleFrameReadMemoryRequest)
        .waitTime(getResponseWaitTime(testPoints))
        .build());
  }
}
