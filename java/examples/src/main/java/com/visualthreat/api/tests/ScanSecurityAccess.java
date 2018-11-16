package com.visualthreat.api.tests;

import static com.visualthreat.api.tests.common.TestConst.UDS_SID_READ_MEM_BY_ADDRESS;
import static com.visualthreat.api.tests.common.TestConst.UDS_SID_REQUEST_DOWNLOAD;

import com.visualthreat.api.VTCloud;
import com.visualthreat.api.data.CANFrame;
import com.visualthreat.api.data.CANResponseFilter;
import com.visualthreat.api.data.Request;
import com.visualthreat.api.data.Response;
import com.visualthreat.api.tests.common.TestPoints;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScanSecurityAccess extends AbstractScenario{

  private Map<Integer, Set<Integer>> ecuIDs;

  private static final int NUMBER_OF_REQUESTS = 50;
  private static byte[] requestSeedPacket = new byte[]{0x2, 0x27, 0x01, 0, 0, 0, 0, 0};
  private Map<Integer, Integer> seedLengthMap = new HashMap<>();

  public ScanSecurityAccess(VTCloud cloud,
      TestPoints testPoints) {
    super(cloud, testPoints);
  }

  @Override
  public void run() {
    // Get the ecu id and corresponding response id
    ecuIDs = readInPredefinedIDOrServices("ecuIDs");
    final CANResponseFilter filter = CANResponseFilter.filterIds(MIN_ID, MAX_ID);
    for(int requestId : ecuIDs.keySet()){
      internalSecurityAccessTest(requestId, filter);
    }
  }

  private void internalSecurityAccessTest(int requestId, CANResponseFilter filter) {
    log.info(String.format("Starts testing ECU=0x%X", requestId));

    checkSecurityAccessNecessity(requestId, filter);
    checkSeedUnpredictabilityTests(requestId, filter);
    checkUnlockingAndTimeDelayTests(requestId, filter);

  }

  private void checkUnlockingAndTimeDelayTests(int requestId, CANResponseFilter filter){
    final Collection<Request> requests = new ArrayList<>();
    for(int i = 0; i < NUMBER_OF_REQUESTS; i++){
      requests.add(Request.Builder.newBuilder()
          .id(requestId)
          .data(requestSeedPacket)
          .waitTime(getResponseWaitTime(testPoints))
          .build());

      for(Request request : createRequestWithRandomSeed(requestId)){
        requests.add(request);
      }
    }
    final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
    // logs
    exportLogToConsole(responses);
  }

  private void checkSeedUnpredictabilityTests(int requestId, CANResponseFilter filter){
    final Collection<Request> requests = new ArrayList<>();
    for(int i = 0; i < NUMBER_OF_REQUESTS; i++){
      requests.add(Request.Builder.newBuilder()
          .id(requestId)
          .data(requestSeedPacket)
          .waitTime(getResponseWaitTime(testPoints))
          .build());
    }

    final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
    // logs
    exportLogToConsole(responses);
    //analyze response
    analyzeCANLogsForSeedUnpredictabilityTests(responses);
  }

  private int getSeedLength(int offset, byte[] data){
    int seedLength;
    if(offset == 1){
      seedLength = (data[0] - 0x10) * 16 * 16 + data[offset] -2;
    }else{
      seedLength = data[offset] -2;
    }
    return seedLength;
  }
  private LinkedList<Request> createRequestWithRandomSeed(int requestId){
    LinkedList<Request> packets = new LinkedList<>();
    Request.Builder.newBuilder()
        .id(requestId)
        .data(requestSeedPacket)
        .waitTime(getResponseWaitTime(testPoints))
        .build();


    int seedLength = seedLengthMap.containsKey(requestId) ? seedLengthMap.get(requestId) : 0;
    byte[] packet = new byte[]{(byte) seedLength, 0x27, 0x02, 0, 0, 0, 0, 0};
    byte[] byteFlowControlTraffic = new byte[]{0x30, 0, 0, 0, 0, 0, 0, 0};
    if(seedLength <= 5){
      for(int i = 3; i < 8; i++){
        Random random = new Random();
        packet[i] = (byte) random.nextInt(255);
      }
      packets.add(Request.Builder.newBuilder()
          .id(requestId)
          .data(packet)
          .waitTime(getResponseWaitTime(testPoints))
          .build());
    }else if (seedLength > 5 && seedLength <= 255) {
      byte[] headPacket = new byte[]{10,(byte) seedLength, 0x27, 0x02, 0, 0, 0, 0};
      for(int i = 4; i < 8; i++){
        Random random = new Random();
        headPacket[i] = (byte) random.nextInt(255);
      }
      packets.add(Request.Builder.newBuilder()
          .id(requestId)
          .data(headPacket)
          .waitTime(getResponseWaitTime(testPoints))
          .build());
      packets.add(Request.Builder.newBuilder()
          .id(requestId)
          .data(byteFlowControlTraffic)
          .waitTime(getResponseWaitTime(testPoints))
          .build());
      int pos = 21;
      while(seedLength - 7 > 0){
        byte[] followPacket = new byte[]{(byte) pos, 0,0,0,0,0,0,0};
        for(int i = 1; i < 8; i++){
          Random random = new Random();
          followPacket[i] = (byte) random.nextInt(255);
        }
        packets.add(Request.Builder.newBuilder()
            .id(requestId)
            .data(followPacket)
            .waitTime(getResponseWaitTime(testPoints))
            .build());
        pos++;
        if(++pos > 2F){
          break;
        }
      }
    }
    return packets;
  }

  private void checkSecurityAccessNecessity(int requestId, CANResponseFilter filter) {

    final byte[] byteReadMemory =
        {0x07, (byte) UDS_SID_READ_MEM_BY_ADDRESS, 0x14, 0, 0, 0, 0, (byte) 0x80};
    final byte[] byteReqDownloadFirstFrame =
        {0x10, 0x0B, (byte) UDS_SID_REQUEST_DOWNLOAD, 0, 0x44, 0, 1, 0};
    final byte[] byteReqDownloadConsecutiveFrame =
        {0x21, 0x08, 0, 6, (byte) 0xFF, (byte) 0xF8, 0, 0};

    final Collection<Request> requests = new ArrayList<>();
      requests.add(Request.Builder.newBuilder()
          .id(requestId)
          .data(byteReadMemory)
          .waitTime(getResponseWaitTime(testPoints))
          .build());

      requests.add(Request.Builder.newBuilder()
          .id(requestId)
          .data(byteReqDownloadFirstFrame)
          .waitTime(getResponseWaitTime(testPoints))
          .build());

    requests.add(Request.Builder.newBuilder()
        .id(requestId)
        .data(byteReqDownloadConsecutiveFrame)
        .waitTime(getResponseWaitTime(testPoints))
        .build());

    final Iterator<Response> responses = cloud.sendCANFrames(requests, filter);
    // logs
    exportLogToConsole(responses);
  }

  private void analyzeCANLogsForSeedUnpredictabilityTests(Iterator<Response> responses){
    LinkedList<List<Byte>> seedsList = new LinkedList<>();

    boolean isPartialSeed = false;
    while (responses.hasNext()) {
      Response response = responses.next();
      CANFrame request = response.getRequest();
      Iterator<CANFrame> responseIterator = response.getResponses();
      int defaultResponseId = request.getId() + 8;
      while(responseIterator.hasNext()){
        CANFrame responseEntry = responseIterator.next();
        if ( responseEntry.getData()[0] < 0x20) {
          // Skip all the non corresponding response for requestId
          if (responseEntry.getId() != defaultResponseId) {
            continue;
          }
          int offset = getOffset(responseEntry.getData());
          if (responseEntry.getData()[1 + offset] == 0x67 && responseEntry.getData()[2 + offset] == 0x01) {
            //    2.2 Get seed length
            int seedLength = getSeedLength(offset, responseEntry.getData());
            if(!seedLengthMap.containsKey(request.getId()) && seedLength > 0) {
              seedLengthMap.put(request.getId(), seedLength);
              log.info(String.format("Seed length for ECU=0x%X is %d\n", request.getId(), seedLength));
            }
            // Add seed into seedsList handling the multi frame response
            if(offset == 1){
              isPartialSeed = true;
            }
            List<Byte> seed = new LinkedList<>();
            for(int i = 3 + offset; i < responseEntry.getData().length; i++){
              if (seed.size() >= seedLength){
                isPartialSeed = false;
                break;
              }
              seed.add(responseEntry.getData()[i]);
            }
            seedsList.add(seed);
          }
        } else if(responseEntry.getData()[0] >= 0x20 && responseEntry.getData()[0] < 0x30){
          List<Byte> seed = seedsList.getLast();
          for(int i = 1; i < responseEntry.getData().length; i++){
            if(isPartialSeed){
              seed.add(responseEntry.getData()[i]);
              if(seed.size() >= seedLengthMap.get(request.getId())){
                isPartialSeed = false;
                break;
              }
            }
          }
          seedsList.removeLast();
          seedsList.add(seed);
        }
      }
    }
  }

  private int getOffset(byte[] data){
    return data[0] >=0x10 ? 1 : 0;
  }
}
