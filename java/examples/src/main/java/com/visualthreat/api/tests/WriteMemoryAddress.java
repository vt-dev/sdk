package com.visualthreat.api.tests;

import com.visualthreat.api.VTCloud;
import com.visualthreat.api.data.CANResponseFilter;
import com.visualthreat.api.data.Request;
import com.visualthreat.api.data.Response;
import com.visualthreat.api.tests.common.TestConst.DiagnosticSession;
import com.visualthreat.api.tests.common.TestPoints;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WriteMemoryAddress extends AbstractScenario {
  private Map<Integer, Set<Integer>> ecuIDs;
  private static final int WRITE_DATA_BY_ADDRESS_SERVICE = 0x3D;

  public enum ADDRESS_SIZE {
    TWO_BYTES,
    THREE_BYTES,
    FOUR_BYTES
  }
  private static Random rand = new Random();
  public WriteMemoryAddress(VTCloud cloud, TestPoints testPoint) {
    super(cloud, testPoint);
  }

  @Override
  public void run() {
    // Get the ecu id and corresponding response id
    ecuIDs = readInPredefinedIDOrServices("ecuIDs");
    final CANResponseFilter filter = CANResponseFilter.filterIds(MIN_ID, MAX_ID);
    for (Integer requestId : ecuIDs.keySet()) {
      log.info(String.format("Starts testing ECU=0x%X", requestId));
      sendWriteMemoryByAddressTraffic(requestId, filter);
    }
  }

  private void sendWriteMemoryByAddressTraffic(Integer requestId, CANResponseFilter filter) {
    // create request
    Collection<Request> requests = new ArrayList<>();
    requests.add(enterSession(requestId, DiagnosticSession.PROGRAMMING));
    for (int addr = 0; addr < 2048; addr = addr + 16) {
      requests.addAll(createWriteMemTraffic(requestId, addr, ADDRESS_SIZE.FOUR_BYTES));
    }
    // send traffic
    final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
    // logs
    exportLogToConsole(responses);
    // write random address
    requests.clear();
    requests.add(enterSession(requestId, DiagnosticSession.PROGRAMMING));
    for (int i = 0; i < 4096; i++) {
      int addr = randomInt(2048, 1024 * 1024);
      requests.addAll(createWriteMemTraffic(requestId, addr, ADDRESS_SIZE.FOUR_BYTES));
    }
    // send traffic
    final Iterator<Response> modifyByRandomAddrResponses = cloud.sendCANFrames(requests, filter);
    // logs
    exportLogToConsole(modifyByRandomAddrResponses);
  }

  private List<Request> createWriteMemTraffic(
      Integer requestId, int address, ADDRESS_SIZE addressSize) {
    List<Request> requests = new ArrayList<>();

    if (addressSize == ADDRESS_SIZE.TWO_BYTES || addressSize == ADDRESS_SIZE.THREE_BYTES) {
      byte[] curPayLoad = getGenericPayLoad();
      curPayLoad[1] = WRITE_DATA_BY_ADDRESS_SERVICE;
      if (addressSize == ADDRESS_SIZE.TWO_BYTES) {
        curPayLoad[2] = 0x12;
        curPayLoad[3] = (byte) ((address >> 8) & 0xFF);
        curPayLoad[4] = (byte) (address & 0xFF);
        curPayLoad[5] = 0x1;
        curPayLoad[6] = (byte) 0xff; // data
        curPayLoad[0] = 0x06;
      } else {
        curPayLoad[2] = 0x13;
        curPayLoad[3] = (byte) ((address >> 16) & 0xFF);
        curPayLoad[4] = (byte) ((address >> 8) & 0xFF);
        curPayLoad[5] = (byte) (address & 0xFF);
        curPayLoad[6] = 0x1;
        curPayLoad[7] = (byte) 0xff; // data
        curPayLoad[0] = 0x07;
      }
      requests.add(Request.Builder.newBuilder()
          .id(requestId)
          .data(curPayLoad)
          .waitTime(getResponseWaitTime(testPoints))
          .build()
      );
    } else if (addressSize == ADDRESS_SIZE.FOUR_BYTES) {
      // need to send multiple packets in ISO-TP protocol
      byte[] curPayLoad = getGenericPayLoad();
      curPayLoad[0] = 0x10;
      curPayLoad[1] = 0x08; // send 8 bytes
      curPayLoad[2] = WRITE_DATA_BY_ADDRESS_SERVICE;
      curPayLoad[3] = 0x14;
      curPayLoad[4] = (byte) ((address >> 24) & 0xFF);
      curPayLoad[5] = (byte) ((address >> 16) & 0xFF);
      curPayLoad[6] = (byte) ((address >> 8) & 0xFF);
      curPayLoad[7] = (byte) (address & 0xFF);
      requests.add(Request.Builder.newBuilder()
          .id(requestId)
          .data(curPayLoad)
          .waitTime(getResponseWaitTime(testPoints))
          .build()
      );
      curPayLoad = getGenericPayLoad();
      curPayLoad[0] = 0x21; // second packet
      curPayLoad[1] = 0x5;
      curPayLoad[2] = (byte) 0xFF; // data
      curPayLoad[3] = (byte) 0xFF; // data
      curPayLoad[4] = (byte) 0xFF; // data
      curPayLoad[5] = (byte) 0xFF; // data
      curPayLoad[6] = (byte) 0xFF; // data
      requests.add(Request.Builder.newBuilder()
          .id(requestId)
          .data(curPayLoad)
          .waitTime(getResponseWaitTime(testPoints))
          .build()
      );
    }
    return requests;
  }

  public static int randomInt(int from, int to) {
    return rand.nextInt((to - from) + 1) + from;
  }
}
